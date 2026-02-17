# Contributing to Disenchantment

Thank you for your interest in contributing! All contributions are welcome, whether it's a bug fix, a new feature,
documentation improvements, or support for a new Minecraft version.

## Reporting Issues

Before opening a new issue, please search [existing issues](https://github.com/H7KZ/Disenchantment/issues) to check if
it has already been reported.

- **Bug reports** - Use the [Bug Report](https://github.com/H7KZ/Disenchantment/issues/new?template=bug_report.md)
  template. Include your server version, Java version, Disenchantment version, and a list of other plugins.
- **Feature requests** - Use
  the [Feature Request](https://github.com/H7KZ/Disenchantment/issues/new?template=feature_request.md) template.

## Development Setup

### Prerequisites

- [Java JDK 21+](https://adoptium.net/)
- [Apache Maven](https://maven.apache.org/download.cgi)
- [Spigot BuildTools](https://www.spigotmc.org/wiki/buildtools/)

### Installing Spigot Dependencies

Run BuildTools for each required Minecraft version. These install the Spigot NMS artifacts into your local Maven
repository:

```bash
java -jar BuildTools.jar --rev 1.18
java -jar BuildTools.jar --rev 1.20.5
java -jar BuildTools.jar --rev 1.21
java -jar BuildTools.jar --rev 1.21.5
java -jar BuildTools.jar --rev 1.21.8
```

> **Note:** Versions 1.21.8+ (including 1.21.9, 1.21.10, 1.21.11) all use the same v1_21_R5 module, so installing 1.21.8
> is sufficient.

### Building the Plugin

```bash
mvn clean package
```

The output JAR is written to `target/Disenchantment-<version>.jar`.

## Project Structure

| Module      | Purpose                                                                                   |
|-------------|-------------------------------------------------------------------------------------------|
| `core/`     | Shared logic: main plugin class, config, commands, events, listeners, GUIs, NMS interface |
| `v1_18_R1/` | NMS implementation for Minecraft 1.18 - 1.20.4                                            |
| `v1_20_R4/` | NMS implementation for Minecraft 1.20.5 - 1.20.6                                          |
| `v1_21_R1/` | NMS implementation for Minecraft 1.21 - 1.21.4                                            |
| `v1_21_R4/` | NMS implementation for Minecraft 1.21.5 - 1.21.7                                          |
| `v1_21_R5/` | NMS implementation for Minecraft 1.21.8 - 1.21.+                                          |
| `dist/`     | Shading module that assembles all modules into the final plugin JAR                       |

## Pull Request Process

1. Fork the repository and create a branch from `master`.
2. Make your changes, following the code style described below.
3. Test your changes on a local server with the relevant Minecraft version(s).
4. Open a pull request with a clear description of what was changed and why.

## Code Style

- Match the existing formatting throughout the repository.
- Use tabs for indentation.
- Opening braces on the same line as the statement.
- Static imports for `Disenchantment.plugin`, `Disenchantment.logger`, `Disenchantment.nms`, and
  `Disenchantment.config`.
- Listeners use the `EventExecutor` pattern with configurable `EventPriority`.
- Third-party plugin JARs go in the relevant module's `libs/` directory as system-scope Maven dependencies.

## Adding Support for a New Minecraft Version

1. Add the version entry to `MinecraftVersion.java` in `core/` with the correct NMS module mapping.
2. Create a new `v*` module (or extend an existing one) with an `NMS_v*_R*.java` class implementing the `NMS` interface.
3. Add plugin adapters in the module's `plugins/` package for each supported third-party enchantment plugin.
4. Place required third-party plugin JARs in the module's `libs/` directory.
5. Register the module in the root `pom.xml` `<modules>` list and add it as a dependency in `dist/pom.xml`.
