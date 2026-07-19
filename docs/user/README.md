<!-- generated-by: gsd-doc-writer -->

# User Guide

Welcome to the Disenchantment user documentation. Disenchantment is a Spigot/Paper/Folia plugin that adds two features
to the vanilla anvil: removing enchantments from items onto books (disenchanting) and splitting multi-enchantment books
into individual books (shattering). No new blocks or GUIs are added — everything works through the standard anvil.

**New to the plugin? Start here:**

- [GETTING_STARTED.md](GETTING_STARTED.md) — Just installed? This is your first stop. Five-step setup, your first
  disenchant, and answers to the most common questions.
- [HOW_TO.md](HOW_TO.md) — Practical step-by-step guides for common admin tasks (restrict worlds, block enchantments,
  set up economy costs, and more).

## Documents in This Folder

| Document                                 | Description                                                                                                        |
|------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| [GETTING_STARTED.md](GETTING_STARTED.md) | **Start here.** Quick setup, your first disenchant, your first book split, and common first questions.             |
| [HOW_TO.md](HOW_TO.md)                   | Cookbook of common admin tasks with exact commands and config snippets.                                            |
| [INSTALLATION.md](INSTALLATION.md)       | Requirements, download locations, step-by-step setup, and Vault/economy plugin configuration.                      |
| [COMMANDS.md](COMMANDS.md)               | Full command reference for `/disenchantment` and all subcommands, including diagnostic and configuration commands. |
| [CONFIG.md](CONFIG.md)                   | Complete reference for every option in `plugins/Disenchantment/config.yml`.                                        |
| [PERMISSIONS.md](PERMISSIONS.md)         | Every permission node, its default, and what it controls.                                                          |
| [FAQ.md](FAQ.md)                         | Answers to common questions and troubleshooting steps for known issues.                                            |

## Quick Overview

### Disenchanting

Place an enchanted item in the first anvil slot and a blank book in the second. For a configurable XP cost the
enchantment is stripped from the item and transferred to the book. Useful for salvaging enchantments from worn-out gear.

### Shattering (Book Splitting)

Place a multi-enchantment book in the first anvil slot and a blank book in the second. One enchantment is lifted off the
original book onto the new book. The result is two books: the original with one fewer enchantment, and a new book
containing only the split enchantment.

### Configuration

All settings can be changed in `plugins/Disenchantment/config.yml`, via `/disenchantment gui` in-game, or via commands
(see [COMMANDS.md](COMMANDS.md)). Features can be restricted per-world, per-material, and per-enchantment. An optional
Vault economy integration can charge players an in-game currency cost per operation.

## Supported Minecraft Versions

Minecraft 1.18 through 1.21.x and 26.1.x are supported. Unknown versions 1.21+ fall back to the latest NMS module
automatically.

## Links

- [GitHub](https://github.com/H7KZ/Disenchantment)
- [SpigotMC](https://www.spigotmc.org/resources/110741)
- [Modrinth](https://modrinth.com/plugin/disenchantment)
- [bStats](https://bstats.org/plugin/bukkit/Disenchantment/19058)
