<!-- generated-by: gsd-doc-writer -->

# Disenchantment API

Disenchantment exposes a public Java API for other plugins to integrate with its disenchant and shatter mechanics. The
API consists of four Bukkit events and a PlaceholderAPI expansion.

## Contents

| Document                           | Description                                                                                                                                                                  |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [EVENTS.md](EVENTS.md)             | Full reference for `PreDisenchantEvent`, `PostDisenchantEvent`, `PreShatterEvent`, and `PostShatterEvent` — including all methods, cancellation behaviour, and code examples |
| [PLACEHOLDERS.md](PLACEHOLDERS.md) | PlaceholderAPI placeholders registered under the `%disenchantment_*%` identifier                                                                                             |

## Adding the Dependency

Disenchantment is not published to Maven Central. Download the JAR
from [GitHub Releases](https://github.com/H7KZ/Disenchantment/releases) and add it as a local or system-scoped
dependency. It must be declared as a `compileOnly`/`provided` dependency — the plugin ships as a full JAR on the server;
your plugin must not shade it.

### Maven

```xml
<dependency>
    <groupId>com.jankominek</groupId>
    <artifactId>disenchantment-core</artifactId>
    <version>6.5.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/Disenchantment-6.5.0.jar</systemPath>
</dependency>
```

Replace `6.5.0` with the version you are targeting and adjust `systemPath` to wherever you placed the JAR.

### Gradle

```groovy
dependencies {
    compileOnly files('libs/Disenchantment-6.5.0.jar')
}
```

## Declaring a Soft Dependency

Add Disenchantment as a soft dependency in your `plugin.yml` so Bukkit loads it before your plugin when both are present
on the server:

```yaml
softdepend:
  - Disenchantment
```

Use `softdepend` rather than `depend` so your plugin still loads on servers that do not have Disenchantment installed.

## Listening to Events

Disenchantment events are standard Bukkit events. Register a listener the same way you would for any other Bukkit event:

```java
public class MyListener implements Listener {

    @EventHandler
    public void onPreDisenchant(PreDisenchantEvent event) {
        // cancel the operation, inspect enchantments, etc.
    }

    @EventHandler
    public void onPostDisenchant(PostDisenchantEvent event) {
        // react after the disenchant completes
    }
}
```

Register the listener in your plugin's `onEnable`:

```java
getServer().getPluginManager().registerEvents(new MyListener(), this);
```

No special setup is required — events fire automatically whenever a player uses the anvil mechanic.

## API Stability

The four event classes and `IPluginEnchantment` interface in the `com.jankominek.disenchantment.events.api` and
`com.jankominek.disenchantment.plugins` packages are considered stable public API. Other classes in the
`com.jankominek.disenchantment` package tree are internal and may change without notice between versions.

## Links

- [GitHub Releases](https://github.com/H7KZ/Disenchantment/releases)
- [SpigotMC](https://www.spigotmc.org/resources/110741)
- [Modrinth](https://modrinth.com/plugin/disenchantment)
