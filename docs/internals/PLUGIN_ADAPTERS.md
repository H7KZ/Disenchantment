<!-- generated-by: gsd-doc-writer -->

# Plugin Adapter System

`core/src/main/java/com/jankominek/disenchantment/plugins/`

The adapter system allows Disenchantment to read and manipulate enchantments added by third-party enchantment plugins.
Each supported plugin gets a dedicated adapter class in the `plugins/` subpackage of the relevant NMS module(s).

---

## `ISupportedPlugin`

`plugins/ISupportedPlugin.java`

```java
public interface ISupportedPlugin {

    String getName();
    // The plugin name exactly as registered on the server (matches Bukkit.getPluginManager().getPlugin(name)).
    // Used by SupportedPluginManager to detect if the plugin is installed.

    default List<IPluginEnchantment> getItemEnchantments(ItemStack item, World world) {
        return getItemEnchantments(item);
    }
    // Returns all enchantments from this plugin present on the item.
    // World parameter available for plugins that have per-world enchantment registries.
    // Defaults to calling the world-less overload.

    List<IPluginEnchantment> getItemEnchantments(ItemStack item);
    // World-less overload. Must be implemented.

    default void activate() {}
    // Called once when the adapter is activated at startup.
    // Use for any one-time initialization (caching API references, etc.).
}
```

---

## `IPluginEnchantment`

`plugins/IPluginEnchantment.java`

The common wrapper for a single enchantment instance, regardless of whether it comes from vanilla Bukkit or a
third-party plugin.

```java
public interface IPluginEnchantment {

    String getKey();
    // Unique enchantment key. For vanilla: the NamespacedKey string (e.g. "minecraft:mending").
    // For third-party: the plugin-specific ID or key.
    // Used as the map key in enchantment state lookups and deduplication.

    int getLevel();
    // Enchantment level as stored on the item.

    default ItemStack addToBook(ItemStack book) { return addToItem(book); }
    // Applies this enchantment to an enchanted book (for the disenchant result).
    // Default delegates to addToItem().

    default ItemStack removeFromBook(ItemStack book) { return removeFromItem(book); }
    // Removes this enchantment from a book.
    // Default delegates to removeFromItem().

    default ItemStack addToItem(ItemStack item) { return item; }
    // Adds this enchantment to an arbitrary item.
    // Must be overridden by adapters — the default is a no-op.

    default ItemStack removeFromItem(ItemStack item) { return item; }
    // Removes this enchantment from an arbitrary item.
    // Must be overridden by adapters — the default is a no-op.
}
```

For vanilla enchantments, `EnchantmentUtils` creates `IPluginEnchantment` wrappers that call standard Bukkit
`EnchantmentStorageMeta.addStoredEnchant()` / `removeStoredEnchant()` for books, and `ItemMeta.addEnchant()` /
`removeEnchant()` for regular items.

---

## `SupportedPluginManager`

`plugins/SupportedPluginManager.java`

Manages the lifecycle of all adapters. Holds a static `List<ISupportedPlugin> activatedPlugins`.

### Key methods

```java
public static void activatePlugins(List<String> plugins)
```

Called once during `Disenchantment.enable()` with the names of all currently loaded plugins.

- Gets the supported list from `nms.getSupportedPlugins()`.
- For each name in `plugins`, looks up a matching adapter by `getSupportedPluginByName(name)`.
- If found, adds to `activatedPlugins` and calls `adapter.activate()`.

```java
public static List<ISupportedPlugin> getAllActivatedPlugins()
```

Returns the list of adapters whose server-side plugin is installed and running.

```java
public static List<ISupportedPlugin> getAllSupportedPlugins()
```

Returns all adapters available in the current NMS module (from `nms.getSupportedPlugins()`), regardless of whether they
are installed.

```java
public static void deactivateAllPlugins()
```

Clears `activatedPlugins`. Called during `disable()`.

### How adapters are used in event handlers

`AnvilEventGuards.collectEnchantments()` checks `SupportedPluginManager.getAllActivatedPlugins()`:

- If empty: calls the vanilla collector (uses `EnchantmentUtils.getItemEnchantments()`).
- If non-empty: calls the plugin-specific collector for each adapter. Each adapter provides enchantments from its own
  registry.

After collection, enchantments are deduplicated by key (first occurrence wins) to prevent double-charging when both a
vanilla wrapper and a plugin wrapper claim the same key.

---

## Supported Plugins Per NMS Module

| Plugin               | v1_18_R1 | v1_20_R4 | v1_21_R1 | v1_21_R4 | v1_21_R5 |
|----------------------|----------|----------|----------|----------|----------|
| AdvancedEnchantments | Yes      | Yes      | Yes      | Yes      | Yes      |
| EcoEnchants          | Yes      | Yes      | Yes      | Yes      | Yes      |
| EnchantsSquared      | Yes      | Yes      | Yes      | Yes      | Yes      |
| UberEnchant          | Yes      | Yes      | Yes      | Yes      | Yes      |
| ExcellentEnchants    | No       | No       | Yes      | Yes      | Yes      |
| Vane                 | No       | No       | Yes      | Yes      | Yes      |
| Zenchantments        | No       | No       | Yes      | Yes      | Yes      |

**Note on EcoEnchants:** Requires a patched build of EcoEnchants. The standard release has API incompatibilities with
anvil events.

---

## Adapter Class Naming Convention

Each adapter is in the NMS module's `plugins/` subpackage:

```
v1_21_R1/src/main/java/com/jankominek/disenchantment/nms/plugins/
    AdvancedEnchantments_v1_21_R1.java
    EcoEnchants_v1_21_R1.java
    EnchantsSquared_v1_21_R1.java
    ExcellentEnchants_v1_21_R1.java
    UberEnchant_v1_21_R1.java
    Vane_v1_21_R1.java
    Zenchantments_v1_21_R1.java
```

Note: even though these classes are in `com.jankominek.disenchantment.nms.plugins`, they are referenced using
unqualified names (`new plugins.AdvancedEnchantments_v1_21_R1()`) from within the same package in the NMS
implementation.

---

## Third-Party JARs

Each module that supports a plugin must have the plugin's JAR in `<module>/libs/`:

```
v1_21_R1/libs/
    AdvancedEnchantments.jar
    EcoEnchants.jar
    ...
```

These are declared as `system`-scope Maven dependencies in the module's `pom.xml`:

```xml
<dependency>
    <groupId>com.ssomar</groupId>
    <artifactId>advancedenchantments</artifactId>
    <version>1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/AdvancedEnchantments.jar</systemPath>
</dependency>
```

System-scope dependencies are included at compile time but **not** bundled in the output JAR — they are only present on
the server as separate plugins.

---

## PlaceholderAPI Integration

`plugins/placeholderapi/DisenchantmentPlaceholderExpansion.java`

Registered in `Disenchantment.enable()` if PlaceholderAPI is detected:

```java
if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
    new DisenchantmentPlaceholderExpansion().register();
}
```

Extends PAPI's `PlaceholderExpansion`. Provides plugin-specific placeholders (e.g. for use in scoreboards or chat
formatting plugins).

---

## How to Implement a New Plugin Adapter

1. Obtain the plugin's JAR. Place it in `<target-module>/libs/<PluginName>.jar`.

2. Add a system-scope Maven dependency in `<target-module>/pom.xml`.

3. Create the adapter class in `<target-module>/src/main/java/com/jankominek/disenchantment/nms/plugins/`:
   ```java
   public class MyPlugin_v1_21_R1 implements ISupportedPlugin {

       @Override
       public String getName() {
           return "MyPlugin";  // exact plugin name as registered with Bukkit
       }

       @Override
       public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
           List<IPluginEnchantment> result = new ArrayList<>();
           // Use MyPlugin's API to get enchantments from the item
           for (MyPluginEnchantment enc : MyPluginAPI.getEnchantments(item)) {
               result.add(new IPluginEnchantment() {
                   public String getKey() { return enc.getId(); }
                   public int getLevel() { return enc.getLevel(); }
                   public ItemStack addToItem(ItemStack item) {
                       return MyPluginAPI.addEnchantment(item, enc);
                   }
                   public ItemStack removeFromItem(ItemStack item) {
                       return MyPluginAPI.removeEnchantment(item, enc);
                   }
               });
           }
           return result;
       }
   }
   ```

4. Register the adapter in the target NMS module's `getSupportedPlugins()`:
   ```java
   // In NMS_v1_21_R1.getSupportedPlugins():
   add(new plugins.MyPlugin_v1_21_R1());
   ```

5. If you need to support all NMS modules, repeat steps 1–4 for each module. The adapter class can often be identical;
   only the JAR reference and class name suffix change.

6. If the plugin needs special handling around the `EnchantsSquared` 1-tick slot-replacement timing issue, it will be
   handled automatically by `AnvilEventGuards.scheduleSecondItemRemoval()` which already uses a 2-tick delay.
