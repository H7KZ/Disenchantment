<!-- generated-by: gsd-doc-writer -->
# Adding a New Minecraft Version

This guide walks through every step required to add support for a new Minecraft version. Read [ARCHITECTURE.md](ARCHITECTURE.md) first if you are not already familiar with the NMS abstraction layer. See [SETUP.md](SETUP.md) for environment prerequisites.

## Decision: extend an existing module or create a new one?

Minecraft's internal NMS API changes with some releases but not others. The question is whether the new version's internals are close enough to an existing NMS module.

**Extend an existing module** (just add an enum entry) when:
- The new version is a minor patch of the same major release (e.g. 1.21.12 after 1.21.11).
- The NMS API for anvil operations, enchantment registry, and repair cost has not changed.
- The new version is >= 1.21 and no breaking API changes have been announced — the LATEST fallback already handles it until you add the explicit enum entry.

**Create a new module** when:
- Spigot has changed the internals used by the existing NMS class (compile errors against the new BuildTools JAR).
- A new `R` revision number has been assigned (e.g. a hypothetical `1_22_R1` would follow `1_21_R5`).
- The Bukkit API has changed in a way that requires different code paths (e.g. the `InventoryView` class-to-interface change between 1.20 and 1.21).

When in doubt, try compiling the new version's Spigot JAR against the closest existing module. Compile errors tell you whether a new module is needed.

---

## Case A: extend an existing module (enum entry only)

This is the most common case for patch releases.

### Step 1 — Install the Spigot NMS artifact

Run BuildTools for the new version to install the JAR into your local Maven repository:

```bash
java -jar BuildTools.jar --rev <new-version>
# e.g. java -jar BuildTools.jar --rev 1.21.12
```

This is required even if the existing NMS module will be reused, because `mvn clean package` may attempt to resolve the new version's JAR for verification.

### Step 2 — Add an entry to MinecraftVersion.java

Open `core/src/main/java/com/jankominek/disenchantment/nms/MinecraftVersion.java`.

Find the section for the appropriate NMS module and add the new version entry. **Order matters:** the enum is iterated top-to-bottom and uses substring matching against the server version string. More-specific versions (e.g. `1.21.12`) must appear before less-specific ones (e.g. `1.21`).

The enum entry constructor signature is:
```java
ENUM_CONSTANT((byte) ordinal, "underlined_version", "dotted.version", "nmsModule"),
```

- `ordinal` — a unique byte used for version comparison. Assign the next integer after the current highest-ordinal entry in that module's block. For example, if `MINECRAFT_1_21_11` has ordinal `25`, the new entry gets `(byte) 26` (but check that `26` is not already used by a 26.x.x entry further up the file).
- `underlined_version` — the version string with underscores: `"1_21_12"`.
- `dotted_version` — the version string with dots: `"1.21.12"`. This is what gets matched against the server version string.
- `nmsModule` — the NMS module string: `"v1_21_R5"` (or whichever module handles this version).

**Example** — adding 1.21.12 as a `v1_21_R5` version:

```java
// In the // 1_21_R5 section, above MINECRAFT_1_21_11:
MINECRAFT_1_21_12((byte) 26, "1_21_12", "1.21.12", "v1_21_R5"),
MINECRAFT_1_21_11((byte) 25, "1_21_11", "1.21.11", "v1_21_R5"),
MINECRAFT_1_21_10((byte) 25, "1_21_10", "1.21.10", "v1_21_R5"),
// ...
```

Wait — ordinal `26` is already used by `MINECRAFT_26_1`. Check the file carefully. The byte ordinal just needs to be unique across all entries; you can use any unused byte value. The convention is to assign ordinals sequentially within a version family.

> **Tip:** Scan the entire enum for the ordinal value you intend to use before assigning it. Two entries with the same ordinal value will cause incorrect `currentVersionOlderThan` / `currentVersionNewerThan` results.

That is all that is required for Case A. Rebuild and test.

---

## Case B: create a new NMS module

Follow all the steps below when the new version requires a new NMS implementation.

### Step 1 — Install the Spigot NMS artifact

```bash
java -jar BuildTools.jar --rev <new-version>
```

This installs `org.spigotmc:spigot:<new-version>-R0.1-SNAPSHOT:remapped-mojang` into `~/.m2`.

### Step 2 — Create the module directory structure

```
v{X}_{Y}_R{Z}/
├── pom.xml
├── libs/
│   ├── AdvancedEnchantments-X.X.X.jar
│   ├── EnchantsSquared-X.X.X.jar
│   ├── UberEnchant-X.X.X.jar
│   ├── ExcellentEnchants-X.X.X.jar   (if supported for this version)
│   ├── zenchantments-X.X.X.jar        (if supported for this version)
│   └── vane-core-X.X.X.jar            (if supported for this version)
└── src/
    └── main/
        └── java/
            └── com/jankominek/disenchantment/nms/
                └── NMS_v{X}_{Y}_R{Z}.java
            └── plugins/
                ├── AdvancedEnchantments_v{X}_{Y}_R{Z}.java
                ├── EcoEnchants_v{X}_{Y}_R{Z}.java
                ├── EnchantsSquared_v{X}_{Y}_R{Z}.java
                ├── UberEnchant_v{X}_{Y}_R{Z}.java
                ├── ExcellentEnchants_v{X}_{Y}_R{Z}.java
                ├── Vane_v{X}_{Y}_R{Z}.java
                └── Zenchantments_v{X}_{Y}_R{Z}.java
```

### Step 3 — Create the module pom.xml

Copy the closest existing module's `pom.xml` as a starting point. At minimum change:

```xml
<artifactId>disenchantment-v{X}_{Y}_R{Z}</artifactId>

<dependencies>
    <dependency>
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot</artifactId>
        <version>{new-version}-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
        <classifier>remapped-mojang</classifier>
    </dependency>
    <dependency>
        <groupId>com.jankominek</groupId>
        <artifactId>disenchantment-core</artifactId>
        <version>${revision}</version>
    </dependency>
    <!-- third-party JARs — copy from closest existing module and update versions/paths -->
</dependencies>
```

If the new module requires Java 21 (all modules except `v1_18_R1` do), ensure the compiler plugin is set to `<source>21</source> <target>21</target>`. Copy the `<build>` section from `v1_21_R5/pom.xml`.

### Step 4 — Implement NMS_v{X}_{Y}_R{Z}.java

Create the NMS implementation class. Use the closest existing implementation as a template. The class must:

- Be in package `com.jankominek.disenchantment.nms`.
- Be named exactly `NMS_v{X}_{Y}_R{Z}` — `NMSMapper` resolves it by this naming convention.
- Implement the `NMS` interface from `core/`.

Skeleton:

```java
package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.guis.HeadBuilder;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NMS_v{X}_{Y}_R{Z} implements NMS {

    @Override
    public boolean canItemBeEnchanted(ItemStack item) {
        // Check using enchantments that can apply to any enchantable item.
        // v1_21_R1+ uses Enchantment.BINDING_CURSE, UNBREAKING, MENDING.
        // v1_18_R1 uses Enchantment.BINDING_CURSE, DURABILITY, MENDING.
        Enchantment[] checkers = {
                Enchantment.BINDING_CURSE,
                Enchantment.UNBREAKING,
                Enchantment.MENDING
        };
        return Arrays.stream(checkers).anyMatch(e -> e.canEnchantItem(item));
    }

    @Override
    public List<Enchantment> getRegisteredEnchantments() {
        // v1_21_R1+ can use Registry.ENCHANTMENT
        return new ArrayList<>(Registry.ENCHANTMENT.stream().toList());
    }

    @Override
    public List<Material> getMaterials() {
        // v1_21_R1+ can use Registry.MATERIAL
        return new ArrayList<>(Registry.MATERIAL.stream().toList());
    }

    @Override
    public List<ISupportedPlugin> getSupportedPlugins() {
        return new ArrayList<>() {{
            add(new plugins.AdvancedEnchantments_v{X}_{Y}_R{Z}());
            add(new plugins.EcoEnchants_v{X}_{Y}_R{Z}());
            add(new plugins.EnchantsSquared_v{X}_{Y}_R{Z}());
            add(new plugins.ExcellentEnchants_v{X}_{Y}_R{Z}());
            add(new plugins.UberEnchant_v{X}_{Y}_R{Z}());
            add(new plugins.Vane_v{X}_{Y}_R{Z}());
            add(new plugins.Zenchantments_v{X}_{Y}_R{Z}());
        }};
    }

    @Override
    public int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        // v1_21_R1+: cast inventoryView to AnvilView for the typed API
        if (inventoryView instanceof AnvilView anvilView) {
            return anvilView.getRepairCost();
        }
        return anvilInventory.getRepairCost();
    }

    @Override
    public void setItemRepairCost(ItemStack item, int repairCost) {
        // v1_21_R1+: use Repairable meta — no NBT needed
        if (item.getItemMeta() instanceof Repairable repairable) {
            repairable.setRepairCost(repairCost);
            item.setItemMeta((org.bukkit.inventory.meta.ItemMeta) repairable);
        }
    }

    @Override
    public void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
        if (inventoryView instanceof AnvilView anvilView) {
            anvilView.setRepairCost(repairCost);
            return;
        }
        anvilInventory.setRepairCost(repairCost);
    }

    @Override
    public HeadBuilder setTexture(HeadBuilder headBuilder, String texture) {
        // v1_21_R1+: use PlayerProfile / PlayerTextures API
        // Copy implementation from NMS_v1_21_R5.java
        return headBuilder;
    }
}
```

**NBT note:** `v1_18_R1` and `v1_20_R4` use a custom `nbt/` package to manipulate repair cost via NBT because the Bukkit `Repairable` meta API was not reliable in those versions. Modules `v1_21_R1` and later use `Repairable` directly and do not have an `nbt/` package.

### Step 5 — Create plugin adapters

Copy the adapter classes from the closest existing module into the new module's `plugins/` package. Rename each class to `<PluginName>_v{X}_{Y}_R{Z}`. Test compilation — if the adapter API from a third-party plugin has changed, update the method calls accordingly.

Copy the latest third-party JARs into the new module's `libs/` directory and update the `systemPath` references in `pom.xml`.

### Step 6 — Add the MinecraftVersion enum entry

Follow the same instructions as Case A, Step 2. Map the new version to the new NMS module string.

### Step 7 — Register the module in the build

**Root pom.xml** — add the new module to `<modules>`:

```xml
<modules>
    <module>core</module>
    <module>v{X}_{Y}_R{Z}</module>   <!-- add this -->
    <module>v1_21_R5</module>
    <module>v1_21_R4</module>
    <module>v1_21_R1</module>
    <module>v1_20_R4</module>
    <module>v1_18_R1</module>
    <module>dist</module>
</modules>
```

**dist/pom.xml** — add a dependency so the shading module includes the new module's classes:

```xml
<dependency>
    <groupId>com.jankominek</groupId>
    <artifactId>disenchantment-v{X}_{Y}_R{Z}</artifactId>
    <version>${revision}</version>
</dependency>
```

### Step 8 — Update the LATEST fallback if needed

If the new module should become the fallback for all unknown future versions, update `MinecraftVersion.java`:

```java
LATEST((byte) 127, null, null, "v{X}_{Y}_R{Z}"),  // was "v1_21_R5"
```

The LATEST fallback is used by the detection logic when the server version is >= 1.21 (minor) or >= 2 (major) and does not match any named entry. Only change LATEST to a newer module when that module is stable and intended to handle unknown future versions.

### Step 9 — Build and test

```bash
mvn clean package
```

Copy `target/Disenchantment-<version>.jar` to a test server running the new Minecraft version and verify:

1. The startup log shows the correct NMS module: `[Disenchantment] NMS module: NMS_v{X}_{Y}_R{Z}`.
2. Disenchanting works — place an enchanted item and a book in an anvil; verify the enchanted book result and correct XP cost.
3. Shattering works — place a multi-enchantment book and a blank book in an anvil; verify the split.
4. If third-party enchantment plugins are available, test their enchantments are transferred correctly.

---

## LATEST fallback: how unknown versions are handled

The `MinecraftVersion.init()` method uses substring matching to find a named entry for the server version string. When no match is found, it falls back to this regex:

```java
Matcher m = Pattern.compile("MC: (\\d+)\\.(\\d+)").matcher(serverVersionString);
if (m.find()) {
    int major = Integer.parseInt(m.group(1));
    int minor = Integer.parseInt(m.group(2));
    if (major >= 2 || minor >= 21) return LATEST;
}
```

This means any version with a minor number >= 21 (or a major number >= 2, which covers the new 26.x.x scheme) automatically uses the LATEST module. This gives full backward compatibility for future patch releases without requiring a code change — you only need to add the explicit enum entry if you want accurate version reporting in the logs or need to branch behaviour based on the exact version using `currentVersionOlderThan` / `currentVersionNewerThan`.

For versions below 1.18, the fallback returns `INCOMPATIBLE`, which causes `NMSMapper.setup()` to return `null` and the plugin to disable itself with a descriptive console message.

---

## Summary checklist

### Case A (enum entry only)

- [ ] BuildTools run for the new version
- [ ] `MinecraftVersion.java` entry added with correct ordinal, strings, and nmsModule
- [ ] Build passes (`mvn clean package`)
- [ ] Tested on a server running the new version

### Case B (new NMS module)

- [ ] BuildTools run for the new version
- [ ] New module directory created with correct structure
- [ ] `pom.xml` created with correct Spigot dependency version
- [ ] `NMS_v{X}_{Y}_R{Z}.java` implemented
- [ ] Plugin adapter classes created in `plugins/`
- [ ] Third-party JARs copied to `libs/`
- [ ] `MinecraftVersion.java` entry added
- [ ] New module added to root `pom.xml` `<modules>`
- [ ] New module added to `dist/pom.xml` dependencies
- [ ] LATEST fallback updated if this is the new latest module
- [ ] Build passes (`mvn clean package`)
- [ ] Tested on a server running the new version (disenchant, shatter, adapters)
