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

`GrindstoneListener` follows the same pattern but resolves the prepare event by class name (see [Grindstone Flow](#grindstone-flow)).

`DisenchantListener` implements `EventExecutor` and delegates to `DisenchantEvent.onEvent(e)`. Same pattern for the
other three listeners.

The priority is read from `Config.EventPriorities.getDisenchantEvent()` etc., which parses from `config.yml` at
listener-registration time (during `enable()`). **Changing priorities requires a reload.**

`DisenchantListener`'s constructor additionally registers one `AnvilSessionListener` (`listeners/`, plain `@EventHandler`
methods, not priority-configurable): the pending-blank-book click/drag lock, settle-on-close, and cleanup of the
bypass-cost and shatter-selection caches on anvil close / `PlayerQuitEvent`. It lives there only so it is (re)registered
exactly once together with the anvil listeners.

---

## `AnvilEventGuards`

`events/AnvilEventGuards.java` — pure static utility, no state.

### Slot detection

```java
public static boolean isAnvilResultSlotClick(InventoryClickEvent e, Player p)
```

Returns `true` when the inventory is an ANVIL, the clicked **raw** slot is 2 (result slot), and the player's cursor is
empty. Cursor check prevents deleting held items.

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

Returns `true` if the player is in creative mode OR has at least `repairCost` levels. The click handlers send
`messages.not-enough-levels` (with the real, possibly bypass-cached cost) when it fails.

### Maintenance, cooldown and quiet variants

`PrepareAnvilEvent` fires on every keystroke, so it must never message the player:

| Method                              | Messages? | Used by                                                        |
|-------------------------------------|-----------|----------------------------------------------------------------|
| `isMaintenanceActiveFor(Player)`    | no        | Prepare handlers, **after** a genuine operation was detected   |
| `isMaintenanceBlocked(Player)`      | yes       | Click handlers, once a genuine operation is confirmed          |
| `isOnCooldownQuiet(Player)`         | no        | Prepare handlers (just clear the result)                       |
| `isOnCooldown(Player)`              | yes       | Click handlers                                                 |

Maintenance mode only blocks genuine plugin operations (enchantments collected non-empty); vanilla repair/rename is
never touched. Because Prepare nulls the result in maintenance, the click handlers run the maintenance check **before**
the `result == null` check so the player clicking the empty slot is told why.

### Economy processing

```java
public static EconomyResult processEconomy(Player p, EconomyConfig config)
public static EconomyResult processEconomyCost(Player p, EconomyConfig config, double cost)
```

Returns an `EconomyResult` enum:

- `OK` — economy disabled, player in creative, or withdrawal succeeded.
- `NOT_AVAILABLE` — economy enabled but Vault not hooked.
- `INSUFFICIENT_FUNDS` — player balance below required cost.
- `WITHDRAW_FAILED` — the balance check passed but the provider's `EconomyResponse` was unsuccessful (or null). The
  player is **not** charged.

The caller is responsible for cancelling the event and sending messages for non-OK results. Both click handlers do all
abortable precondition checks (result meta, ...) **before** charging, so nothing after the charge can abort without
delivering the result.

#### What is charged

The charge uses the same set the Prepare action bar displays: the enchantments actually transferred to the result book.
`EventUtils.findTransferredEnchantments(collected, resultBook, activatedPlugins, world)` derives it at click time from
the result book (vanilla stored enchants plus each active adapter's view of it) mapped back to the click-time collected
list. The click-time collection is wider (it keeps `DELETE`-state enchantments, and for shatter every split candidate),
so charging from it overcharged. `Pre*Event.getEnchantments()` carries the transferred enchantments plus the
`DELETE`-state ones that will be destroyed on the source (a listener may remove an entry to veto its deletion); the
economy amount is computed from that list restricted to the transferred keys (`EventUtils.retainByKey`).

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

Schedules a **2-tick** deferred consumption of one blank book from anvil slot 1. The delay is intentional (commit
`e86f4a2`): the click handlers run before vanilla finishes the take, which itself clears slot 1, and EnchantsSquared nulls
slot 1 one tick later, so a synchronous (or 1-tick) write is overwritten. The stack is therefore re-written with one book
fewer after 2 ticks. It cannot simply be made synchronous without losing the remaining books of a stack.

The window between click and write used to be exploitable (close the anvil / move the books / two clicks in one tick), so
it is closed with an **operation-pending** mark:

- `scheduleSecondItemRemoval(p, inv, secondItem, originClick)` registers the anvil inventory in a weak, synchronized map.
- `isOperationPending(inv)` — both click handlers refuse (and cancel) a second operation on a pending anvil.
- `isLockedFor(inv, click)` — `AnvilSessionListener` cancels every other `InventoryClickEvent` / `InventoryDragEvent` on a
  pending anvil (the click that started the operation is exempt).
- `settlePendingRemoval(inv)` — idempotent; run by the deferred task **and** by `AnvilSessionListener` on
  `InventoryCloseEvent` (which fires before the server returns slot contents), so closing early cannot keep the consumed
  book. It only writes slot 1 if it is empty or still holds the same book type, and never writes back more books than
  remain (`min(current, original - 1)`); a foreign item is left alone.

### Bypass-cost cache lifecycle

`bypassCostCache` (UUID → real cost) is set by Prepare when `bypass-too-expensive` is on and the cost is `>= 40`, read by
`peekBypassCost`, and cleared by `clearBypassCost` on success, on the anvil `InventoryCloseEvent`, and on
`PlayerQuitEvent` (both in `AnvilSessionListener`).

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
4. Collect enchantments again (same logic as PrepareAnvil but `isPrepare=false`); empty = not ours, return.
5. Maintenance check (messages + cancels), then `result = anvilInventory.getItem(2)` — must be non-null and
   `ENCHANTED_BOOK`; result meta must be present (checked **before** any charge).
6. Unsafe click type (shift/number-key/...) → cancel + `messages.use-normal-click`. Operation still pending on this anvil
   → cancel. Cooldown → cancel + message.
7. Read repair cost: `AnvilCostUtils.getRepairCost(anvilInventory, e.getView())`.
8. XP check: `AnvilEventGuards.hasEnoughXp(p, repairCost)` — cancel + `messages.not-enough-levels` if insufficient.
9. Permission check.
10. **Fire `PreDisenchantEvent`** (cancellable) with the transferred + DELETE-state enchantments — cancel Bukkit event
    if pre-event is cancelled.
11. Economy: `processEconomyCost(...)` on the transferred set — cancel and message on `NOT_AVAILABLE`,
    `INSUFFICIENT_FUNDS` or `WITHDRAW_FAILED`.
12. Calculate new XP: `exp = p.getLevel() - repairCost`. Find enchantments to delete:
    `EventUtils.Disenchantment.findEnchantmentsToDelete(enchantments)`.
13. Remove enchantments from `firstItem`:
    - Without plugins: `EnchantmentUtils.removeEnchantments(finalFirstItem, resultItemMeta.getStoredEnchants())`, then
      remove DELETE-state enchantments.
    - With plugins: iterate activated plugins, call `activatedPlugin.getItemEnchantments(result, world)` and
      `enchantment.removeFromItem(finalFirstItem)` for each.
14. Optionally reset repair cost: `AnvilCostUtils.setItemRepairCost(finalFirstItem, 0)`.
15. Set `anvilInventory.setItem(0, finalFirstItem)`.
16. Schedule second-item removal.
17. Deduct XP: `p.setLevel(exp)` (skipped in creative mode).
18. Per-enchantment chance rolls. If **every** roll failed (and no adapter enchantment remains) a plain `BOOK` is
    delivered instead of an empty enchanted book, with `messages.enchantments-failed-to-transfer`.
19. Give result book: `p.setItemOnCursor(...)`.
20. **Fire `PostDisenchantEvent`**.
21. Play sound if enabled.

---

## Shatter Flow

Identical structure to disenchant, using `ShatterListener`/`ShatterClickEvent` and `EventUtils.Shatterment.*` as
collectors. Key differences:

- Slot 0 must be `ENCHANTED_BOOK` (not a regular enchanted item).
- Requires `>= 2` enchantments on the book (`EventUtils.Shatterment.getShattermentEnchantments` rejects single-enchant
  books).
- Result is a new `ENCHANTED_BOOK` with only the split-off enchantment (s). `Config.Shatterment.getSplitCount()`
  controls how many enchantments split off per operation (default 1).
- The chosen subset is **cached per player** (`ShatterEvent.selectSplit`) together with snapshots of slot 0 / slot 1
  (`isSimilar` + amount) and the split-count config, so repeated `PrepareAnvilEvent`s (rename keystrokes) don't re-roll it.
  Any input change drops it on the next prepare; `ShatterEvent.clearSelection(p)` runs when an operation completes and
  from `AnvilSessionListener` on anvil close / quit.
- Public API events: `PreShatterEvent` / `PostShatterEvent`. `PreShatterEvent.getEnchantments()` is the split subset
  (see "What is charged" above), not every enchantment on the source book.

---

## Grindstone Flow

`events/GrindstoneEvent.java` (prepare), `events/GrindstoneClickEvent.java` (clicks), `events/GrindstoneOps.java`
(pure computation), `listeners/GrindstoneListener.java` (registration). Two opt-in operations share one pipeline;
`Config.Grindstone` holds the settings (read live, not cached).

### Registration

`GrindstoneListener` registers `InventoryClickEvent` at `event-priorities.grindstone-click` and the prepare event at
`event-priorities.grindstone`. The core is compiled against Spigot 1.18, which has no `PrepareGrindstoneEvent`
(Spigot 1.19.4+; Paper has `com.destroystokyo.paper.event.inventory.PrepareGrindstoneEvent` since 1.16), so the class is
looked up by name (`org.bukkit.event...` first, Paper's legacy one second; only the first found is registered) and
handled as a plain `Event`: the inventory comes from `InventoryEvent#getInventory()` (`instanceof GrindstoneInventory`) and
the result is set through a cached reflective `setResult(ItemStack)`. If neither class exists the listener logs an INFO
line and uses a **tick-based fallback**: after any click on a grindstone it schedules (`SchedulerUtils.runForEntityLater`,
1 tick) `GrindstoneEvent.refresh`, which runs the same `prepare` logic and writes slot 2 directly.

Research notes: Paper's `GrindstoneMenu` patch calls `callPrepareGrindstoneEvent(view, computeResult(...))`
unconditionally, so the event also fires for combinations vanilla rejects (different items, plain book). The vanilla
input slots only accept damageable or enchanted items (`mayPlace`), which is why a plain book needs the click
interception below.

### Prepare (`GrindstoneEvent.prepare(player, inventory, setResult)`)

Slot 1 is a blank book (`EventUtils.isBlankBook`) -> **extract** (needs `grindstone.enabled`); otherwise -> **transfer**
(needs `grindstone.transfer.enabled`). Nothing is touched when the combination is not a plugin operation, so vanilla
results stay. Extract uses `AnvilEventGuards.collectEnchantments` with the `EventUtils.Disenchantment` collectors
(worlds, materials, states, plugin adapters) and drops curses unless `grindstone.curses`. Maintenance mode and cooldown
clear the result silently, a missing permission leaves it alone. The cost is shown in the action bar (no cost label on a
grindstone).

### Clicks (`GrindstoneClickEvent`)

- **Book placement** (raw slot 1, cursor is a blank book, LEFT/RIGHT; or shift-click of a blank book in the player
  inventory): if slot 1 is empty and slot 0 holds something extractable, the click is cancelled and exactly one book moves
  into slot 1 via `inventory.setItem` (the API bypasses `mayPlace`). No-op on a server that already allows it.
- **Result click** (raw slot 2), manual-result pattern. Order: maintenance -> result present -> **cancel from here on** ->
  cursor empty -> unsafe click (`isUnsafeResultClick`) -> cooldown -> XP -> permission -> `Pre*Event` -> economy ->
  mutate. The result is **recomputed from slots 0/1** at click time; the item in slot 2 is only a "something is shown"
  check, so a stale result can never be delivered. All items are built before the first write, then (after XP is
  deducted) slot 2 is cleared, slot 1 is consumed, the stripped source goes into slot 0 and the result onto the cursor.
  Vanilla's own take (which clears both inputs and awards XP orbs) never runs.
- Extract per-enchantment chance rolls and the all-failed plain-book fallback mirror `DisenchantClickEvent`. A `Pre*`
  listener removing an entry from `getEnchantments()` vetoes that enchantment (it neither moves nor is removed).

### Transfer (`GrindstoneOps.computeTransfer`)

Vanilla `minecraft:` enchantments only; source and target single, non-book, different materials; per enchantment (highest
level first): not `KEEP`, curse rule, `canEnchantItem(target)`, level clamp, skip if target already >=, no
`conflictsWith` against the target's other enchantments or those already chosen; capped at `grindstone.transfer.max-enchantments`.
A `DISABLE` state on any source enchantment or a restricted source material rejects the whole transfer.

New `AnvilEventType` values `GRINDSTONE_EXTRACT` / `GRINDSTONE_TRANSFER` select the `grindstone.repair` /
`grindstone.transfer.repair` blocks in `AnvilCostUtils.countAnvilCost`. `StatsListener` records the Post events as
`OperationType.GRIND_EXTRACT` / `GRIND_TRANSFER` (not counted in the disenchant/shatter totals).

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

### `PreGrindstoneExtractEvent` / `PostGrindstoneExtractEvent` / `PreGrindstoneTransferEvent` / `PostGrindstoneTransferEvent`

Fired from `GrindstoneClickEvent` at the same points as the disenchant events (Pre after the XP check, before economy and
mutation; Post after the mutation with `xpCost` + `economyCost`). See [api/EVENTS.md](../api/EVENTS.md).

---

## `GUIClickEvent`

`events/GUIClickEvent.java` — registered as a standard `Listener` at `EventPriority.NORMAL`.

Handles `InventoryClickEvent` and `InventoryDragEvent`. While the view's top inventory belongs to one of the plugin
GUIs, every click (top or bottom inventory, any click type) and every drag is cancelled first; clicks on the GUI inventory
are then routed to the correct screen by checking `e.getClickedInventory().getHolder()` type:

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
