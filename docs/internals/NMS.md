<!-- generated-by: gsd-doc-writer -->

# NMS Abstraction System

`core/src/main/java/com/jankominek/disenchantment/nms/`

The NMS (Net Minecraft Server) layer isolates all version-specific Minecraft internals behind a single interface. This
allows the plugin core to compile against Spigot API only, with each NMS module compiled against the specific server
version it targets.

---

## `NMS` Interface

`nms/NMS.java`

All methods have default no-op implementations (returning empty lists, `0`, or the builder unchanged). This means
partial NMS implementations compile without errors — useful if a specific method is not needed for a version.

```java
public interface NMS {

    default boolean canItemBeEnchanted(ItemStack item)
    // Returns true if the item can receive enchantments.
    // Used to validate the source item before collecting enchantments.
    // v1_21_R1 checks BINDING_CURSE, UNBREAKING, and MENDING canEnchantItem().

    default List<Enchantment> getRegisteredEnchantments()
    // Returns all enchantments known to the server, including third-party ones
    // (via Bukkit Registry.ENCHANTMENT or Enchantment.values() depending on version).
    // Used by EnchantmentsGUI to populate the enchantment list.

    default List<Material> getMaterials()
    // Returns all Material values available in this version.
    // Used by MaterialsGUI to populate the materials list.

    default List<ISupportedPlugin> getSupportedPlugins()
    // Returns adapters for all third-party enchantment plugins supported in this NMS module.
    // Instantiates adapter classes from the module's plugins/ package.

    default int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView)
    // Reads the current anvil repair cost.
    // Pre-1.21: reads from AnvilInventory directly.
    // 1.21+: casts InventoryView to AnvilView and calls getRepairCost().

    default void setItemRepairCost(ItemStack item, int repairCost)
    // Sets the repair cost NBT on an individual item.
    // Pre-1.21: edits NBT directly via the module's nbt/ package.
    // 1.21+: casts ItemMeta to Repairable and calls setRepairCost().

    default void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost)
    // Sets the cost displayed in the anvil UI.
    // Pre-1.21: uses NMS field access via the nbt/ package.
    // 1.21+: casts InventoryView to AnvilView and calls setRepairCost().

    default HeadBuilder setTexture(HeadBuilder headBuilder, String texture)
    // Applies a Base64-encoded skin texture to a HeadBuilder for player head GUI items.
    // Implementation varies per version due to Profile API changes.
    // Returns the modified headBuilder.
}
```

---

## `NMSMapper`

`nms/NMSMapper.java`

### `NMS setup()`

1. Calls `MinecraftVersion.getServerVersion()` to get the detected version enum constant.
2. Calls `version.getNmsVersion()` — returns a string like `"v1_21_R1"` or `null` for INCOMPATIBLE.
3. If null, logs and returns null. Caller (`Disenchantment.enable()`) will disable the plugin.
4. Constructs class name: `"com.jankominek.disenchantment.nms.NMS_" + nmsVersion`.
5. Loads with `Class.forName(className)` — this resolves at runtime from the shaded JAR which contains all NMS modules.
6. Verifies `NMS.class.isAssignableFrom(clazz)`.
7. Instantiates with `clazz.getDeclaredConstructor().newInstance()` and returns the `NMS` instance.
8. Any exception logs the error and returns null.

### `boolean hasNMS()`

Same class resolution without instantiation. Used by `DiagnosticUtils` to include NMS status in reports.

---

## `MinecraftVersion` Enum

`nms/MinecraftVersion.java`

Maps every supported Minecraft release to its NMS module.

### Enum constants (selected)

| Constant            | Byte value | versionDotted | nmsVersion |
|---------------------|------------|---------------|------------|
| `LATEST`            | 127        | null          | `v1_21_R5` |
| `MINECRAFT_26_1_3`  | 29         | `26.1.3`      | `v1_21_R5` |
| `MINECRAFT_26_1_2`  | 28         | `26.1.2`      | `v1_21_R5` |
| `MINECRAFT_26_1_1`  | 27         | `26.1.1`      | `v1_21_R5` |
| `MINECRAFT_26_1`    | 26         | `26.1`        | `v1_21_R5` |
| `MINECRAFT_1_21_11` | 25         | `1.21.11`     | `v1_21_R5` |
| `MINECRAFT_1_21_10` | 25         | `1.21.10`     | `v1_21_R5` |
| `MINECRAFT_1_21_9`  | 25         | `1.21.9`      | `v1_21_R5` |
| `MINECRAFT_1_21_8`  | 24         | `1.21.8`      | `v1_21_R5` |
| `MINECRAFT_1_21_7`  | 23         | `1.21.7`      | `v1_21_R4` |
| `MINECRAFT_1_21_6`  | 22         | `1.21.6`      | `v1_21_R4` |
| `MINECRAFT_1_21_5`  | 21         | `1.21.5`      | `v1_21_R4` |
| `MINECRAFT_1_21_4`  | 20         | `1.21.4`      | `v1_21_R1` |
| `MINECRAFT_1_21_3`  | 19         | `1.21.3`      | `v1_21_R1` |
| `MINECRAFT_1_21_2`  | 18         | `1.21.2`      | `v1_21_R1` |
| `MINECRAFT_1_21_1`  | 17         | `1.21.1`      | `v1_21_R1` |
| `MINECRAFT_1_21`    | 16         | `1.21`        | `v1_21_R1` |
| `MINECRAFT_1_20_6`  | 15         | `1.20.6`      | `v1_20_R4` |
| `MINECRAFT_1_20_5`  | 14         | `1.20.5`      | `v1_20_R4` |
| `MINECRAFT_1_20_4`  | 13         | `1.20.4`      | `v1_18_R1` |
| `MINECRAFT_1_20_3`  | 12         | `1.20.3`      | `v1_18_R1` |
| ...                 | ...        | ...           | `v1_18_R1` |
| `MINECRAFT_1_18`    | 1          | `1.18`        | `v1_18_R1` |
| `INCOMPATIBLE`      | -1         | null          | null       |

The `value` (byte) field is used only for `currentVersionOlderThan()` / `currentVersionNewerThan()` comparisons.

### Version Detection Algorithm (`init()`)

The static field `serverVersion` is initialized once via `init()` at class-load time (which happens during
`NMSMapper.setup()` called from `Disenchantment.enable()`).

`init()` reads `Disenchantment.plugin.getServer().getVersion()` — a string like `"git-Paper-406 (MC: 1.21.4)"`.

**Step 1 — exact substring match:**

```java
for (MinecraftVersion version : MinecraftVersion.values()) {
    if (version.versionDotted == null) continue;
    if (v.contains(version.versionDotted)) return version;
}
```

Enum values are ordered most-specific first (`1.21.11` before `1.21.1` before `1.21`), so the first match is always the
most precise.

**Step 2 — regex extraction for unknown future versions:**

```java
Matcher m = Pattern.compile("MC: (\\d+)\\.(\\d+)").matcher(v);
if (m.find()) {
    int major = Integer.parseInt(m.group(1));
    int minor = Integer.parseInt(m.group(2));
    if (major >= 2 || minor >= 21) return LATEST;
}
```

Handles strings like `"MC: 26.1.1"` (major version ≥ 2) or `"MC: 1.21.x"` (minor ≥ 21).

**Step 3 — fallback dot-split:**

```java
int minorVersion = Integer.parseInt(v.split("\\.")[1]);
if (minorVersion >= 21) return LATEST;
```

**Step 4 — fallback underscore-split:**

```java
int minorVersion = Integer.parseInt(v.split("_")[1]);
if (minorVersion >= 21) return LATEST;
```

**Step 5 — INCOMPATIBLE** if all above fail (versions < 1.18).

### Utility Methods

```java
public static boolean currentVersionOlderThan(MinecraftVersion version)
// Returns true if serverVersion.value <= version.value.
// Returns false if serverVersion is INCOMPATIBLE.

public static boolean currentVersionNewerThan(MinecraftVersion version)
// Returns true if serverVersion.value >= version.value.
// Returns false if serverVersion is INCOMPATIBLE.

public static MinecraftVersion getServerVersion()
// Returns the cached serverVersion detected at class-load.

public String getVersionString()
// Returns the underscore form, e.g. "1_21_4", or null for LATEST/INCOMPATIBLE.

public String getNmsVersion()
// Returns the NMS module identifier, e.g. "v1_21_R1", or null for INCOMPATIBLE.
```

---

## Per-Module Differences

### `v1_18_R1` — Minecraft 1.18 to 1.20.4

- Uses the `nbt/` subpackage for direct NBT manipulation via reflection.
- `setItemRepairCost()` and `setAnvilRepairCost()` write NBT directly to the item/container NMS object.
- `getRegisteredEnchantments()` uses `Enchantment.values()`.
- Supported plugins: AdvancedEnchantments, EcoEnchants, EnchantsSquared, UberEnchant.

### `v1_20_R4` — Minecraft 1.20.5 to 1.20.6

- Same NBT approach as v1_18_R1.
- Intermediate version that handles the 1.20.5 data component system changes.
- Supported plugins: AdvancedEnchantments, EcoEnchants, EnchantsSquared, UberEnchant.

### `v1_21_R1` — Minecraft 1.21 to 1.21.4

- **No NBT package** — uses Bukkit API directly.
- `setItemRepairCost()` casts `ItemMeta` to `Repairable` and calls `setRepairCost(0)`.
- `setAnvilRepairCost()` casts `InventoryView` to `AnvilView` (new Paper/Bukkit interface in 1.21).
- `getRepairCost()` also uses `AnvilView.getRepairCost()`.
- `setTexture()` uses `getServer().createPlayerProfile()` + `PlayerTextures.setSkin(URL)`.
- `getRegisteredEnchantments()` uses `Registry.ENCHANTMENT.stream()`.
- `getMaterials()` uses `Registry.MATERIAL.stream()`.
- Supported plugins: AdvancedEnchantments, EcoEnchants, EnchantsSquared, ExcellentEnchants, UberEnchant, Vane,
  Zenchantments.

### `v1_21_R4` — Minecraft 1.21.5 to 1.21.7

- Same Bukkit API approach as v1_21_R1.
- Handles changes in 1.21.5 (data component API evolution).
- Supported plugins: AdvancedEnchantments, EcoEnchants, EnchantsSquared, ExcellentEnchants, UberEnchant, Vane,
  Zenchantments.

### `v1_21_R5` — Minecraft 1.21.8+ and LATEST fallback

- Same Bukkit API approach as v1_21_R1.
- This module is also used for the `LATEST` fallback enum, covering unknown future versions ≥ 1.21.8 and major version ≥
  2 (e.g. 26.x.x).
- Supported plugins: same as v1_21_R1/v1_21_R4.

---

## How NMS Modules Are Loaded

All NMS modules are shaded into the single output JAR by the `dist/` Maven module using `maven-shade-plugin`. At
runtime, `Class.forName("com.jankominek.disenchantment.nms.NMS_v1_21_R1")` succeeds because the class is present in the
shaded JAR.

Each NMS module has its own `src/main/java/com/jankominek/disenchantment/nms/NMS_v*.java` file plus a `plugins/`
sub-package for its adapters. All share the same package namespace `com.jankominek.disenchantment.nms` — they coexist in
the shaded JAR without conflicts because they have different class names.

See [PLUGIN_ADAPTERS.md](PLUGIN_ADAPTERS.md) for the adapter classes in each module's `plugins/` package.
