# Disenchantment - Minecraft Plugin

A Spigot/Paper/Folia Minecraft plugin that adds disenchanting and book-splitting mechanics to the vanilla anvil. Players can remove enchantments from items onto books, or split multi-enchantment books into individual ones.

## Project Overview

- **Group ID**: `com.jankominek`
- **Version**: managed via `${revision}` property in root `pom.xml` (currently `6.3.4`)
- **Java**: 17 (core/v1_18_R1), 21 (v1_20_R4, v1_21_R1, v1_21_R4, v1_21_R5)
- **API**: Spigot API 1.18+, Folia API support
- **Author**: Jan Kominek (H7KZ)
- **Entry point**: `com.jankominek.disenchantment.Disenchantment` (registered in `core/src/main/resources/plugin.yml`)

## Build

```bash
mvn clean package
```

**Prerequisites** (see CONTRIBUTING.md):
- Maven
- JDK 21+
- Spigot BuildTools with versions 1.21, 1.20.5, and 1.18 installed
- Output JAR lands in `target/Disenchantment-<version>.jar`

## Module Structure

Multi-module Maven project. Each NMS module provides version-specific implementations.

| Module | Artifact | Purpose |
|---|---|---|
| `core/` | `disenchantment-core` | Shared logic: plugin main class, config, commands, events, listeners, GUIs, NMS interface, plugin adapter interface |
| `v1_18_R1/` | `disenchantment-v1_18_R1` | NMS for Minecraft 1.18 - 1.20.4 (uses NBT editing via `nbt/` package) |
| `v1_20_R4/` | `disenchantment-v1_20_R4` | NMS for Minecraft 1.20.5 - 1.20.6 (uses NBT editing via `nbt/` package) |
| `v1_21_R1/` | `disenchantment-v1_21_R1` | NMS for Minecraft 1.21 - 1.21.4 (no NBT package, uses Bukkit API directly) |
| `v1_21_R4/` | `disenchantment-v1_21_R4` | NMS for Minecraft 1.21.5 - 1.21.7 |
| `v1_21_R5/` | `disenchantment-v1_21_R5` | NMS for Minecraft 1.21.8 - 1.21.9 (and future via LATEST fallback) |
| `dist/` | `disenchantment-dist` | Shading module - assembles all modules into the final plugin JAR using maven-shade-plugin |

## Architecture

### NMS (Net Minecraft Server) Abstraction

- **`core/.../nms/NMS.java`** - Interface with default methods for version-specific operations (enchantment checks, registry access, repair cost, skull textures)
- **`core/.../nms/NMSMapper.java`** - Reflectively loads `NMS_<version>` class at runtime based on detected server version
- **`core/.../nms/MinecraftVersion.java`** - Enum mapping every supported MC version to its NMS module. Unknown versions >= 1.21 fall back to `LATEST` (v1_21_R5)
- Each `v*` module provides `NMS_v*_R*.java` implementing the `NMS` interface

### Custom Enchantment Plugin Support

Each NMS module contains adapters in `plugins/` for third-party enchantment plugins:

- **AdvancedEnchantments** - all versions
- **EcoEnchants** - all versions (requires patched build, see FAQ)
- **EnchantsSquared** - all versions
- **UberEnchant** - all versions
- **ExcellentEnchants** - v1_21_R1, v1_21_R4, v1_21_R5 only

Adapters implement `ISupportedPlugin` interface. They are instantiated in each `NMS_v*` class's `getSupportedPlugins()` method. `SupportedPluginManager` activates only those whose server-side plugin is present.

Third-party JARs are stored in each module's `libs/` directory and referenced as system-scope Maven dependencies.

### Core Package Layout (`core/src/main/java/com/jankominek/disenchantment/`)

```
Disenchantment.java          Main plugin class (extends JavaPlugin)
commands/                    Command framework (register, builder, completer, implementations)
  impl/                      Individual command handlers (Help, Toggle, Status, GUI, Diagnostic, etc.)
config/                      Config reading/writing, i18n, migration system
  migration/                 Config migration framework with numbered steps (1-5)
events/                      Custom Bukkit events (DisenchantEvent, ShatterEvent, click variants)
guis/                        In-game GUI system (InventoryBuilder, ItemBuilder, HeadBuilder, components)
  impl/                      GUI screens (NavigationGUI, WorldsGUI, EnchantmentsGUI, repair/sound GUIs)
listeners/                   Event listeners registering at configurable EventPriority
nms/                         NMS interface, mapper, version enum
plugins/                     ISupportedPlugin, IPluginEnchantment interfaces, SupportedPluginManager
types/                       Enums (ConfigKeys, I18nKeys, PermissionType, EnchantmentStateType, etc.)
utils/                       Utilities (AnvilCostUtils, BStatsMetrics, ConfigUtils, DiagnosticUtils,
                              EnchantmentUtils, EventUtils, MapUtils, MaterialUtils, PrecisionUtils,
                              SchedulerUtils, UpdateChecker)
```

### Key Mechanics

1. **Disenchanting**: Player places enchanted item + blank book in anvil -> enchantments transfer to book for XP cost
2. **Shattering (Book Splitting)**: Player places multi-enchantment book + blank book in anvil -> one enchantment splits off

Both features:
- Listen on `PrepareAnvilEvent` and `InventoryClickEvent` at configurable priorities
- Support per-world, per-material, per-enchantment disabling
- Have configurable XP cost formula: `base + (level * multiply)`
- Can be toggled on/off independently
- Fire custom cancellable events (`DisenchantEvent`, `ShatterEvent`)

### Resources (`core/src/main/resources/`)

- `plugin.yml` - Plugin descriptor, commands, permissions
- `config.yml` - Default configuration
- `locales/en.yml`, `locales/cs.yml` - Localization files
- `migrations/1.yml` - `migrations/5.yml` - Config migration definitions

## Adding a New Minecraft Version

1. Determine which NMS module to extend or create a new `v*` module
2. Add the version entry to `MinecraftVersion.java` enum with the correct NMS mapping
3. Implement `NMS_v*_R*.java` implementing the `NMS` interface
4. Create plugin adapters in the `plugins/` package
5. Add the module to root `pom.xml` `<modules>` and `dist/pom.xml` dependencies
6. Place required third-party plugin JARs in the module's `libs/` directory

## Conventions

- Code style: match existing formatting (tabs for indentation, same brace style)
- Static imports used for `Disenchantment.plugin`, `Disenchantment.logger`, `Disenchantment.nms`, `Disenchantment.config`
- Commands use builder pattern via `CommandBuilder`
- Config access through static methods in `Config` class and its inner classes
- Listeners use `EventExecutor` pattern with configurable `EventPriority`
- Permissions default to `op` (admin commands/GUI), `true` (anvil usage)
