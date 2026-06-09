<!-- generated-by: gsd-doc-writer -->
# Contributing to Disenchantment

Thank you for contributing. This document covers the developer-side details — code style, PR process, testing expectations, and how to add new plugin adapters. For environment setup see [SETUP.md](SETUP.md). For a codebase overview see [ARCHITECTURE.md](ARCHITECTURE.md).

## Reporting issues

Before opening a new issue, search [existing issues](https://github.com/H7KZ/Disenchantment/issues) to avoid duplicates.

- **Bug reports** — Use the [Bug Report](https://github.com/H7KZ/Disenchantment/issues/new?template=bug_report.md) template. Include: server software and version, Java version, Disenchantment version, full list of other installed plugins, and the exact steps to reproduce. Attach any relevant console output and, if applicable, enable `logging.level: DEBUG` in `config.yml` before reproducing to get the full operation trace.
- **Feature requests** — Use the [Feature Request](https://github.com/H7KZ/Disenchantment/issues/new?template=feature_request.md) template. Describe the use case, not just the feature.
- **New Minecraft version support** — Open an issue if you notice a new MC version has been released and Disenchantment does not yet support it. The process is documented in detail in [ADDING_NEW_VERSION.md](ADDING_NEW_VERSION.md).

## Development setup

See [SETUP.md](SETUP.md) for the full setup walkthrough. Summary:

1. Install JDK 21+ and Apache Maven.
2. Run Spigot BuildTools for all five required versions (`1.18`, `1.20.5`, `1.21`, `1.21.5`, `1.21.8`).
3. `git clone https://github.com/H7KZ/Disenchantment.git && cd Disenchantment`
4. `mvn clean package` — output is `target/Disenchantment-<version>.jar`.

## Project structure

| Module | Artifact ID | Purpose |
|---|---|---|
| `core/` | `disenchantment-core` | Main plugin class, config, commands, events, listeners, GUIs, NMS interface |
| `v1_18_R1/` | `disenchantment-v1_18_R1` | NMS for 1.18 – 1.20.4 (uses NBT package for repair cost) |
| `v1_20_R4/` | `disenchantment-v1_20_R4` | NMS for 1.20.5 – 1.20.6 (uses NBT package for repair cost) |
| `v1_21_R1/` | `disenchantment-v1_21_R1` | NMS for 1.21 – 1.21.4 (Bukkit API, no NBT) |
| `v1_21_R4/` | `disenchantment-v1_21_R4` | NMS for 1.21.5 – 1.21.7 |
| `v1_21_R5/` | `disenchantment-v1_21_R5` | NMS for 1.21.8 – 1.21.11+, 26.x.x (LATEST fallback) |
| `dist/` | `disenchantment-dist` | Shading module — assembles the final plugin JAR |

## Code style

Match the existing formatting throughout. The rules:

- **Indentation:** tabs (not spaces).
- **Braces:** opening brace on the same line as the statement (`if (x) {`, not on a new line).
- **Static imports:** use static imports for the five global statics — `Disenchantment.plugin`, `Disenchantment.nms`, `Disenchantment.config`, `Disenchantment.localeConfig`, `Disenchantment.logger`.
- **Listener pattern:** use `EventExecutor` with configurable `EventPriority` (see the existing `DisenchantListener` as the template). Do not use `@EventHandler` for the anvil event listeners.
- **Third-party JARs:** place them in the relevant module's `libs/` directory and declare them as `system`-scope Maven dependencies in that module's `pom.xml`. Do not commit JARs that belong in a public Maven repository.
- **Error handling:** wrap event handler bodies in a `try/catch` that delegates to `DiagnosticUtils.throwReport(e)`. This ensures all crashes are reported with context instead of being silently swallowed.
- **Javadoc:** all public API classes and methods should have Javadoc. Private helpers do not require it, but a one-line comment is appreciated.

## Pull request process

1. Fork the repository and create a branch from `master`. Branch names are not enforced but `feat/`, `fix/`, `nms/`, `docs/` prefixes help reviewers at a glance.
2. Make your changes following the code style above.
3. Add or update tests for any logic changes. See [TESTING.md](TESTING.md) for guidance.
4. Test on a real server with at least the Minecraft version(s) your change affects. For NMS changes, test all versions covered by the affected module.
5. Open a pull request against `master` with a clear description of what was changed and why. Reference any related issue numbers.
6. Keep PRs focused — one concern per PR is easier to review and merge.

There is no formal review SLA but smaller, well-tested PRs merge faster.

## Testing guidelines

- Unit tests live in `core/src/test/`. They use JUnit 5, MockBukkit 4.x, and Mockito.
- Extend `DisenchantmentTestBase` which sets up MockBukkit and injects `MockNMS`.
- Do not use real NMS classes in tests. Use `MockNMS` and `MockPluginAdapter`.
- The NMS layer is mocked via `Mockito.mockStatic(NMSMapper.class)` in `DisenchantmentTestBase.setUpBase()`.
- Config values can be set per-test with the `setConfig(key, value)` helper.
- Run all tests with `mvn test`.

See [TESTING.md](TESTING.md) for a complete guide.

## Adding a new custom enchantment plugin adapter

If a third-party enchantment plugin is not yet supported, here is how to add it.

### 1. Understand the interface

Your adapter must implement `ISupportedPlugin` from `core/src/main/java/com/jankominek/disenchantment/plugins/`:

```java
public interface ISupportedPlugin {
    String getName();  // must match the plugin's folder name exactly (case-sensitive)
    List<IPluginEnchantment> getItemEnchantments(ItemStack item);
    // optionally override the world-aware overload:
    default List<IPluginEnchantment> getItemEnchantments(ItemStack item, World world) { ... }
    default void activate() {}  // called once when the adapter is activated at startup
}
```

`IPluginEnchantment` is the common enchantment handle. It provides:

```java
public interface IPluginEnchantment {
    NamespacedKey getKey();    // namespaced enchantment key
    int getLevel();            // enchantment level
    ItemStack addToBook(ItemStack book);      // add this enchantment to a book item
    ItemStack removeFromItem(ItemStack item); // remove this enchantment from an item
}
```

### 2. Choose which NMS modules to add the adapter to

Adapters that work across all MC versions go in all five `v*` modules. Adapters that require APIs only available in newer MC versions go in the relevant modules only (e.g. ExcellentEnchants is in `v1_21_R1`, `v1_21_R4`, `v1_21_R5`).

### 3. Obtain the third-party JAR

Get the third-party plugin JAR for compilation. Copy it into each target module's `libs/` directory:

```
v1_21_R5/libs/MyEnchantPlugin-1.0.0.jar
```

### 4. Declare the dependency in the module's pom.xml

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>myenchantplugin</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${pom.basedir}/libs/MyEnchantPlugin-1.0.0.jar</systemPath>
</dependency>
```

### 5. Create the adapter class

Name the class after the plugin and the NMS module version, e.g. `MyEnchantPlugin_v1_21_R5.java`. Place it in `v1_21_R5/src/main/java/plugins/`:

```java
package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class MyEnchantPlugin_v1_21_R5 implements ISupportedPlugin {

    @Override
    public String getName() {
        return "MyEnchantPlugin"; // must match the server plugin folder name exactly
    }

    @Override
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        List<IPluginEnchantment> result = new ArrayList<>();
        // Use the third-party plugin's API to read enchantments from item
        // Wrap each enchantment in an IPluginEnchantment implementation
        return result;
    }
}
```

### 6. Register the adapter in the NMS class

Open the NMS class for the module (e.g. `NMS_v1_21_R5.java`) and add the adapter to `getSupportedPlugins()`:

```java
@Override
public List<ISupportedPlugin> getSupportedPlugins() {
    return new ArrayList<>() {{
        add(new plugins.AdvancedEnchantments_v1_21_R5());
        add(new plugins.EcoEnchants_v1_21_R5());
        // ... existing adapters ...
        add(new plugins.MyEnchantPlugin_v1_21_R5()); // add here
    }};
}
```

### 7. Add `softdepend` in plugin.yml

Open `core/src/main/resources/plugin.yml` and add the plugin name to the `softdepend` list so Bukkit loads it before Disenchantment when present:

```yaml
softdepend: [ Vault, VaultUnlocked, ExcellentEnchants, EcoEnchants, eco,
              AdvancedEnchantments, UberEnchant, EnchantsSquared,
              PlaceholderAPI, MyEnchantPlugin ]
```

### 8. Test

Build the plugin and test on a server with the third-party plugin installed. Check that the adapter appears in the startup log:

```
[Disenchantment] Plugin adapters active: MyEnchantPlugin
```

Enable `logging.level: DEBUG` in `config.yml` to trace the adapter's interaction with each anvil event.
