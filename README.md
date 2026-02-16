# Disenchantment

[![GitHub Release](https://img.shields.io/github/v/release/H7KZ/Disenchantment?style=flat-square)](https://github.com/H7KZ/Disenchantment/releases/latest)
[![License](https://img.shields.io/github/license/H7KZ/Disenchantment?style=flat-square)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.18--1.21.+-brightgreen?style=flat-square)](https://www.spigotmc.org/resources/110741)
[![Java](https://img.shields.io/badge/Java-21%2B-orange?style=flat-square)](https://adoptium.net/)
[![Modrinth](https://img.shields.io/modrinth/dt/disenchantment?style=flat-square&label=Modrinth)](https://modrinth.com/plugin/disenchantment)
[![SpigotMC](https://img.shields.io/spiget/downloads/110741)](https://www.spigotmc.org/resources/110741)
[![Players](https://img.shields.io/bstats/players/19058)](https://bstats.org/plugin/bukkit/Disenchantment/19058)
[![Servers](https://img.shields.io/bstats/servers/19058)](https://bstats.org/plugin/bukkit/Disenchantment/19058)

![event.png](assets/event.png)

A lightweight Spigot/Paper/Folia plugin that enhances the vanilla enchanting system by adding powerful, intuitive mechanics to the standard anvil. Give your players the tools to manage their enchantments efficiently, without adding any new blocks or complex systems.

## Features

### Disenchanting: Reclaim Your Enchantments

Ever found a nearly-broken tool with a rare enchantment you wish you could save? The disenchanting mechanic provides a straightforward solution.

- **Simple Anvil Process** - Place the enchanted item in the first anvil slot and a book in the second.
- **Controlled Transfer** - For a configurable XP cost, the enchantment is removed from the item and transferred to the book, leaving the original item clean. Players can salvage valuable enchantments from worn-out or obsolete gear.

### Book Splitting: Divide Your Enchanted Books

Need just one enchantment from a book with many? Book Splitting allows for precise, strategic use of your enchanted books.

- **Intuitive Splitting** - Combine a multi-enchantment book with a blank book on the anvil to lift a single enchantment off the original.
- **Strategic Application** - Results in two books: the original with one less enchantment, and a new book with the single enchantment that was split off. This enables targeted application and a more dynamic server economy.

### Why Choose This Plugin?

- **Truly Vanilla-Friendly** - No new machines or confusing UIs. All functions are handled through the standard anvil.
- **Lightweight & Efficient** - Adds valuable features without causing server lag or performance issues.
- **Fully Customizable** - Configure XP costs, per-world/per-enchantment/per-material restrictions, event priorities, and more.
- **Folia Support** - Compatible with the Folia server software for regionized multithreading.

## Installation

### Requirements

- Java **21** or newer
- Spigot, Paper, or Folia server running Minecraft **1.18 - 1.21.+**

### Download

Get the latest release from any of these sources:

- [GitHub Releases](https://github.com/H7KZ/Disenchantment/releases/latest)
- [SpigotMC](https://www.spigotmc.org/resources/110741)
- [Modrinth](https://modrinth.com/plugin/disenchantment)

### Setup

1. Download the `Disenchantment-<version>.jar` file.
2. Place the JAR in your server's `plugins/` directory.
3. Restart the server.
4. Edit `plugins/Disenchantment/config.yml` to customize settings or use ingame admin UI with `/disenchantment gui`.

## Supported Custom Enchantment Plugins

| Plugin | 1.21.+ - 1.21.8 | 1.21.7 - 1.21.5 | 1.21.4 - 1.21 | 1.20.6 - 1.20.5 | 1.20.4 - 1.18 |
|---|:---:|:---:|:---:|:---:|:---:|
| [AdvancedEnchantments](https://www.spigotmc.org/resources/43058) | ✓ | ✓ | ✓ | ✓ | ✓ |
| [EcoEnchants](https://www.spigotmc.org/resources/79573) | ✓ | ✓ | ✓ | ✓ | ✓ |
| [EnchantsSquared](https://www.spigotmc.org/resources/86747) | ✓ | ✓ | ✓ | ✓ | ✓ |
| [UberEnchant](https://www.spigotmc.org/resources/19448) | ✓ | ✓ | ✓ | ✓ | ✓ |
| [Vane](https://modrinth.com/plugin/vane) | ✓ | ✓ | ✓ | - | - |
| [Zenchantments](https://github.com/moikheck/Zenchantments) | ✓ | ✓ | ✓ | - | - |
| [ExcellentEnchants](https://www.spigotmc.org/resources/61693) | ✓ | ✓ | - | - | - |

> **Note:** EcoEnchants requires a patched build. See [EcoEnchants#417](https://github.com/Auxilor/EcoEnchants/pull/417) for details, or download the pre-built v12.24.0 from the [v6.2.2 release](https://github.com/H7KZ/Disenchantment/releases/tag/v6.2.2).

## Documentation

| Document | Description |
|---|---|
| [Commands](COMMANDS.md) | Full command reference with usage and permissions |
| [Permissions](PERMISSIONS.md) | All permission nodes and their defaults |
| [Configuration](CONFIG.md) | Complete configuration reference |
| [FAQ](FAQ.md) | Frequently asked questions and troubleshooting |

## Building from Source

```bash
mvn clean package
```

The output JAR is written to `target/Disenchantment-<version>.jar`. See [CONTRIBUTING.md](CONTRIBUTING.md) for prerequisites and development setup.

## Contributing

Contributions are welcome! Please read the [Contributing Guidelines](CONTRIBUTING.md) before opening a pull request.

## Links

- [SpigotMC](https://www.spigotmc.org/resources/110741)
- [Modrinth](https://modrinth.com/plugin/disenchantment)
- [bStats](https://bstats.org/plugin/bukkit/Disenchantment/19058)
- [GitHub](https://github.com/H7KZ/Disenchantment)

## License

This project is licensed under the [MIT License](LICENSE).

## Dependencies and Attribution

### Bundled Libraries

The following libraries are included in the distributed plugin JAR:

| Library | License | Author | Purpose |
|---------|---------|--------|---------|
| [bStats](https://github.com/Bastian/bStats-Metrics) | MIT | Bastian Oppermann | Anonymous plugin usage statistics (optional, can be disabled in config) |

### Runtime Dependencies (Provided by Server)

These APIs are provided by the Minecraft server and are not bundled with the plugin:

| API | License | Link |
|-----|---------|------|
| Spigot API | GPL-3.0 | [github.com/SpigotMC/Spigot-API](https://github.com/SpigotMC/Spigot-API) |
| Paper API | GPL-3.0 | [github.com/PaperMC/Paper](https://github.com/PaperMC/Paper) |
| Folia API | GPL-3.0 | [github.com/PaperMC/Folia](https://github.com/PaperMC/Folia) |

### Custom Enchantment Plugin Adapters (Compile-time Only)

Disenchantment includes adapters for the following plugins. **These plugins are NOT bundled or distributed** with Disenchantment. They are used only as compile-time dependencies to create integration adapters. Server administrators must obtain and install these plugins separately if they wish to use the integration features.

| Plugin | License | Author | Link |
|--------|---------|--------|------|
| AdvancedEnchantments | Proprietary | GC-spigot | [github.com/GC-spigot/AdvancedEnchantments](https://github.com/GC-spigot/AdvancedEnchantments) |
| EcoEnchants | GPL-3.0 | Auxilor | [github.com/Auxilor/EcoEnchants](https://github.com/Auxilor/EcoEnchants) |
| EnchantsSquared | No public license | Athlaeos | [github.com/Athlaeos/EnchantsSquared](https://github.com/Athlaeos/EnchantsSquared) |
| UberEnchant | GPL-3.0 | sciguymjm | [github.com/Sciguymjm/UberEnchant](https://github.com/Sciguymjm/UberEnchant) |
| ExcellentEnchants | GPL-3.0 | NightExpress | [github.com/nulli0n/ExcellentEnchants-spigot](https://github.com/nulli0n/ExcellentEnchants-spigot) |
| Zenchantments | GPL-3.0 | Zedly | [github.com/Zedly/Zenchantments](https://github.com/Zedly/Zenchantments) |
| Vane | MIT | oddlama | [github.com/oddlama/vane](https://github.com/oddlama/vane) |

**Note:** The adapters created by Disenchantment merely interface with these plugins' public APIs and do not contain any code from the plugins themselves. Each plugin retains its own independent license.

### Build Tools

The following tools are used during the build process but are not included in the final plugin:

| Tool | License | Link |
|------|---------|------|
| Maven Shade Plugin | Apache-2.0 | [maven.apache.org/plugins/maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/) |
| Maven Compiler Plugin | Apache-2.0 | [maven.apache.org/plugins/maven-compiler-plugin](https://maven.apache.org/plugins/maven-compiler-plugin/) |
