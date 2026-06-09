<!-- generated-by: gsd-doc-writer -->
# GUI System

`core/src/main/java/com/jankominek/disenchantment/guis/`

The GUI framework is a lightweight chest-inventory system built on top of Bukkit's `Inventory` API. It provides builders for items and inventories, a component model for layout, and screen classes that implement `InventoryHolder`.

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

| Method | Description |
|--------|-------------|
| `setDisplayName(String)` | Sets display name using `LegacyComponentSerializer` (supports `§` codes) |
| `setLore(List<String>)` / `setLore(String)` | Sets lore lines |
| `setGlow(boolean)` | Adds/removes KNOCKBACK+HIDE_ENCHANTS for visual glow |
| `setAmount(int)` | Sets stack size |
| `setUnbreakable(boolean)` | Sets unbreakable NBT flag |
| `addEnchantment(Enchantment, int)` | Adds enchantment ignoring restrictions |
| `addItemFlag(ItemFlag)` / `addAllFlags()` | Hides item attributes |
| `setHead(String owner)` | Sets skull owner for `PLAYER_HEAD` items |
| `setColor(Color)` | Sets leather armor dye color |
| `build()` | Returns the final `ItemStack` |

`HeadBuilder` extends `ItemBuilder` with an additional `setTexture(String base64)` method that delegates to `Disenchantment.nms.setTexture(this, texture)` for version-specific skull texture application.

---

### `GUIItem`

`guis/GUIItem.java` — an item bound to a slot with a click handler.

```java
new GUIItem(slot, itemStack, event -> { /* handler */ });
```

Contains a 100ms debounce: if a click arrives within 100ms of the previous click, the event is cancelled without calling the handler. This prevents double-click issues from client lag.

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

`guis/GUIComponent.java` — factory for `ItemStack` items used as navigation buttons in GUIs. Reads localized strings from `I18n.GUI.*`. Not documented in detail here, but it produces pre-built items like `GUIComponent.Navigation.Plugin.enabled()`, `GUIComponent.Navigation.worlds()`, etc. Each method returns an `ItemStack` ready to pass to `GUIItem`.

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

All screens implement `InventoryHolder`. The inventory is created in the constructor and stored; `getInventory()` returns it. Bukkit associates the holder with the inventory, which `GUIClickEvent` uses to route clicks.

Each screen builds a `GUIItem[]` array (using `ArrayUtils.addAll` to combine border + content items), populates the inventory via `InventoryBuilder.fillItems(inventory, items)`, and exposes `onInventoryClick(InventoryClickEvent)` which iterates `items` and calls `item.onClick(event)` on the matching slot.

### `NavigationGUI`

27-slot main hub. Opened by `/disenchantment gui`.

| Slot | Item | Action |
|------|------|--------|
| 10 | Plugin enabled/disabled toggle | Calls `Disenchantment.onToggle()` + `Config.setPluginEnabled()` |
| 11 | Worlds | Opens `WorldsGUI(0)` |
| 12 | Repair | Left-click → `RepairGUI(DISENCHANTMENT)`, Right-click → `RepairGUI(SHATTERMENT)` |
| 13 | Enchantments | Opens `EnchantmentsGUI(0)` |
| 14 | Materials | Opens `MaterialsGUI(0)` |
| 15 | Sound | Left-click → `SoundGUI(DISENCHANTMENT)`, Right-click → `SoundGUI(SHATTERMENT)` |
| 16 | Spigot link | Sends SpigotMC URL to player and closes inventory |
| 22 | Economy | Left-click → `EconomyGUI(DISENCHANTMENT)`, Right-click → `EconomyGUI(SHATTERMENT)` |
| Border | Glass panes | No action |

### `WorldsGUI`

Paginated list of worlds. Takes `int page` in constructor. Shows each world as a player head with disenchantment/shatterment toggle lore. Clicking toggles the world's disabled state for both features simultaneously.

### `EnchantmentsGUI`

Paginated list of enchantments. Shows each enchantment as a book with current state (ENABLE/KEEP/DELETE/DISABLE) for both disenchantment and shatterment. Clicking cycles through states.

### `MaterialsGUI`

Paginated list of materials. Shows each material as its corresponding item with enabled/disabled state. Clicking toggles whether the material is disabled for disenchanting.

### `RepairGUI`

Takes `AnvilFeature feature` (DISENCHANTMENT or SHATTERMENT). Shows four toggleable/adjustable items:
- Cost enabled toggle
- Reset enabled toggle  
- Base cost value (click to change)
- Multiplier value (click to change)

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
5. Add a routing branch in `GUIClickEvent.handler()`:
   ```java
   } else if (clickedHolder instanceof MyGUI myGUI) {
       if (!PermissionGroupType.MY_PERMISSION.hasPermission(p)) return;
       myGUI.onInventoryClick(e);
   }
   ```
6. Add a navigation item in `NavigationGUI` that opens your screen with `event.getWhoClicked().openInventory(new MyGUI().getInventory())`.
7. Add locale strings to `I18nKeys` and locale files.
