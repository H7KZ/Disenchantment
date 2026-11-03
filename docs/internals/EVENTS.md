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
empty. Cursor check prevents deleting held items. Disenchant/shatter ignore a full cursor (an enchanted book cannot stack, so
vanilla cannot merge onto it). `isAnvilResultSlot(e)` is the cursor-agnostic variant used by erase, whose result may stack:
a full cursor on a prepared erase result is **cancelled**, never ignored.

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

### Too Expensive!

```java
public static boolean refuseIfTooExpensive(Player p, AnvilEventType eventType, int cost)
```

The click handlers deliver the result themselves, ahead of vanilla's pick-up check (which blocks the take at 40+ levels), so
they enforce the cap too: not creative, `isTooExpensive(type, cost)` (cost >= 40 and the operation's `bypass-too-expensive`
off) -> `FeedbackUtils.refuse` with `messages.bedrock-status-expensive`, click cancelled. `cost` is the bypass-cached real
cost. Disenchant, shatter and erase call it after the XP check; the grindstone has no vanilla cap and does not.

### Pre-cancelled clicks

Every click handler that acts manually (`Disenchant/Shatter/EraseClickEvent`, `GrindstoneClickEvent`, `GUIClickEvent`) starts
with `if (e.isCancelled()) return;` - listeners are registered with `ignoreCancelled=false`, so an earlier veto
(protection/shop plugin) is checked explicitly. `GUIClickEvent` still cancels the click first, then returns.

### XP guard

```java
public static boolean hasEnoughXp(Player p, int repairCost)
```

Returns `true` if the player is in creative mode OR has at least `repairCost` levels. The click handlers send
`messages.not-enough-levels` (with the real, possibly bypass-cached cost) when it fails.

### Special anvil gate

```java
public static boolean isAnvilAllowed(Inventory anvilInventory, Player p)
public static void sendSpecialAnvilHint(Player p)
```

`isAnvilAllowed` applies `special-anvils.mode`: `ALL` is always `true` (and costs one config read); `MARKED` asks
`SpecialAnvilStore.isMarked(block)` for `anvilInventory.getLocation()`; `BLOCK_BELOW` compares the block under the
anvil with `Config.SpecialAnvils.getBlockBelow()`. A `null` location, or a location whose block is not an anvil
(plugin-opened anvil menus), is a *virtual* anvil and follows `allow-virtual-anvils`. `Inventory#getLocation()` is
used, never the `InventoryView`. Erase mode (and any future anvil operation) must call the same gate.

Placement in the handlers, right after the world check:

- **Prepare** (`DisenchantEvent`, `ShatterEvent`): when refused and `hint-message` is off, return immediately, leaving
  the event untouched (vanilla result stays). With the hint on, the collection runs so the hint is only sent for a
  genuine operation (`sendSpecialAnvilHint` debounces to once per 5 s per player; cleared on quit).
- **Click** (`DisenchantClickEvent`, `ShatterClickEvent`): fails **closed**. When refused, collection still runs;
  if it is a genuine operation and slot 2 holds the plugin's `ENCHANTED_BOOK` (the gate closed between Prepare and the
  click), the event is cancelled and slot 2 cleared, inputs untouched. Anything else is vanilla and returns silently.

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

## Special anvil lifecycle (`anvils/`)

| Class                 | Role                                                                                                         |
|-----------------------|--------------------------------------------------------------------------------------------------------------|
| `SpecialAnvilStore`   | Chunk-PDC marks: key `disenchantment:special_anvils`, `LONG_ARRAY` of packed local positions `(x&15) \| (z&15)<<4 \| (y+2048)<<8` (y -2048..2047). `isMarked` validates the block is an anvil and prunes stale marks; `mark` does not validate. |
| `SpecialAnvilItem`    | `ANVIL` (any damage state) + item PDC `disenchantment:special_anvil` (BYTE 1) and configured name/lore. Identified by PDC only. |
| `SpecialAnvilListener`| Plain `@EventHandler` listener, registered in `enable()`/`reload()` regardless of mode (see below).          |
| `SpecialAnvilRecipe`  | `ShapedRecipe` `disenchantment:special_anvil`; `register()` removes then re-adds; `reregister()` (used by `reload()`) hands `register()` to `SchedulerUtils.runGlobal` on Folia, where the recipe manager belongs to the global region; `unregister()` on disable. `ANVIL` ingredient = MaterialChoice of all three states. |

Listener behaviour (all MONITOR + `ignoreCancelled` unless noted):

- `BlockPlaceEvent`: always clears a stale mark at the position, then marks if the item has the PDC **and** the player
  has `disenchantment.anvil.special`.
- `BlockBreakEvent`: unmarks. A break by a non-creative player with drops arms a 2 s pending-drop entry (position key)
  because the mark is already gone when `BlockDropItemEvent` fires.
- `BlockDropItemEvent` (HIGH): with a pending entry, swaps dropped anvil stacks for the special item of the same damage
  state (`drop-special-item`).
- `EntityChangeBlockEvent` with a `FallingBlock`: anvil to non-anvil = fall start, marked: unmark and tag the entity PDC
  `special_fall`; non-anvil to anvil = landing: clear any stale mark, mark if tagged. `EntityDropItemEvent` (HIGH):
  a tagged falling anvil that breaks drops the special item.
- `InventoryCloseEvent` on an anvil with a mark: prune if the block is gone, now and one tick later via
  `SchedulerUtils.runForLocationLater` (region scheduler on Folia). Using an anvil can destroy it with no event.
- `PlayerQuitEvent`: clears the hint debounce.

Placed anvils have blast resistance 1200 and `PushReaction.BLOCK`: explosions and pistons never move or break them,
so no handlers exist for them. Damage-state changes fire no event and need none: marks are position based.

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
3. Guard: `Config.isPluginEnabled()`, `Config.Disenchantment.isEnabled()`, world not in disabled list, then the [special anvil gate](#special-anvil-gate).
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
2. Guards: plugin/feature enabled, world not disabled, then the [special anvil gate](#special-anvil-gate) (fails closed).
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

## Erase Flow

`EraseListener` / `EraseClickListener` -> `EraseEvent` / `EraseClickEvent`, collectors
`EraseUtils::getErasableEnchantments` (both overloads), permission group `PermissionGroupType.ERASE_EVENT`, cost type
`AnvilEventType.ERASE`. Registered next to the other listeners in `Disenchantment.enable()` / `reload()` with
`Config.Erase.getPrepareEventPriority()` / `getClickEventPriority()`; it does **not** register `AnvilSessionListener`
(that is done once by `DisenchantListener`).

**Prepare (`EraseEvent`).** Guards as disenchant (plugin + `EraseUtils.isActive()`, world via the *disenchantment* world
restriction), collect erasable enchantments, maintenance/cooldown clear the result, permission, then
`e.setResult(EraseUtils.markPrepared(EraseUtils.buildResult(first, enchantments)))`. `markPrepared` tags the result with the
PDC byte `disenchantment:erase_result`; the click handler only treats a slot-2 item carrying the tag as its own (both for
the fail-closed clear and for the delivery), so a vanilla repair result is never cleared or swallowed even when the catalyst
is also a repair material. The delivered item is rebuilt from slot 0 and never carries the tag and `applyAnvilCostAndSchedule(..., AnvilEventType.ERASE)`.

**Click (`EraseClickEvent`) - manual-result pattern.** Check order matches `DisenchantClickEvent`: result slot -> collect
(empty = not ours) -> maintenance -> `result.getType() == first.getType()` -> unsafe click -> pending -> cooldown -> XP ->
permission -> `PreEraseEvent` -> economy. After the last check **nothing returns early**: the handler

1. rebuilds the item from slot 0 (`EraseUtils.buildResult`, not from the result slot),
2. `e.setCancelled(true)`,
3. `setItem(0, null)`, `setItem(1, <catalyst - 1>)` (or `null`), `setItem(2, null)` synchronously,
4. `p.setItemOnCursor(item)`, deducts XP (not in creative), records the cooldown,
5. fires `PostEraseEvent`, plays sound + `ParticleUtils.playErase`, `p.updateInventory()`.

Why cancel: the book flows only work because an enchanted book cannot stack, so vanilla's own pick-up is a no-op after
`setItemOnCursor`. An erase result is an ordinary item that may stack (custom-enchant plugins), where vanilla would merge a
second copy onto the cursor. Cancelling removes the dependence on vanilla `onTake`; the catalyst is decremented
synchronously, so there is no 2-tick window (`scheduleSecondItemRemoval` is not used). Refusals (unsafe click, cooldown,
XP, funds, Pre cancel) return before any mutation, so they can neither lose nor duplicate items.

---

## Shared settlement (`AnvilOperations`)

`events/AnvilOperations.java` is the single place that settles a disenchant, shatter or erase. The three click
handlers do their entry-specific checks (result slot, special anvil, maintenance, unsafe click, pending, cooldown, XP,
permission) and then call `AnvilOperations.settle(kind, p, first, second, result, enchantments, repairCost, effects, applier)`,
which runs, in this order and for every entry point: `Pre*` event (cancel -> `CANCELLED`), then **build every item** (disenchant/shatter:
strip the source, per-enchantment chance rolls, level restore #76, plain-book fallback, `PreviewLore.strip` on a copy; erase:
`EraseUtils.buildResult`) - adapters can throw here, and nothing has been charged or written yet - then **`takePayment`**
(economy `charge`, whose refusals message the player and return `CANCELLED`; then the XP is re-checked, because an economy
plugin can change levels while withdrawing, and taken; `clearBypassCost`), then the **`Applier`** (the only caller-specific
part: write the slots and hand over the result), the cooldown, the `Post*` event, sound and particles.

If the `Applier` throws, the XP and the money are given back (`refund`: levels restored, `AnvilEventGuards.refundEconomy` deposits;
a no-op in creative or with the economy off) and the exception propagates to the handler's `DiagnosticUtils.throwReport`.
`Outcome(status, delivered, newFirst, xpCost, economyCost)` reports the result;
`DONE`, `CANCELLED` (anvil handlers cancel the click), `IGNORED`.

`AnvilOperations.Kind` (`DISENCHANT`, `SHATTER`, `ERASE`) also owns the per-operation bindings that used to be duplicated in
each handler: the `EconomyConfig`, enabled/world checks, permission group, cost type; and `collect`, `buildBook`,
`economyCost`, `decrement`, `consumeCatalyst`. The Prepare handlers use the same `collect`/`buildBook`/`economyCost`.

## Bedrock menu flow

`events/BedrockOperation.java` (stateless; the menu in `guis/impl/BedrockAnvilGUI.java` owns the items).

- `evaluate(p, first, second, specialAnvilReady)` -> `Plan(kind, status, enchantments, result, xpCost, economyCost)`:
  `detectKind` (plain book + enchanted book = shatter, plain book + other = disenchant, erase catalyst = erase), then
  plugin/feature/world, special anvil (`specialAnvilReady`, = `BedrockAnvilGUI.specialAnvilHolds()`: the remembered anvil block), maintenance,
  cooldown (quiet), permission, `AnvilOperations.collect(..., true)`, the shatter subset through
  `ShatterEvent.selectForInputs` (same per-player cache as the anvil preview, so the previewed subset is the confirmed
  one), result, cost and `AnvilEventGuards.isTooExpensive` (40+ levels without `bypass-too-expensive`; not in creative).
- `confirm(p, first, second, specialAnvilReady, applier)`: `evaluate`, then maintenance -> cooldown (messages) -> XP
  (`refuseNotEnoughXp`) -> permission, `collect(..., false)`, and `AnvilOperations.settle` with `effects = null` (particles
  at the player). The second item is consumed through the `Applier` (`decrement`, or `consumeCatalyst` for erase).
- Stats, API events and PlaceholderAPI need nothing extra: they hang off the `Pre*`/`Post*` events `settle` fires.

`listeners/BedrockListener.java`: close and quit hooks (return the inputs), and the opt-in sneak-click trigger
(`PlayerInteractEvent` at HIGHEST with `ignoreCancelled`, so an earlier protection veto wins: `RIGHT_CLICK_BLOCK`, main hand, sneaking, anvil block, detected Bedrock player,
`disenchantment.command.anvil`, the clicked anvil is special; then the event is cancelled and `tryOpen` runs).
`utils/BedrockUtils.java` is the detector (Floodgate/Geyser by reflection, UUID heuristic, name prefix; never throws).

---

## Feedback hooks

- **Refusals.** `FeedbackUtils.refuse(p, message)` = debounced message (one per player per second, shared by all reasons)
  + `feedback.failure-sound`. `refuseNotEnoughXp(p, levels)` additionally honours `feedback.not-enough-xp-message`.
  Used by the three click handlers and by `AnvilEventGuards.isOnCooldown` / `isMaintenanceBlocked`. The sound is a vanilla
  key played with `Player#playSound(Location, String, float, float)` - never a `Sound` constant.
- **Success.** The click handlers call `ParticleUtils.playSuccess(p, anvilInventory)` (erase: `playErase`). Names are
  resolved with `Particle.class.getField(name)` (never `valueOf`/`values`), data-less particles only, cached per name list.
- **Preview lore.** `DisenchantEvent` / `ShatterEvent` call `PreviewLore.append(book, lines)`: the "Transfers" / "Cost"
  lines (when `feedback.preview-lore`) plus the "some enchantments may not transfer" warning (always, when a chance is
  below 1). The line count is stored in the book's PDC (`disenchantment:preview_lines`). Both click handlers call
  `PreviewLore.strip(result)` **in place** after the last abort point and before `setItemOnCursor` / the Post event, so the
  result slot cannot hand vanilla an annotated second copy. `strip` is idempotent and leaves foreign lore alone.

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
line and uses a **tick-based fallback**: after any click on a grindstone (and any `InventoryDragEvent` into it, which
changes the inputs without a click; registered only in this fallback, `ignoreCancelled`) it schedules (`SchedulerUtils.runForEntityLater`,
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
  cursor empty -> unsafe click (`isUnsafeResultClick`) -> cooldown -> XP -> permission -> `Pre*Event` -> build items -> economy + XP ->
  mutate. The result is **recomputed from slots 0/1** at click time; the item in slot 2 is only a "something is shown"
  check, so a stale result can never be delivered. All items are built before anything is charged or written
  (an adapter that throws costs nothing), then the economy and XP are charged (a failing slot write refunds both), slot 2 is cleared, slot 1 is consumed, the stripped source goes into slot 0 and the result onto the cursor.
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
`OperationType.GRIND_EXTRACT` / `GRIND_TRANSFER`: they have their own counters in `StatsCache` (`getTotalGrindExtracts/Transfers`, per-player `grindstones`, shown by `/disenchantment stats`, PAPI `%disenchantment_total_grindstones%` and `%disenchantment_player_grindstones%`), count toward the XP and money totals, and are not in the disenchant/shatter totals or `total_operations`.

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

### `PreEraseEvent` / `PostEraseEvent`

`PreEraseEvent(player, sourceItem, enchantments, catalyst)` is cancellable and fires after the XP check, before the
economy charge and before any mutation; removing entries from `getEnchantments()` keeps them on the item.
`PostEraseEvent(player, resultItem, erased, xpCost, economyCost)` is informational. `StatsListener` does not record
erase operations (the stats schema only knows `DISENCHANT` and `SHATTER`).

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
| `BedrockAnvilGUI` | `gui.onInventoryClick(e)` for a click in **either** half of the window (top holder check, before the clicked-holder switch) | none here; operation permissions are checked when confirming |

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
