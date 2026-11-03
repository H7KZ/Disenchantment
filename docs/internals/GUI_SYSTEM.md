<!-- generated-by: gsd-doc-writer -->

# GUI System

`core/src/main/java/com/jankominek/disenchantment/guis/`

The GUI framework is a lightweight chest-inventory system built on top of Bukkit's `Inventory` API. It provides builders
for items and inventories, a component model for layout, and screen classes that implement `InventoryHolder`.

---

## Core Components

### `ItemBuilder`

`guis/ItemBuilder.java` — fluent builder for `ItemStack`.

```java
new ItemBuilder(Material.EMERALD)
    .setDisplayName("&aEnabled")
    .setLore(List.of("&7Click to disable"))
    .setGlow(true)
    .addAllFlags()
    .build();
```

Key methods:

| Method                                      | Description                                                              |
|---------------------------------------------|--------------------------------------------------------------------------|
| `setDisplayName(String)`                    | Sets display name using `LegacyComponentSerializer` (supports `§` codes) |
| `setLore(List<String>)` / `setLore(String)` | Sets lore lines                                                          |
| `setGlow(boolean)`                          | Adds/removes KNOCKBACK+HIDE_ENCHANTS for visual glow                     |
| `setAmount(int)`                            | Sets stack size                                                          |
| `setUnbreakable(boolean)`                   | Sets unbreakable NBT flag                                                |
| `addEnchantment(Enchantment, int)`          | Adds enchantment ignoring restrictions                                   |
| `addItemFlag(ItemFlag)` / `addAllFlags()`   | Hides item attributes                                                    |
| `setHead(String owner)`                     | Sets skull owner for `PLAYER_HEAD` items                                 |
| `setColor(Color)`                           | Sets leather armor dye color                                             |
| `build()`                                   | Returns the final `ItemStack`                                            |

`HeadBuilder` extends `ItemBuilder` with an additional `setTexture(String base64)` method that delegates to
`Disenchantment.nms.setTexture(this, texture)` for version-specific skull texture application.

---

### `GUIItem`

`guis/GUIItem.java` — an item bound to a slot with a click handler.

```java
new GUIItem(slot, itemStack, event -> { /* handler */ });
```

Contains a 100ms debounce: if a click arrives within 100ms of the previous click, the event is cancelled without calling
the handler. This prevents double-click issues from client lag.

Fields: `int slot`, `ItemStack item`, `IOnClickEvent onClick`, `long lastClick`.

---

### `IOnClickEvent`

```java
@FunctionalInterface
public interface IOnClickEvent {
    void onClick(InventoryClickEvent event);
}
```

Implemented as a lambda in each `GUIItem` constructor call.

---

### `InventoryBuilder`

`guis/InventoryBuilder.java` — places `GUIItem` array into an `Inventory`.

```java
InventoryBuilder.fillItems(inventory, items);
```

Iterates the `GUIItem[]` array and calls `inventory.setItem(item.getSlot(), item.getItem())` for each.

---

### `GUIComponent`

`guis/GUIComponent.java` — factory for `ItemStack` items used as navigation buttons in GUIs. Reads localized strings
from `I18n.GUI.*`. Not documented in detail here, but it produces pre-built items like
`GUIComponent.Navigation.Plugin.enabled()`, `GUIComponent.Navigation.worlds()`, etc. Each method returns an `ItemStack`
ready to pass to `GUIItem`.

---

### `GUIBorderComponent`

`guis/GUIBorderComponent.java` — generates border items for 9×3 (27-slot) and 9×6 (54-slot) inventories.

```java
GUIBorderComponent.border9x3()  // returns GUIItem[] for all border slots in a 27-slot inventory
GUIBorderComponent.border9x6()  // returns GUIItem[] for all border slots in a 54-slot inventory
```

Border items are typically gray stained glass panes with empty click handlers.

---

## GUI Screens (`guis/impl/`)

All screens implement `InventoryHolder`. The inventory is created in the constructor and stored; `getInventory()`
returns it. Bukkit associates the holder with the inventory, which `GUIClickEvent` uses to route clicks.

**Click/drag hardening**: `GUIClickEvent` cancels *every* `InventoryClickEvent` and `InventoryDragEvent` whose view's top
inventory holder is one of these screens (shift-click from the player inventory, empty slots, double-click
collect-to-cursor, number-key swaps, outside clicks, drags, permission-denied clicks) **before** dispatching clicks on the
GUI inventory to `onInventoryClick`. Item handlers therefore don't need to guard against item movement, though they
still call `event.setCancelled(true)` by convention. A new screen must be added to `GUIClickEvent.isPluginGui()`.

Each screen builds a `GUIItem[]` array (using `ArrayUtils.addAll` to combine border + content items), populates the
inventory via `InventoryBuilder.fillItems(inventory, items)`, and exposes `onInventoryClick(InventoryClickEvent)` which
iterates `items` and calls `item.onClick(event)` on the matching slot.

### `NavigationGUI`

27-slot main hub. Opened by `/disenchantment gui`.

| Slot   | Item                           | Action                                                                             |
|--------|--------------------------------|------------------------------------------------------------------------------------|
| 10     | Plugin enabled/disabled toggle | Calls `Disenchantment.onToggle()` + `Config.setPluginEnabled()`                    |
| 11     | Worlds                         | Opens `WorldsGUI(0)`                                                               |
| 12     | Repair                         | Left-click → `RepairGUI(DISENCHANTMENT)`, Right-click → `RepairGUI(SHATTERMENT)`   |
| 13     | Enchantments                   | Opens `EnchantmentsGUI(0)`                                                         |
| 14     | Materials                      | Opens `MaterialsGUI(0)`                                                            |
| 15     | Sound                          | Left-click → `SoundGUI(DISENCHANTMENT)`, Right-click → `SoundGUI(SHATTERMENT)`     |
| 16     | Spigot link                    | Sends SpigotMC URL to player and closes inventory                                  |
| 22     | Economy                        | Left-click → `EconomyGUI(DISENCHANTMENT)`, Right-click → `EconomyGUI(SHATTERMENT)` |
| Border | Glass panes                    | No action                                                                          |

### `WorldsGUI`

Paginated list of worlds. Takes `int page` in constructor. Shows each world as a player head with
disenchantment/shatterment toggle lore. Clicking toggles the world's disabled state for both features simultaneously.

### `EnchantmentsGUI`

Paginated list of enchantments. Shows each enchantment as a book with current state (ENABLE/KEEP/DELETE/DISABLE) for
both disenchantment and shatterment. Clicking cycles through states.

### `MaterialsGUI`

Paginated list of materials. Shows each material as its corresponding item with enabled/disabled state. Clicking toggles
whether the material is disabled for disenchanting.

### `RepairGUI`

Takes `AnvilFeature feature` (DISENCHANTMENT or SHATTERMENT). Shows five toggleable/adjustable items:

- Cost enabled toggle
- Reset enabled toggle
- Base cost value (click to change)
- Multiplier value (click to change)
- Max cost cap value (slot 16, click to change; left-click increments, right-click decrements down to `-1`, displayed as
  "No cap"/"Unlimited")

### `SoundGUI`

Takes `AnvilFeature feature`. Shows three items:

- Sound enabled toggle
- Volume (click to change)
- Pitch (click to change)

### `EconomyGUI`

Takes `AnvilFeature feature`. Shows four items:

- Economy enabled toggle
- Cost value (click to change)
- Show-cost toggle (action bar display)
- Charge-message toggle (chat confirmation)

### `BedrockAnvilGUI`

`guis/impl/BedrockAnvilGUI.java` - the Bedrock fallback (`/disenchantment anvil`, optional sneak-click). It is the one
screen that **holds real player items**, so it differs from the others in three ways.

**Layout (27 slots).** `10` item, `12` second item (plain book / erase catalyst), `13` mode label (anvil icon, name =
detected mode, lore = status), `14` result preview, `16` Confirm, `25` Cancel; `19`/`21`/`23` caption papers; everything
else border panes. Slots `10` and `12` are the only slots that ever contain real items; empty means `null`.

**Item movement is explicit** because `GUIClickEvent` cancels every click and drag. For this holder the router skips
the clicked-holder switch and calls `onInventoryClick(e)` for a click in *either* half of the window:

- click in the player's own inventory (`rawSlot >= 27`): one item moves into the matching input slot - `BOOK` or an erase
  catalyst into slot 12 (merging with a similar stack up to its max), anything else into slot 10 (an occupied slot is
  swapped, the old stack returned). The player slot is decremented *before* the menu slot is written, in the same call.
- click on slot 10/12: the content is taken out of the menu first, then handed back.
- only `LEFT`, `RIGHT`, `SHIFT_LEFT`, `SHIFT_RIGHT` do anything; all other click types stay cancelled and inert.
- one item per tap is a deliberate choice: an operation consumes exactly one book/catalyst, so extra items never leave the
  inventory and are never exposed to the close/crash paths.

**No loss, no duplication.** `finish()` is the single exit and is idempotent: `returnInputs()` empties both slots *before*
giving anything (`giveOrDrop`: `addItem`, leftovers `dropItem` at the feet, and the whole stack is dropped if `addItem`
throws), then unregisters the menu and clears the cached shatter selection. It is called from `BedrockListener`
(`InventoryCloseEvent`, `PlayerQuitEvent` at LOWEST so it runs before the data save; a kick ends in a quit),
from the Cancel button, when a second menu is opened for the same player, and from `closeAll()` in
`Disenchantment#disable()` (Bukkit does not close inventories when a plugin goes away). A `busy` flag blocks re-entrant
confirms (a `Pre*` listener clicking again) and `CONFIRM_DEBOUNCE_MS` (500 ms, overridable with `setClock`) swallows the
second tap of a double tap after a successful confirm. The result slot, mode label and buttons are display copies and are
never given to anyone.

**Operations.** `refresh()` calls `BedrockOperation.evaluate` (the PrepareAnvilEvent equivalent: world, special anvil
proximity, maintenance, cooldown, permission, collection, shatter subset via `ShatterEvent.selectForInputs`, result, cost,
too-expensive limit) and redraws slots 13/14/16. Confirm calls `BedrockOperation.confirm`, which re-evaluates, applies the
click-time guards in the anvil handlers' order and delegates to `AnvilOperations.settle`; the menu only supplies the
`Applier` that writes its slots and gives the result to the player. See [EVENTS.md](EVENTS.md#bedrock-menu-flow).

`tryOpen(player, clickedAnvil)` is the one gate for the command and the sneak-click trigger: plugin and
`bedrock.fallback-enabled` on, `BedrockUtils.isBedrockPlayer` (or `allow-java-players`), and - unless
`special-anvils.mode` is `ALL` - `AnvilEventGuards.isSpecialAnvilNearby(p, 5)` (command) or `isSpecialAnvilBlock(block)`
(sneak-click). Text lives under `messages.bedrock-*` in the locale files.

---

## How to Add a New GUI Screen

1. Create `MyGUI.java` in `guis/impl/`, implementing `InventoryHolder`.
2. Build your item array:
   ```java
   private final GUIItem[] items = ArrayUtils.addAll(
       GUIBorderComponent.border9x3(),
       new GUIItem(13, myItemStack, event -> {
           event.setCancelled(true);
           // handle click
       })
   );
   ```
3. In the constructor, create the inventory and fill it:
   ```java
   Inventory inv = Bukkit.createInventory(this, 27,
       LegacyComponentSerializer.legacySection().deserialize(title));
   this.inventory = InventoryBuilder.fillItems(inv, items);
   ```
4. Implement `getInventory()` and `onInventoryClick(InventoryClickEvent)`.
5. Add the class to `GUIClickEvent.isPluginGui()` (so its clicks/drags are cancelled) and a routing branch in
   `GUIClickEvent.handler()`:
   ```java
   } else if (clickedHolder instanceof MyGUI myGUI) {
       if (!PermissionGroupType.MY_PERMISSION.hasPermission(p)) return;
       myGUI.onInventoryClick(e);
   }
   ```
6. Add a navigation item in `NavigationGUI` that opens your screen with
   `event.getWhoClicked().openInventory(new MyGUI().getInventory())`.
7. Add locale strings to `I18nKeys` and locale files.
