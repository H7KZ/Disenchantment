<!-- generated-by: gsd-doc-writer -->
# Architecture

This document describes the technical design of the Disenchantment plugin. See [SETUP.md](SETUP.md) to get your environment ready, and [ADDING_NEW_VERSION.md](ADDING_NEW_VERSION.md) for the concrete steps to extend the NMS layer.

## System overview

Disenchantment is a Spigot/Paper/Folia plugin that intercepts vanilla anvil interactions and adds two new mechanics: **disenchanting** (removing all enchantments from an item onto a blank book) and **shattering** (splitting one enchantment off a multi-enchantment book). Both features hook into `PrepareAnvilEvent` to compute and preview a result, and `InventoryClickEvent` to execute the transfer when the player takes the result. Version-specific NMS operations (repair cost manipulation, enchantment registry access, skull textures) are abstracted behind a single `NMS` interface so the same core logic runs across every supported Minecraft version.

## Module dependency diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        dist/                                │
│        (maven-shade-plugin assembles final JAR)             │
└────────────────┬────────────────────────────────────────────┘
                 │ depends on all modules
    ┌────────────┼──────────────────────────────────┐
    │            │                                  │
    ▼            ▼                                  ▼
core/        v1_18_R1/   v1_20_R4/  v1_21_R1/  v1_21_R4/  v1_21_R5/
(shared)     └──────────────────────────────────────────────┘
                      each NMS module depends on core/
```

The `core/` module is a regular Maven dependency of every `v*` module. It provides the `NMS` interface, `NMSMapper`, and all plugin logic. Each `v*` module provides one `NMS_v*_R*.java` class and a `plugins/` package of third-party adapters. The `dist/` module has no source of its own — it exists solely to shade all modules into a single fat JAR.

## NMS abstraction layer

### NMS interface (`core/.../nms/NMS.java`)

All version-specific operations are declared here as `default` methods that return safe no-op values. Version modules override only the methods they need:

| Method | Purpose |
|---|---|
| `canItemBeEnchanted(ItemStack)` | Determines whether an item can hold enchantments (used to validate slot 0 in the anvil) |
| `getRegisteredEnchantments()` | Returns all enchantments known to this MC version, including third-party ones |
| `getMaterials()` | Returns all `Material` values for this version (registry differs between versions) |
| `getSupportedPlugins()` | Returns the list of third-party plugin adapter instances for this NMS version |
| `getRepairCost(AnvilInventory, InventoryView)` | Reads the current repair cost from an anvil |
| `setItemRepairCost(ItemStack, int)` | Writes the repair cost to an item's NBT/meta data |
| `setAnvilRepairCost(AnvilInventory, InventoryView, int)` | Updates the anvil GUI's displayed repair cost |
| `setTexture(HeadBuilder, String)` | Applies a Base64 skin texture to a player head item |

### NMSMapper (`core/.../nms/NMSMapper.java`)

`NMSMapper.setup()` is called once during `onEnable()`. It:

1. Calls `MinecraftVersion.getServerVersion()` to get the detected version enum entry.
2. Reads the `nmsVersion` string from that entry (e.g. `"v1_21_R5"`).
3. Reflectively loads `com.jankominek.disenchantment.nms.NMS_<nmsVersion>` via `Class.forName`.
4. Instantiates it with `getDeclaredConstructor().newInstance()` and casts to `NMS`.
5. Returns `null` if the version is unsupported, causing the plugin to disable itself.

`NMSMapper.hasNMS()` is a lighter check used during diagnostics and testing to verify the class is on the classpath.

### MinecraftVersion enum (`core/.../nms/MinecraftVersion.java`)

The enum maps every known Minecraft version string to an NMS module identifier. It is ordered most-specific first so that substring matching on the server version string (e.g. `"git-Paper-406 (MC: 1.21.4)"`) finds `1.21.4` before it would accidentally match `1.21`.

Each entry carries four fields:

```java
MINECRAFT_1_21_4((byte) 20, "1_21_4", "1.21.4", "v1_21_R1"),
//               ordinal    underlined  dotted    nmsModule
```

The `ordinal` (byte) enables `currentVersionOlderThan` / `currentVersionNewerThan` comparisons used in features that need to vary behaviour by MC version without adding another NMS module.

**Version mapping table:**

| NMS Module | Minecraft Versions | Java |
|---|---|---|
| `v1_18_R1` | 1.18, 1.18.1, 1.18.2, 1.19 – 1.19.4, 1.20 – 1.20.4 | 17 |
| `v1_20_R4` | 1.20.5, 1.20.6 | 21 |
| `v1_21_R1` | 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4 | 21 |
| `v1_21_R4` | 1.21.5, 1.21.6, 1.21.7 | 21 |
| `v1_21_R5` | 1.21.8 – 1.21.11+, 26.x.x, **LATEST** | 21 |

**LATEST fallback:** Any version not matched by a named entry is detected by the `init()` fallback logic. If the MC minor version is >= 21, or the major version is >= 2, `LATEST` is returned. `LATEST` maps to `v1_21_R5`, so future patch releases and the new 26.x.x numbering scheme work out of the box without a code change.

**`INCOMPATIBLE`:** Returned for versions below 1.18. `NMSMapper.setup()` returns `null` when the nmsVersion field is `null` (as it is for `INCOMPATIBLE`), and the plugin disables itself.

## Disenchanting — end-to-end flow

### Phase 1: Preview (`PrepareAnvilEvent` → `DisenchantEvent`)

1. `DisenchantListener` (an `EventExecutor`) receives `PrepareAnvilEvent` at the configured priority.
2. `DisenchantEvent.onEvent` delegates to the private `handler` method.
3. Guards check: plugin enabled, disenchantment feature enabled, world not disabled.
4. `AnvilEventGuards.collectEnchantments` inspects slot 0 (the item) and slot 1 (the book). It calls `EventUtils.Disenchantment.getDisenchantedEnchantments` to determine which enchantments are eligible, filtering by disabled-enchantments config and per-world rules.
5. If the enchantment list is empty, the handler exits — no result is set, allowing vanilla behaviour.
6. A permission check runs (`disenchantment.anvil.disenchant`).
7. A new `ENCHANTED_BOOK` `ItemStack` is constructed and each eligible `IPluginEnchantment` is applied to it via `addToBook(book)`.
8. `e.setResult(book)` sets the anvil output slot.
9. `AnvilEventGuards.applyAnvilCostAndSchedule` computes the XP cost using `AnvilCostUtils.countAnvilCost` (formula: `base + sum(level_n * multiply * position_multiplier)`) and writes it to the anvil via `nms.setAnvilRepairCost`.
10. If economy is configured and enabled, an action bar message shows the money cost.

### Phase 2: Execution (`InventoryClickEvent` → `DisenchantClickEvent`)

1. `DisenchantClickListener` receives `InventoryClickEvent` at the configured priority.
2. Guards check: same enabled/world guards, then `AnvilEventGuards.isAnvilResultSlotClick` verifies the click is on slot 2 (the output) of an anvil.
3. The result item must be an `ENCHANTED_BOOK` (set by phase 1).
4. XP is checked: `p.getLevel() >= repairCost`. If insufficient, the event is cancelled.
5. Permission is checked.
6. Economy check: if Vault economy is enabled, `AnvilEventGuards.processEconomy` charges the player. If the plugin is not available or funds are insufficient, the event is cancelled with a message.
7. A cancellable `PreDisenchantEvent` (custom Bukkit event) is fired. Third-party plugins can cancel the disenchant here.
8. Enchantments are removed from the source item. If third-party adapters are active, each adapter's `removeFromItem` is called; otherwise `EnchantmentUtils.removeEnchantments` removes vanilla enchantments.
9. If `disenchantment.anvil.repair.reset` is enabled, the item's repair cost is set to 0 via `nms.setItemRepairCost`.
10. The source item is updated in slot 0. The blank book in slot 1 is removed (scheduled for next tick to avoid inventory sync issues).
11. XP is deducted: `p.setLevel(p.getLevel() - repairCost)` (skipped in Creative mode).
12. The enchanted book is placed on the player's cursor.
13. A `PostDisenchantEvent` is fired with the resulting book and modified source item.
14. If the anvil sound is enabled, `Sound.BLOCK_ANVIL_USE` is played.

## Shattering — end-to-end flow

Shattering follows the same two-phase pattern via `ShatterListener` / `ShatterEvent` (PrepareAnvilEvent) and `ShatterClickListener` / `ShatterClickEvent` (InventoryClickEvent).

The key difference is input validation and result construction:
- Slot 0 must be an `ENCHANTED_BOOK` (not an arbitrary item).
- Slot 1 must be a regular `BOOK`.
- `EventUtils.Shatterment.getShatteredEnchantments` selects a subset of enchantments to split off. The number of enchantments taken per shatter is controlled by `shatterment.split-count` (default `1`). Enchantments are shuffled and the first `split-count` are taken.
- The result book gets only the selected enchantments; the source book retains the rest.
- On click, the source book's selected enchantments are removed, and the result is delivered to the cursor.
- Custom events `PreShatterEvent` and `PostShatterEvent` are fired analogously.

## Static globals pattern

The main class `Disenchantment` exposes five static fields that are the primary access point throughout the codebase:

```java
public static Disenchantment plugin;   // JavaPlugin instance
public static NMS nms;                 // active NMS implementation
public static FileConfiguration config;       // plugin config.yml
public static FileConfiguration localeConfig; // active locale file
public static Logger logger;
```

These are initialised in `onEnable()` before any other subsystem, and accessed via static imports:

```java
import static com.jankominek.disenchantment.Disenchantment.nms;
import static com.jankominek.disenchantment.Disenchantment.config;
// etc.
```

This avoids passing the plugin instance through every layer and keeps call sites concise. The trade-off is that tests must reset these fields after each test (see [TESTING.md](TESTING.md)).

## Listener registration via EventExecutor

Listeners are not registered via the standard `@EventHandler` annotation. Instead, the `EventExecutor` pattern is used so that the `EventPriority` can be read from config at plugin startup and applied at registration time:

```java
// In DisenchantListener constructor:
getServer().getPluginManager().registerEvent(
        PrepareAnvilEvent.class,
        listener,       // anonymous Listener placeholder
        priority,       // read from Config.EventPriorities.getDisenchantEvent()
        this,           // this class IS the EventExecutor
        plugin,
        false
);

// EventExecutor.execute() delegates to the static event class:
@Override
public void execute(Listener l, Event e) throws EventException {
    DisenchantEvent.onEvent(e);
}
```

This pattern is used for all four anvil event listeners: `DisenchantListener`, `DisenchantClickListener`, `ShatterListener`, `ShatterClickListener`.

## Economy hook via ServerLoadEvent

Vault economy is hooked in a `ServerLoadEvent` handler, not in `onEnable()`:

```java
getServer().getPluginManager().registerEvents(new Listener() {
    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        EconomyUtils.setup();
    }
}, plugin);
```

**Why:** VaultUnlocked registers its vault2→vault1 bridge service *after* all plugins have finished `onEnable()`, as part of the server's Done startup phase. Hooking Vault in `onEnable()` (or even on a one-tick delay) would find no economy service registered yet. `ServerLoadEvent` fires once after the server is fully done loading, by which point VaultUnlocked's bridge is present.

## Config migration system

The plugin's config schema is versioned. The current version is tracked in `config.yml` under the `migration` key (currently `9`). On startup, `ConfigUtils.setupConfig()` compares the user's existing config version against the bundled default, then calls `ConfigMigrations.apply()`.

`ConfigMigrations` holds a `Map<Integer, IConfigMigration>` with one handler per migration step. Each step loads a template from `core/src/main/resources/migrations/<N>.yml` and calls the `IConfigMigration.migrate(oldConfig, template)` method to copy old values into the new schema. Unrecognised step numbers log a warning and are skipped. This means config upgrades are additive and non-destructive.

To add a new migration step: create `Migration<N>.java` implementing `IConfigMigration`, create `migrations/<N>.yml` template, and register it in `ConfigMigrations.migrations`.

## Plugin adapter system

Third-party enchantment plugin support is provided per-NMS-module via the `ISupportedPlugin` / `IPluginEnchantment` interfaces.

### ISupportedPlugin

```java
public interface ISupportedPlugin {
    String getName();                                          // plugin name as registered on server
    List<IPluginEnchantment> getItemEnchantments(ItemStack);  // reads enchantments from an item
    default List<IPluginEnchantment> getItemEnchantments(ItemStack, World) { ... }
    default void activate() {}                                // called when the adapter is activated
}
```

Each NMS module's `NMS_v*_R*.getSupportedPlugins()` returns the full list of adapters that module can support (regardless of whether those plugins are installed).

### SupportedPluginManager

At startup, `SupportedPluginManager.activatePlugins(loadedPluginNames)` is called with the names of all currently loaded server plugins. It cross-references these against the list from `nms.getSupportedPlugins()` and activates any that match. Only activated adapters are consulted at runtime.

### Per-module adapter coverage

| Adapter | v1_18_R1 | v1_20_R4 | v1_21_R1 | v1_21_R4 | v1_21_R5 |
|---|---|---|---|---|---|
| AdvancedEnchantments | yes | yes | yes | yes | yes |
| EcoEnchants | yes | yes | yes | yes | yes |
| EnchantsSquared | yes | yes | yes | yes | yes |
| UberEnchant | yes | yes | yes | yes | yes |
| ExcellentEnchants | — | — | yes | yes | yes |
| Vane | — | — | yes | yes | yes |
| Zenchantments | — | — | yes | yes | yes |

Third-party JARs are stored in each module's `libs/` directory and declared as `system`-scope Maven dependencies so they are on the compile classpath without being published to Maven Central.

## Core package layout

```
core/src/main/java/com/jankominek/disenchantment/
├── Disenchantment.java              Main plugin class (extends JavaPlugin)
├── commands/
│   ├── CommandBuilder.java          Fluent builder for command registration
│   ├── CommandCompleter.java        Tab completion router
│   ├── CommandRegister.java         Command dispatch router
│   └── impl/                        Individual command handlers
│       ├── HelpCommand.java
│       ├── ToggleCommand.java
│       ├── StatusCommand.java
│       ├── GUICommand.java
│       ├── DiagnosticCommand.java
│       └── ...
├── config/
│   ├── Config.java                  Typed config access (inner classes per feature)
│   ├── ConfigUtils.java             Config file setup, migration orchestration
│   ├── I18n.java                    Locale string access
│   └── migration/
│       ├── IConfigMigration.java    Migration step interface
│       ├── ConfigMigrations.java    Step registry and orchestrator
│       └── steps/
│           ├── Migration1.java – Migration9.java
├── events/
│   ├── DisenchantEvent.java         PrepareAnvilEvent handler for disenchanting
│   ├── DisenchantClickEvent.java    InventoryClickEvent handler for disenchanting
│   ├── ShatterEvent.java            PrepareAnvilEvent handler for shattering
│   ├── ShatterClickEvent.java       InventoryClickEvent handler for shattering
│   ├── GUIClickEvent.java           GUI interaction handler
│   └── api/
│       ├── PreDisenchantEvent.java  Cancellable event before enchantments transfer
│       ├── PostDisenchantEvent.java Event after enchantments transfer
│       ├── PreShatterEvent.java     Cancellable event before book split
│       └── PostShatterEvent.java    Event after book split
├── guis/
│   ├── InventoryBuilder.java
│   ├── ItemBuilder.java
│   ├── HeadBuilder.java
│   └── impl/                        GUI screen implementations
│       ├── NavigationGUI.java
│       ├── WorldsGUI.java
│       ├── EnchantmentsGUI.java
│       └── ...
├── listeners/
│   ├── DisenchantListener.java      EventExecutor wrapping DisenchantEvent
│   ├── DisenchantClickListener.java EventExecutor wrapping DisenchantClickEvent
│   ├── ShatterListener.java         EventExecutor wrapping ShatterEvent
│   └── ShatterClickListener.java    EventExecutor wrapping ShatterClickEvent
├── nms/
│   ├── NMS.java                     Version-agnostic interface
│   ├── NMSMapper.java               Reflective loader
│   └── MinecraftVersion.java        Version → NMS module enum
├── plugins/
│   ├── ISupportedPlugin.java        Third-party adapter interface
│   ├── IPluginEnchantment.java      Common enchantment representation
│   ├── SupportedPluginManager.java  Adapter activation/deactivation
│   └── placeholderapi/
│       └── DisenchantmentPlaceholderExpansion.java
├── types/
│   ├── AnvilEventType.java          DISENCHANTMENT / SHATTERMENT enum
│   ├── AnvilFeature.java
│   ├── ConfigKeys.java              Typed config key constants
│   ├── EnchantmentStateType.java    ALLOWED / DISABLED / FORCED enum
│   ├── I18nKeys.java                Locale string key constants
│   ├── LogLevelType.java            NONE / INFO / DEBUG enum
│   └── PermissionGroupType.java
└── utils/
    ├── AnvilCostUtils.java          XP cost calculation and NMS delegation
    ├── AnvilEventGuards.java        Shared validation logic for both anvil event handlers
    ├── BStatsMetrics.java           bStats integration
    ├── ConfigUtils.java             Config file setup helpers
    ├── DiagnosticUtils.java         Structured debug logging and crash reports
    ├── EconomyUtils.java            Vault economy hook and charge helpers
    ├── EnchantmentUtils.java        Enchantment manipulation helpers
    ├── EventUtils.java              Enchantment eligibility logic for disenchant/shatter
    ├── MapUtils.java
    ├── MaterialUtils.java
    ├── PrecisionUtils.java          Rounding for cost formula
    ├── SchedulerUtils.java          Folia-compatible task scheduler abstraction
    └── UpdateChecker.java           SpigotMC update check
```
