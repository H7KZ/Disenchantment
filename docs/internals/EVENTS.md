<!-- generated-by: gsd-doc-writer -->

# Event System

`core/src/main/java/com/jankominek/disenchantment/events/`

The anvil mechanics are driven by four event handlers: two for disenchanting (PrepareAnvil + InventoryClick) and two for
shattering. A shared utility class `AnvilEventGuards` provides validation logic used by all four.

---

## EventExecutor Pattern

All four anvil listeners use the `EventExecutor` pattern instead of `@EventHandler` annotations. This allows the
`EventPriority` to be configured at runtime:

```java
// In DisenchantListener constructor:
getServer().getPluginManager().registerEvent(
    PrepareAnvilEvent.class,
    listener,             // empty Listener instance
    priority,             // configured EventPriority
    this,                 // the EventExecutor
    plugin,
    false                 // ignoreCancelled
);
```

`DisenchantListener` implements `EventExecutor` and delegates to `DisenchantEvent.onEvent(e)`. Same pattern for the
other three listeners.

The priority is read from `Config.EventPriorities.getDisenchantEvent()` etc., which parses from `config.yml` at
listener-registration time (during `enable()`). **Changing priorities requires a reload.**

---

## `AnvilEventGuards`

`events/AnvilEventGuards.java` — pure static utility, no state.

### Slot detection

```java
public static boolean isAnvilResultSlotClick(InventoryClickEvent e, Player p)
```

Returns `true` when the inventory is an ANVIL, the clicked slot is 2 (result slot), and the player's cursor is empty.
Cursor check prevents deleting held items.

### Enchantment collection

```java
public static List<IPluginEnchantment> collectEnchantments(
    ItemStack firstItem,
    ItemStack secondItem,
    boolean isPrepare,
    EnchantmentCollector baseCollector,
    PluginEnchantmentCollector pluginCollector,
    World world)
```

- If no plugins are activated, calls `baseCollector` (vanilla).
- If plugins are activated, calls `pluginCollector` for each activated plugin.
- **Deduplicates by enchantment key** — first occurrence wins. Prevents double costs when multiple adapters claim the
  same enchantment.

### XP guard

```java
public static boolean hasEnoughXp(Player p, int repairCost)
```

Returns `true` if the player is in creative mode OR has at least `repairCost` levels.

### Economy processing

```java
public static EconomyResult processEconomy(Player p, EconomyConfig config)
```

Returns an `EconomyResult` enum:

- `OK` — economy disabled, player in creative, or withdrawal succeeded.
- `NOT_AVAILABLE` — economy enabled but Vault not hooked.
- `INSUFFICIENT_FUNDS` — player balance below required cost.

The caller is responsible for cancelling the event and sending messages for non-OK results.

```java
public static void showEconomyActionBar(Player p, EconomyConfig config)
```

Sends the economy cost as an action bar message during `PrepareAnvilEvent` if `isShowCostEnabled()` is true.

### Anvil cost application

```java
public static void applyAnvilCostAndSchedule(
    PrepareAnvilEvent e,
    Player p,
    List<IPluginEnchantment> enchantments,
    AnvilEventType eventType)
```

Sets the anvil repair cost immediately via `AnvilCostUtils.setAnvilRepairCost()`, then schedules a one-tick follow-up
refresh via `SchedulerUtils.runForEntity(...)`. The scheduled refresh is required because the Minecraft client caches
anvil cost and ignores server updates without the additional tick.

### Second-item removal

```java
public static void scheduleSecondItemRemoval(Player p, AnvilInventory anvilInventory, ItemStack secondItem)
```

Schedules a **2-tick** deferred reduction of the blank book in anvil slot 1. The 2-tick delay is intentional:
EnchantsSquared replaces slot 1 with null after 1 tick, so a 1-tick delay would be overridden. The method decrements
amount if `> 1`, otherwise sets slot to null.

---

## `EconomyConfig` Interface

Used to abstract disenchantment vs shatterment economy settings:

```java
public interface EconomyConfig {
    boolean isEnabled();
    double getCost();
    boolean isChargeMessageEnabled();
    default boolean isShowCostEnabled() { return false; }
}
```

Each event handler (`DisenchantEvent`, `DisenchantClickEvent`, etc.) declares a static anonymous implementation:

```java
private static final AnvilEventGuards.EconomyConfig ECONOMY_CONFIG = new AnvilEventGuards.EconomyConfig() {
    public boolean isEnabled()             { return Config.Disenchantment.Economy.isEnabled(); }
    public double getCost()                { return Config.Disenchantment.Economy.getCost(); }
    public boolean isChargeMessageEnabled(){ return Config.Disenchantment.Economy.isChargeMessageEnabled(); }
    public boolean isShowCostEnabled()     { return Config.Disenchantment.Economy.isShowCostEnabled(); }
};
```

---

## Disenchant Flow

### Step 1 — `DisenchantEvent` (PrepareAnvilEvent)

`events/DisenchantEvent.java` — invoked by `DisenchantListener`.

1. Verify event is `PrepareAnvilEvent`.
2. Extract player from `e.getInventory().getViewers().get(0)`. (Uses `getViewers()` not `getView().getPlayer()` to avoid
   `IncompatibleClassChangeError` in environments where `InventoryView` changed from class to interface in 1.21.)
3. Guard: `Config.isPluginEnabled()`, `Config.Disenchantment.isEnabled()`, world not in disabled list.
4. Call `AnvilEventGuards.collectEnchantments(...)` using `EventUtils.Disenchantment.getDisenchantedEnchantments(...)`
   as the collectors. If empty, return.
5. Permission check: `PermissionGroupType.DISENCHANT_EVENT.hasPermission(p)`.
6. Build result `ItemStack(Material.ENCHANTED_BOOK)`, call `pluginEnchantment.addToBook(book)` for each enchantment.
7. Call `e.setResult(book)`.
8. Call `AnvilEventGuards.applyAnvilCostAndSchedule(...)`.
9. Call `AnvilEventGuards.showEconomyActionBar(p, ECONOMY_CONFIG)`.

### Step 2 — `DisenchantClickEvent` (InventoryClickEvent)

`events/DisenchantClickEvent.java` — invoked by `DisenchantClickListener`.

1. Verify `InventoryClickEvent`, extract player.
2. Guards: plugin/feature enabled, world not disabled.
3. `AnvilEventGuards.isAnvilResultSlotClick(e, p)` — must be slot 2, cursor empty.
4. `result = anvilInventory.getItem(2)` — must be non-null and `ENCHANTED_BOOK`.
5. Collect enchantments again (same logic as PrepareAnvil but `isPrepare=false`).
6. Read repair cost: `AnvilCostUtils.getRepairCost(anvilInventory, e.getView())`.
7. XP check: `AnvilEventGuards.hasEnoughXp(p, repairCost)` — cancel event if insufficient.
8. Permission check.
9. Economy check: `AnvilEventGuards.processEconomy(p, ECONOMY_CONFIG)` — cancel and message if non-OK.
10. **Fire `PreDisenchantEvent`** (cancellable) — cancel Bukkit event if pre-event is cancelled.
11. Calculate new XP: `exp = p.getLevel() - repairCost`.
12. Find enchantments to delete: `EventUtils.Disenchantment.findEnchantmentsToDelete(enchantments)`.
13. Remove enchantments from `firstItem`:
    - Without plugins: `EnchantmentUtils.removeEnchantments(finalFirstItem, resultItemMeta.getStoredEnchants())`, then
      remove DELETE-state enchantments.
    - With plugins: iterate activated plugins, call `activatedPlugin.getItemEnchantments(result, world)` and
      `enchantment.removeFromItem(finalFirstItem)` for each.
14. Optionally reset repair cost: `AnvilCostUtils.setItemRepairCost(finalFirstItem, 0)`.
15. Set `anvilInventory.setItem(0, finalFirstItem)`.
16. Schedule second-item removal.
17. Deduct XP: `p.setLevel(exp)` (skipped in creative mode).
18. Give result book: `p.setItemOnCursor(result)`.
19. **Fire `PostDisenchantEvent`**.
20. Play sound if enabled.

---

## Shatter Flow

Identical structure to disenchant, using `ShatterListener`/`ShatterClickEvent` and `EventUtils.Shatterment.*` as
collectors. Key differences:

- Slot 0 must be `ENCHANTED_BOOK` (not a regular enchanted item).
- Requires `>= 2` enchantments on the book (`EventUtils.Shatterment.getShattermentEnchantments` rejects single-enchant
  books).
- Result is a new `ENCHANTED_BOOK` with only the split-off enchantment(s). `Config.Shatterment.getSplitCount()` controls
  how many enchantments split off per operation (default 1).
- Public API events: `PreShatterEvent` / `PostShatterEvent`.

---

## Public API Events

All in `events/api/`.

### `PreDisenchantEvent` (cancellable)

```java
public class PreDisenchantEvent extends Event implements Cancellable {
    public Player getPlayer()                          // the player performing the operation
    public ItemStack getSourceItem()                   // clone of the enchanted item in slot 0
    public List<IPluginEnchantment> getEnchantments()  // enchantments about to be transferred
    public boolean isCancelled()
    public void setCancelled(boolean cancel)
}
```

Fired from `DisenchantClickEvent` handler **after** XP and economy checks but **before** item modification. If
cancelled, the Bukkit `InventoryClickEvent` is also cancelled (player gets nothing, items unchanged).

### `PostDisenchantEvent` (not cancellable)

```java
public class PostDisenchantEvent extends Event {
    public Player getPlayer()
    public ItemStack getResultBook()          // clone of the enchanted book given to the player
    public ItemStack getModifiedSourceItem()  // clone of the source item after enchantment removal
}
```

Fired after item modification and XP deduction, before sound playback.

### `PreShatterEvent` / `PostShatterEvent`

Same fields and semantics as the disenchant variants but for the shattering operation. `PreShatterEvent` is cancellable.

---

## `GUIClickEvent`

`events/GUIClickEvent.java` — registered as a standard `Listener` at `EventPriority.NORMAL`.

Routes `InventoryClickEvent` to the correct GUI screen by checking `e.getClickedInventory().getHolder()` type:

| Holder type       | Routed to                             | Permission check                                                 |
|-------------------|---------------------------------------|------------------------------------------------------------------|
| `NavigationGUI`   | `navigationGUI.onInventoryClick(e)`   | `GUI`                                                            |
| `RepairGUI`       | `repairGUI.onInventoryClick(e)`       | `GUI_DISENCHANT_REPAIR` or `GUI_SHATTER_REPAIR` based on feature |
| `SoundGUI`        | `soundGUI.onInventoryClick(e)`        | `GUI_DISENCHANT_SOUND` or `GUI_SHATTER_SOUND`                    |
| `WorldsGUI`       | `worldsGUI.onInventoryClick(e)`       | `GUI_WORLDS`                                                     |
| `MaterialsGUI`    | `materialsGUI.onInventoryClick(e)`    | `GUI_MATERIALS`                                                  |
| `EnchantmentsGUI` | `enchantmentsGUI.onInventoryClick(e)` | `GUI_ENCHANTMENTS`                                               |
| `EconomyGUI`      | `economyGUI.onInventoryClick(e)`      | `GUI_DISENCHANT_ECONOMY` or `GUI_SHATTER_ECONOMY`                |

Each GUI class implements `InventoryHolder` so Bukkit associates the inventory with it.

---

## `IPluginEnchantment` in Events

`IPluginEnchantment` is the common wrapper for **both** vanilla Bukkit enchantments and third-party plugin enchantments.
This allows event handlers to work with a single type regardless of source.

For vanilla enchantments, `EnchantmentUtils.getItemEnchantments(ItemStack)` returns a list of `IPluginEnchantment`
wrappers around Bukkit `Enchantment` + level pairs.

For third-party enchantments, each `ISupportedPlugin.getItemEnchantments(ItemStack)` returns plugin-specific
`IPluginEnchantment` implementations.

See [PLUGIN_ADAPTERS.md](PLUGIN_ADAPTERS.md) for details on the `IPluginEnchantment` interface.
