<!-- generated-by: gsd-doc-writer -->

# Disenchantment Documentation

Disenchantment is a Spigot/Paper/Folia plugin that extends the vanilla anvil with two mechanics: **disenchanting**
(strip enchantments from gear onto books) and **shattering** (split multi-enchantment books into individual ones). It
supports Minecraft 1.18 through the latest 1.21.x releases and integrates with custom enchantment plugins, Vault, and
PlaceholderAPI.

Use the sections below to find the documentation relevant to your role.

---

## For Server Admins and Players

Everything needed to install, configure, and use the plugin on a live server.

| Document                                     | Description                                                     |
|----------------------------------------------|-----------------------------------------------------------------|
| [user/INSTALLATION.md](user/INSTALLATION.md) | Requirements, download locations, and step-by-step server setup |
| [user/COMMANDS.md](user/COMMANDS.md)         | Full reference for `/disenchantment` and all subcommands        |
| [user/CONFIG.md](user/CONFIG.md)             | Every option in `plugins/Disenchantment/config.yml` explained   |
| [user/PERMISSIONS.md](user/PERMISSIONS.md)   | Permission nodes, their defaults, and what they control         |
| [user/FAQ.md](user/FAQ.md)                   | Common questions and troubleshooting for known issues           |

---

## For Plugin Developers (API)

Integrate your plugin with Disenchantment's disenchant and shatter mechanics.

| Document                                   | Description                                                                                               |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| [api/README.md](api/README.md)             | API overview — adding the dependency, declaring soft-depend, and getting started                          |
| [api/EVENTS.md](api/EVENTS.md)             | Full reference for `PreDisenchantEvent`, `PostDisenchantEvent`, `PreShatterEvent`, and `PostShatterEvent` |
| [api/PLACEHOLDERS.md](api/PLACEHOLDERS.md) | PlaceholderAPI placeholders registered under `%disenchantment_*%`                                         |

---

## For Contributors and Engineers

How to build, develop, and contribute to the Disenchantment codebase.

| Document                                                               | Description                                                          |
|------------------------------------------------------------------------|----------------------------------------------------------------------|
| [engineering/README.md](engineering/README.md)                         | Engineering docs index and repository layout overview                |
| [engineering/ARCHITECTURE.md](engineering/ARCHITECTURE.md)             | System design, module structure, NMS abstraction, and event flow     |
| [engineering/CONTRIBUTING.md](engineering/CONTRIBUTING.md)             | Contribution guide — issues, code style, and PR process              |
| [engineering/SETUP.md](engineering/SETUP.md)                           | Developer environment setup — JDK, Maven, Spigot BuildTools, and IDE |
| [engineering/TESTING.md](engineering/TESTING.md)                       | Test structure, how to run tests, and how to write new ones          |
| [engineering/ADDING_NEW_VERSION.md](engineering/ADDING_NEW_VERSION.md) | Step-by-step guide for adding support for a new Minecraft version    |

---

## Internals Reference

Deep-dive documentation for LLM agents and contributors working on specific subsystems.

| Document                                                     | Description                                                                        |
|--------------------------------------------------------------|------------------------------------------------------------------------------------|
| [internals/README.md](internals/README.md)                   | Internals index and orientation                                                    |
| [internals/CORE.md](internals/CORE.md)                       | Main plugin class, lifecycle, and static singletons                                |
| [internals/COMMAND_SYSTEM.md](internals/COMMAND_SYSTEM.md)   | Command framework — `CommandBuilder`, registration, and completer                  |
| [internals/CONFIG_SYSTEM.md](internals/CONFIG_SYSTEM.md)     | Config loading, `Config` class, i18n, and migration system                         |
| [internals/EVENTS.md](internals/EVENTS.md)                   | Internal and public custom events, listener registration                           |
| [internals/GUI_SYSTEM.md](internals/GUI_SYSTEM.md)           | In-game GUI system — `InventoryBuilder`, `ItemBuilder`, and GUI screens            |
| [internals/NMS.md](internals/NMS.md)                         | NMS abstraction layer — `NMS` interface, `NMSMapper`, and `MinecraftVersion` enum  |
| [internals/PLUGIN_ADAPTERS.md](internals/PLUGIN_ADAPTERS.md) | Custom enchantment plugin adapter system and `ISupportedPlugin` interface          |
| [internals/UTILS.md](internals/UTILS.md)                     | Utility classes — `AnvilCostUtils`, `EnchantmentUtils`, `SchedulerUtils`, and more |

---

## External Links

- [GitHub](https://github.com/H7KZ/Disenchantment)
- [SpigotMC](https://www.spigotmc.org/resources/110741)
- [Modrinth](https://modrinth.com/plugin/disenchantment)
- [bStats](https://bstats.org/plugin/bukkit/Disenchantment/19058)
