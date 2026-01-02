# Frequently Asked Questions

This FAQ addresses common issues and questions regarding the Disenchantment plugin

-----

### Why can't players who aren't server operators (non-ops) use the plugin?

This is intended behavior. To allow all players to use the disenchantment features, you must grant them specific
permissions using a permissions management plugin

1. **Install a permissions plugin:** We recommend using a plugin like [LuckPerms](https://luckperms.net/) to manage
   player permissions
2. **Assign permissions:** Grant the necessary permission nodes to your players or groups. You can find a complete list
   of available permissions here:
    * **[Official Permissions List](https://github.com/H7KZ/Disenchantment/blob/master/PERMISSIONS.md)**

For example, to allow a player to use the basic disenchantment feature, you would grant them the `disenchantment.use`
permission

-----

### Why isn't Disenchantment working with EcoEnchants? The book disappears in the anvil.

This is a known compatibility issue caused by how **EcoEnchants** handles anvil events, which conflicts with *
*Disenchantment**. The enchanted book appears in the anvil for a moment before vanishing

To fix this, you need to update **Disenchantment** and use a modified version of **EcoEnchants**. You have two options
to get the corrected file

**Easiest Solution (Recommended)**

This is the simplest method and works for most users

1. Update the **Disenchantment** plugin to version **`6.2.2`** or newer
2. Download the pre-compiled, fixed version of **EcoEnchants** directly from the Disenchantment release page. Look for
   the file named `EcoEnchants.v12.24.0.jar`
    * *
      *[Download from the v6.2.2 Release Page](https://www.google.com/search?q=https://github.com/H7KZ/Disenchantment/releases/tag/v6.2.2)
      **

You must use both the updated Disenchantment plugin and the provided EcoEnchants `.jar` file for the fix to work

**Alternative for Advanced Users 🔧**

If you're comfortable with compiling Java projects, you can build the plugin yourself instead of downloading the
pre-compiled file

1. Make sure your **Disenchantment** plugin is updated to **`v6.2.2`** or newer
2. Clone or download the **EcoEnchants** source code
3. Incorporate the changes from the official pull request that contains the fix:
    * **[View Pull Request \#417 on GitHub](https://github.com/Auxilor/EcoEnchants/pull/417)**
4. Compile the plugin to create your own `.jar` file

-----

### Why aren't UberEnchants working with Disenchantment?

This was a confirmed bug caused by a conflict in event priorities between the Disenchantment and UberEnchants plugins

This issue was fixed in version **`v6.1.8`** of Disenchantment. Please update the plugin to the latest version to
resolve the problem

* **[Download Latest Release](https://www.google.com/search?q=https://github.com/H7KZ/Disenchantment/releases/latest)**

-----

### I'm using GeyserMC and the plugin has issues. Is this a known problem?

Yes, servers running GeyserMC can sometimes experience issues with plugins that modify anvil mechanics, especially for
players connecting from Bedrock Edition. This is often caused by Geyser itself rather than a bug in Disenchantment

While a guaranteed fix is not available, you can try installing one of the following plugins, which are designed to
patch anvil-related issues on Geyser servers:

* [Geyser-Anvil-Fix](https://github.com/ssquadteam/Geyser-Anvil-Fix)
* [CustomAnvilGUI](https://www.spigotmc.org/resources/customanvilgui.116411/)
* [Geyser Recipe Fix](https://modrinth.com/plugin/geyser-recipe-fix)

**Note:** As reported by some users, these fixes may not work in all cases
