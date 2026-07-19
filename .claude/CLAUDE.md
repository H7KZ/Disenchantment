# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Disenchantment - Minecraft Plugin

A Spigot/Paper/Folia Minecraft plugin that adds disenchanting and book-splitting mechanics to the vanilla anvil. Players
can remove enchantments from items onto books, or split multi-enchantment books into individual ones.

## Project Overview

- **Group ID**: `com.jankominek`
- **Version**: managed via `${revision}` property in root `pom.xml` (currently `6.5.11`)
- **Java**: 17 (core/v1_18_R1), 21 (v1_20_R4, v1_21_R1, v1_21_R4, v1_21_R5)
- **API**: Spigot API 1.18+, Folia API support
- **Author**: Jan Kominek (H7KZ)
- **Entry point**: `com.jankominek.disenchantment.Disenchantment` (registered in `core/src/main/resources/plugin.yml`)

## Build & Test

```bash
# Full build (requires BuildTools JARs installed — see CONTRIBUTING.md)
mvn clean package

# Run all core tests (offline, no BuildTools required)
& "C:\Users\honzi\Documents\Spigot BuildTools\apache-maven-3.9.6\bin\mvn.cmd" test -pl core --offline

# Run a single test class
& "C:\Users\honzi\Documents\Spigot BuildTools\apache-maven-3.9.6\bin\mvn.cmd" test -pl core --offline -Dtest=StatsCacheTest

# Run a single test method
& "C:\Users\honzi\Documents\Spigot BuildTools\apache-maven-3.9.6\bin\mvn.cmd" test -pl core --offline -Dtest=StatsCacheTest#testRecord
```

**Prerequisites** (see CONTRIBUTING.md):

- Maven
- JDK 21+
- Spigot BuildTools with versions 1.21, 1.20.5, and 1.18 installed
- Output JAR lands in `target/Disenchantment-<version>.jar`
- Tests use **MockBukkit + JUnit 5**; core tests run without a live server

## Module Structure

Multi-module Maven project. Each NMS module provides version-specific implementations.

| Module      | Artifact                  | Purpose                                                                                                             |
|-------------|---------------------------|---------------------------------------------------------------------------------------------------------------------|
| `core/`     | `disenchantment-core`     | Shared logic: plugin main class, config, commands, events, listeners, GUIs, NMS interface, plugin adapter interface |
| `v1_18_R1/` | `disenchantment-v1_18_R1` | NMS for Minecraft 1.18 - 1.20.4 (uses NBT editing via `nbt/` package)                                               |
| `v1_20_R4/` | `disenchantment-v1_20_R4` | NMS for Minecraft 1.20.5 - 1.20.6 (uses NBT editing via `nbt/` package)                                             |
| `v1_21_R1/` | `disenchantment-v1_21_R1` | NMS for Minecraft 1.21 - 1.21.4 (no NBT package, uses Bukkit API directly)                                          |
| `v1_21_R4/` | `disenchantment-v1_21_R4` | NMS for Minecraft 1.21.5 - 1.21.7                                                                                   |
| `v1_21_R5/` | `disenchantment-v1_21_R5` | NMS for Minecraft 1.21.8 - 1.21.11+ (and future via LATEST fallback)                                                |
| `dist/`     | `disenchantment-dist`     | Shading module - assembles all modules into the final plugin JAR using maven-shade-plugin                           |

## Architecture

### NMS (Net Minecraft Server) Abstraction

- **`core/.../nms/NMS.java`** - Interface with default methods for version-specific operations (enchantment checks,
  registry access, repair cost, skull textures)
- **`core/.../nms/NMSMapper.java`** - Reflectively loads `NMS_<version>` class at runtime based on detected server
  version
- **`core/.../nms/MinecraftVersion.java`** - Enum mapping every supported MC version to its NMS module. Unknown
  versions >= 1.21 fall back to `LATEST` (v1_21_R5)
- Each `v*` module provides `NMS_v*_R*.java` implementing the `NMS` interface

### Custom Enchantment Plugin Support

Each NMS module contains adapters in `plugins/` for third-party enchantment plugins:

- **AdvancedEnchantments** - all versions
- **EcoEnchants** - all versions (requires patched build, see FAQ)
- **EnchantsSquared** - all versions
- **UberEnchant** - all versions
- **ExcellentEnchants** - v1_21_R1, v1_21_R4, v1_21_R5 only
- **Vane** - v1_21_R1, v1_21_R4, v1_21_R5 only
- **Zenchantments** - v1_21_R1, v1_21_R4, v1_21_R5 only

Adapters implement `ISupportedPlugin` interface. They are instantiated in each `NMS_v*` class's `getSupportedPlugins()`
method. `SupportedPluginManager` activates only those whose server-side plugin is present.

Third-party JARs are stored in each module's `libs/` directory and referenced as system-scope Maven dependencies.

### Core Package Layout (`core/src/main/java/com/jankominek/disenchantment/`)

```
Disenchantment.java          Main plugin class (extends JavaPlugin)
commands/                    Command framework (register, builder, completer, implementations)
  impl/                      Individual command handlers (Help, Toggle, Status, GUI, Diagnostic, etc.)
config/                      Config reading/writing, i18n, migration system
  migration/                 Config migration framework with numbered steps (1-N)
events/                      Custom Bukkit events (DisenchantEvent, ShatterEvent, click variants)
                             Pre* and Post* API events expose xpCost + economyCost to third-party plugins
guis/                        In-game GUI system (InventoryBuilder, ItemBuilder, HeadBuilder, components)
  impl/                      GUI screens (NavigationGUI, WorldsGUI, EnchantmentsGUI, repair/sound GUIs,
                             StatsGUI, SplitCountGUI, MaterialsGUI)
listeners/                   Event listeners registering at configurable EventPriority
nms/                         NMS interface, mapper, version enum
plugins/                     ISupportedPlugin, IPluginEnchantment interfaces, SupportedPluginManager
stats/                       SQLite-backed analytics: StatsDatabase, StatsCache, StatsManager,
                             StatsListener, OperationRecord/Type, BootData
types/                       Enums (ConfigKeys, I18nKeys, PermissionType, EnchantmentStateType, etc.)
utils/                       Utilities (AnvilCostUtils, BStatsMetrics, ConfigUtils, DiagnosticUtils,
                              EnchantmentUtils, EventUtils, MapUtils, MaterialUtils, PrecisionUtils,
                              SchedulerUtils, UpdateChecker, CostMultiplierUtils, CooldownManager)
```

### Stats system

`StatsManager` is a nullable singleton — `getInstance()` returns `null` when `logging.operations: false`. All call sites
must null-check. `StatsCache` is the in-memory read path (never query the DB on the hot path). Writes go async via
`StatsDatabase.insertAsync()`; cache is updated synchronously on the main thread immediately after. Boot data is loaded
async, then applied to the cache on the main thread via a `runTask` callback.

### Event flow (anvil click)

`PrepareAnvilEvent` → `DisenchantEvent`/`ShatterEvent` (cancellable, fires `PreDisenchantEvent`/`PreShatterEvent`) → on
result click: `DisenchantClickEvent`/`ShatterClickEvent` → charges economy → deducts XP → fires `PostDisenchantEvent`/
`PostShatterEvent` (carries `xpCost` + `economyCost`). `StatsListener` listens on the Post events.

### Key Mechanics

1. **Disenchanting**: Player places enchanted item + blank book in anvil -> enchantments transfer to book for XP cost
2. **Shattering (Book Splitting)**: Player places multi-enchantment book + blank book in anvil -> one enchantment splits
   off

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
- Static imports used for `Disenchantment.plugin`, `Disenchantment.logger`, `Disenchantment.nms`,
  `Disenchantment.config`
- Commands use builder pattern via `CommandBuilder`
- Config access through static methods in `Config` class and its inner classes
- Listeners use `EventExecutor` pattern with configurable `EventPriority`
- Permissions default to `op` (admin commands/GUI), `true` (anvil usage)

## Documentation

The `docs/` directory contains comprehensive documentation for all audiences:

- **`docs/README.md`** — top-level index, start here
- **`docs/user/`** — server admin & player guides (installation, commands, config, permissions, FAQ)
- **`docs/api/`** — public API reference (events, PlaceholderAPI)
- **`docs/engineering/`** — architecture, dev setup, contributing, adding new MC versions
- **`docs/internals/`** — deep-dive reference for LLM agents and contributors (core, NMS, events, GUI, commands, config,
  utils)
- **`docs/superpowers/specs/`** — approved design specs for implemented features (ground truth for spec compliance)
- **`docs/superpowers/plans/`** — step-by-step implementation plans corresponding to each spec

When working on this codebase, consult `docs/internals/` for how systems work and
`docs/engineering/ADDING_NEW_VERSION.md` for version support changes.

**Docs discipline:** After every change, run the doc-review rule above before closing the task.
