<!-- generated-by: gsd-doc-writer -->
# Config and I18n System

`core/src/main/java/com/jankominek/disenchantment/config/`

The config system has three main parts: `Config.java` (typed accessors for `config.yml`), `I18n.java` (typed accessors for locale files), and the migration system (`migration/`) that upgrades old config files.

---

## Config.java

`config/Config.java` — pure static class with no instances. All methods read from or write to `Disenchantment.config` (a `FileConfiguration`).

### Batch Mode

```java
Config.beginBatch();
// multiple set calls
Config.commitBatch();
```

When `batchMode = true`, individual setter calls do **not** call `plugin.saveConfig()`. `commitBatch()` saves once. Used by GUI screens that change multiple values in one click.

### `FeatureConfig` Interface

```java
public interface FeatureConfig {
    boolean isEnabled();
    List<World> getDisabledWorlds();
    boolean setDisabledWorlds(List<World> worlds);
    HashMap<String, EnchantmentStateType> getEnchantmentStates();
    boolean setEnchantmentStates(HashMap<String, EnchantmentStateType> enchantmentStates);
}
```

`Config.forFeature(AnvilFeature feature)` returns an anonymous `FeatureConfig` implementation backed by either `Config.Disenchantment` or `Config.Shatterment`, allowing feature-agnostic code in GUI screens to pass a feature argument.

### `invalidateCaches()`

```java
Config.invalidateCaches();
```

Nulls `Config.Disenchantment.ENCHANTMENT_STATES_CACHE` and `Config.Shatterment.ENCHANTMENT_STATES_CACHE`. Called by `ConfigUtils.setupConfig()` after a reload. Enchantment states are parsed from a `List<String>` and cached as a `HashMap<String, EnchantmentStateType>`; this method clears that cache so the next read re-parses from the updated YAML.

---

## Config Inner Class Hierarchy

```
Config
├── isPluginEnabled() / setPluginEnabled(boolean)
├── getLocale() / getLocales()
├── EventPriorities (static inner class)
│   ├── getDisenchantEvent()      → EventPriority
│   ├── getDisenchantClickEvent() → EventPriority
│   ├── getShatterEvent()         → EventPriority
│   └── getShatterClickEvent()    → EventPriority
├── Logging (static inner class)
│   ├── getLevel()                → LogLevelType
│   └── isSaveReportsEnabled()    → boolean
├── Disenchantment (static inner class)
│   ├── isEnabled() / setEnabled(boolean)
│   ├── getDisabledWorlds() / setDisabledWorlds(List<World>)
│   ├── getDisabledMaterials() / setDisabledMaterials(List<Material>)
│   ├── getEnchantmentStates() / setEnchantmentStates(HashMap<String, EnchantmentStateType>)
│   ├── Economy (static inner class)
│   │   ├── isEnabled() / setEnabled(boolean)
│   │   ├── getCost() / setCost(double)
│   │   ├── isShowCostEnabled() / setShowCostEnabled(boolean)
│   │   └── isChargeMessageEnabled() / setChargeMessageEnabled(boolean)
│   └── Anvil (static inner class)
│       ├── Sound (static inner class)
│       │   ├── isEnabled() / setEnabled(boolean)
│       │   ├── getVolume() / setVolume(double)
│       │   └── getPitch() / setPitch(double)
│       └── Repair (static inner class)
│           ├── isResetEnabled() / setResetEnabled(boolean)
│           ├── isCostEnabled() / setCostEnabled(boolean)
│           ├── getBaseCost() / setBaseCost(double)
│           ├── getCostMultiplier() / setCostMultiplier(double)
│           └── getEnchantmentCosts() → Map<String, Integer>
└── Shatterment (static inner class)
    ├── isEnabled() / setEnabled(boolean)
    ├── getDisabledWorlds() / setDisabledWorlds(List<World>)
    ├── getEnchantmentStates() / setEnchantmentStates(HashMap<String, EnchantmentStateType>)
    ├── getSplitCount()            → int (default 1)
    ├── Economy (static inner class)  [same shape as Disenchantment.Economy]
    └── Anvil (static inner class)    [same shape as Disenchantment.Anvil]
```

All setters return `boolean` — `true` if the persisted value equals the requested value (a sanity check). Most callers ignore the return value.

---

## ConfigKeys Enum

`types/ConfigKeys.java` — maps symbolic names to YAML dot-paths.

Key groups:

| Prefix | Example Keys | YAML Path prefix |
|--------|-------------|-----------------|
| global | `ENABLED`, `LOCALE`, `LOCALES` | root |
| `LOGGING_*` | `LOGGING_LEVEL`, `LOGGING_SAVE_REPORTS` | `logging.` |
| `EVENT_PRIORITIES_*` | `EVENT_PRIORITIES_DISENCHANT` | `event-priorities.` |
| `DISENCHANTMENT_*` | `DISENCHANTMENT_ENABLED`, `DISENCHANTMENT_REPAIR_COST_BASE` | `disenchantment.` |
| `SHATTERMENT_*` | `SHATTERMENT_ENABLED`, `SHATTERMENT_SPLIT_COUNT` | `shatterment.` |

Each constant has `getKey()` returning the dot-path string.

---

## I18n.java

`config/I18n.java` — pure static class reading from `Disenchantment.localeConfig`.

### Color translation

All strings go through:
```java
private static String translateColors(String text) {
    return LegacyComponentSerializer.legacySection().serialize(
        LegacyComponentSerializer.legacyAmpersand().deserialize(text));
}
```

This converts `&c`, `&l`, etc. color codes to `§` section sign format as expected by `Player.sendMessage()`.

If `text` is null (missing translation key), returns `"> Missing translation <"`.

### Inner class hierarchy

```
I18n
├── getPrefix()
├── Messages (static inner class) — chat messages to players
│   ├── requiresPermission(), invalidArgument(), ...
│   ├── enchantmentIsEnabled(String), enchantmentIsDisabled(String), ...
│   ├── materialIsEnabled(String), materialIsDisabled(String)
│   ├── repairCostIsEnabled(), repairBaseCostIsSet(String cost), ...
│   ├── soundIsEnabled(), soundVolumeIsSet(String volume), ...
│   ├── worldNotFound(String), worldIsEnabled(String), worldIsDisabled(String)
│   └── economyCost(String), economyInsufficientFunds(String), economyCharged(String), ...
├── States (static inner class) — localized state names
│   └── enable(), keep(), delete(), disable()
├── Commands (static inner class) — command output strings
│   ├── Help.title(String page), Help.pages() → List<List<String>> (8 pages)
│   ├── Status.title(), Status.global(String), Status.disenchantment(String), Status.shatterment(String)
│   ├── Enchantments.Disenchantment.*, Enchantments.Shatterment.*
│   ├── Materials.*
│   ├── Repair.Disenchantment.*, Repair.Shatterment.*
│   ├── Sound.Disenchantment.*, Sound.Shatterment.*
│   ├── Worlds.Disenchantment.*, Worlds.Shatterment.*
│   └── Economy.Disenchantment.*, Economy.Shatterment.*
└── GUI (static inner class) — GUI inventory titles and item labels
    ├── back(), previous(), next()
    ├── Navigation.inventory(), Navigation.Plugin.*, Navigation.Worlds.*, ...
    ├── Worlds.inventory(), Worlds.Lore.*, Worlds.Help.*
    ├── Repair.Disenchantment.*, Repair.Shatterment.*
    ├── Enchantments.inventory(), Enchantments.Lore.*, Enchantments.Help.*
    ├── Materials.inventory(), Materials.Lore.*, Materials.Help.*
    ├── Sound.Disenchantment.*, Sound.Shatterment.*
    └── Economy.Disenchantment.*, Economy.Shatterment.*
```

### I18nKeys Enum

`types/I18nKeys.java` — maps symbolic names to locale YAML dot-paths. Same pattern as `ConfigKeys` but for locale files. Each constant has `getKey()`.

---

## ConfigUtils

`utils/ConfigUtils.java`

### `setupConfig()`

1. `plugin.saveDefaultConfig()` — extracts `config.yml` from JAR if not present.
2. Loads the existing `config.yml` from disk as `oldConfig`.
3. Loads the bundled `config.yml` from the JAR as `newConfig` (the template).
4. Calls `ConfigMigrations.apply(plugin, oldConfig, newConfig)` — returns the migrated config.
5. Saves the migrated config back to `config.yml`.
6. Calls `plugin.reloadConfig()` and `Config.invalidateCaches()`.

### `setupLocaleConfigs()`

Iterates `Config.getLocales()` (the `locales` list in config.yml). For each locale code, checks if `plugins/Disenchantment/locales/<locale>.yml` already exists. If it does, skips (preserving custom translations). If not, calls `plugin.saveResource(...)` to extract from the JAR.

---

## Migration System

`config/migration/`

### How it works

Config files have a `migration` integer key (e.g. `migration: 9`). When the plugin loads:
1. `ConfigMigrations.apply()` reads `oldConfig.getInt("migration", 0)` and `newConfig.getInt("migration", 0)`.
2. For each version step `i` from `oldVersion + 1` to `newVersion`, it loads `migrations/<i>.yml` from the JAR, looks up `migrations.get(i)`, and calls `migrate(oldConfig, configTemplate)`.
3. The migrated config is returned and saved.

### `IConfigMigration`

```java
public interface IConfigMigration {
    FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate);
}
```

Each migration step returns the updated `FileConfiguration`. Typically it copies known keys from `oldConfig` into `configTemplate` (which already has the new defaults) using `ConfigUtils.copyKeys(Set<String>, oldConfig, configTemplate)`.

### Registered Migrations

| Step | Class | Purpose |
|------|-------|---------|
| 1 | `Migration1` | Initial migration from pre-versioned configs |
| 2 | `Migration2` | Adds new keys |
| 3 | `Migration3` | Adds new keys |
| 4 | `Migration4` | Adds new keys |
| 5 | `Migration5` | Adds new keys |
| 6 | `Migration6` | Adds new keys |
| 7 | `Migration7` | Adds new keys |
| 8 | `Migration8` | Adds new keys |
| 9 | `Migration9` | Adds economy keys (`disenchantment.economy.*`, `shatterment.economy.*`) |

---

## How to Add a New Config Key

1. Add a constant to `ConfigKeys` enum with the YAML dot-path:
   ```java
   MY_NEW_KEY("disenchantment.my-new-key"),
   ```
2. Add a getter (and optional setter) to the appropriate inner class in `Config.java`:
   ```java
   public static boolean isMyNewKeyEnabled() {
       return config.getBoolean(ConfigKeys.MY_NEW_KEY.getKey(), false);
   }
   ```
3. Add the default value to `core/src/main/resources/config.yml`.
4. Bump the `migration` counter in `config.yml` and create `migrations/<N>.yml` that copies existing keys into the new template.
5. Create `Migration<N>.java` in `config/migration/steps/` and register it in `ConfigMigrations.migrations`.

---

## How to Add a New Migration Step

1. Create `MigrationN.java` in `config/migration/steps/` implementing `IConfigMigration`:
   ```java
   public class MigrationN implements IConfigMigration {
       @Override
       public FileConfiguration migrate(FileConfiguration oldConfig, FileConfiguration configTemplate) {
           Set<String> keys = Set.of(/* existing keys to preserve */);
           ConfigUtils.copyKeys(keys, oldConfig, configTemplate);
           return configTemplate;
       }
   }
   ```
2. Create `core/src/main/resources/migrations/N.yml` containing the full new default config at version N.
3. Register in `ConfigMigrations`:
   ```java
   N, new MigrationN()
   ```
4. Update `migration: N` in `core/src/main/resources/config.yml`.
