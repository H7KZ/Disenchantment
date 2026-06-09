<!-- generated-by: gsd-doc-writer -->

# Disenchantment — Internals Reference

Deep-dive documentation for engineers and LLM agents working on the Disenchantment codebase. Each file covers a specific
subsystem in detail, including method signatures, data flows, and guidance for extending the plugin.

## Index

| File                                     | Covers                                                                                                                                               |
|------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| [CORE.md](CORE.md)                       | Main plugin class (`Disenchantment.java`), startup/shutdown sequence, static globals, `onToggle()`, task management                                  |
| [COMMAND_SYSTEM.md](COMMAND_SYSTEM.md)   | `CommandRegister`, `CommandBuilder`, `CommandCompleter`, interfaces, all subcommands, how to add a new command                                       |
| [CONFIG_SYSTEM.md](CONFIG_SYSTEM.md)     | `Config.java` inner class hierarchy, `ConfigKeys` enum, `I18n.java`, migration system (Migration1–Migration9), how to add config keys and migrations |
| [EVENTS.md](EVENTS.md)                   | Anvil event flows (disenchant + shatter), `AnvilEventGuards`, public API events (`PreDisenchantEvent` etc.), EventExecutor pattern, `GUIClickEvent`  |
| [GUI_SYSTEM.md](GUI_SYSTEM.md)           | GUI framework (`InventoryBuilder`, `ItemBuilder`, `HeadBuilder`, `GUIItem`, `GUIComponent`), all GUI screens, how to add a new screen                |
| [NMS.md](NMS.md)                         | `NMS` interface methods, `NMSMapper` reflective loading, `MinecraftVersion` enum + detection algorithm, per-module differences                       |
| [PLUGIN_ADAPTERS.md](PLUGIN_ADAPTERS.md) | `ISupportedPlugin`, `IPluginEnchantment`, `SupportedPluginManager`, per-version support matrix, how to add a new adapter                             |
| [UTILS.md](UTILS.md)                     | All utility classes: `AnvilCostUtils`, `DiagnosticUtils`, `EconomyUtils`, `EventUtils`, `SchedulerUtils`, `UpdateChecker`, `BStatsMetrics`, and more |

## Scope

This documentation covers:

- `core/src/main/java/com/jankominek/disenchantment/` — all plugin logic
- `v1_21_R1/src/main/java/` — used as a representative NMS implementation example
- `core/src/main/resources/` — config.yml, plugin.yml, locales, migrations

## Conventions used in these docs

- Method signatures are written in Java syntax.
- "Feature" refers to either DISENCHANTMENT or SHATTERMENT — the two `AnvilFeature` enum values.
- NMS module names (`v1_18_R1`, `v1_20_R4`, `v1_21_R1`, `v1_21_R4`, `v1_21_R5`) correspond to Maven submodule directory
  names and the class name suffix of each `NMS_v*` implementation.
- Static imports: throughout the codebase `Disenchantment.plugin`, `Disenchantment.nms`, `Disenchantment.config`,
  `Disenchantment.localeConfig`, and `Disenchantment.logger` are imported statically and referenced without the class
  prefix.
