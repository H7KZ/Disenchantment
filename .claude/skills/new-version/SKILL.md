---
name: new-version
description: Step-by-step checklist for adding support for a new Minecraft version to Disenchantment.
disable-model-invocation: true
argument-hint: [minecraft-version e.g. 1.21.12]
---

Add Minecraft version $ARGUMENTS support to the Disenchantment plugin.

Work through each step in order. After each step, confirm it is complete before moving to the next.

## Step 1: Determine NMS module mapping

Read `core/src/main/java/com/jankominek/disenchantment/nms/MinecraftVersion.java`.

Determine whether $ARGUMENTS:

- Maps to an **existing** NMS module (version falls within an existing range — most common for patch versions)
- Requires a **new** NMS module (major API changes between versions)

For versions >= 1.21 with no explicit mapping, LATEST (v1_21_R5) is the automatic fallback. Adding an enum entry is
still required.

## Step 2: Update MinecraftVersion.java

Read the current file, then add the new version entry:

- Use the correct NMS_v*_R* class reference
- Place it in version order within the enum
- Match the format of existing entries exactly

## Step 3: Update NMS module (if mapping to existing module)

Read the existing `NMS_v*_R*.java` for the target module.

Check if any Bukkit/Spigot API calls need updating for $ARGUMENTS. Common changes between MC versions:

- Enchantment registry access patterns
- ItemMeta / EnchantmentMeta API changes
- PersistentDataContainer API updates

## Step 4: Create new NMS module (only if new module required)

If a new module is needed:

1. Copy the nearest existing module directory as a template
2. Update the Maven `artifactId` in the new module's `pom.xml`
3. Update the Java version if required (1.18 = Java 17, 1.21+ = Java 21)
4. Update the `NMS_v*_R*.java` class name to match the new module
5. Update package names throughout
6. Port all plugin adapters, updating any changed API calls
7. Place third-party plugin JARs for the new MC version in `libs/`
8. Add the new module to root `pom.xml` `<modules>`
9. Add the new module as a dependency in `dist/pom.xml`

## Step 5: Run BuildTools (reminder)

BuildTools must be run manually outside Claude Code:

```bash
java -jar BuildTools.jar --rev $ARGUMENTS
```

Confirm BuildTools has been run for $ARGUMENTS before attempting to build.

## Step 6: Verify build

Run `/build` to execute `mvn clean package` and confirm the build passes.

## Step 7: Checklist confirmation

Confirm all items are complete:

- [ ] MinecraftVersion.java updated with new version entry
- [ ] NMS module implementation handles the new version correctly
- [ ] All plugin adapters are present and up to date
- [ ] Third-party JARs in `libs/` match the new MC version
- [ ] Build passes with `mvn clean package`
- [ ] (If new module) root pom.xml and dist/pom.xml updated
