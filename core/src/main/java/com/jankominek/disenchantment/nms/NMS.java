package com.jankominek.disenchantment.nms;

import com.jankominek.disenchantment.guis.HeadBuilder;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction layer for version-specific {@code net.minecraft.server} (NMS) operations.
 *
 * <p>Each supported Minecraft version provides its own implementation of this interface,
 * allowing the plugin to interact with enchantments, materials, anvil repair costs,
 * and player head textures in a version-independent way.</p>
 */
public interface NMS {
    /**
     * Checks whether the given item is eligible to receive enchantments.
     *
     * @param item the item to check
     * @return {@code true} if the item can be enchanted, {@code false} otherwise
     */
    default boolean canItemBeEnchanted(ItemStack item) {
        return false;
    }

    /**
     * Returns all enchantments registered on the server, including those added by third-party plugins.
     *
     * @return a list of registered {@link Enchantment} instances
     */
    default List<Enchantment> getRegisteredEnchantments() {
        return new ArrayList<>();
    }

    /**
     * Returns all {@link Material} types available in the current Minecraft version.
     *
     * @return a list of materials
     */
    default List<Material> getMaterials() {
        return new ArrayList<>();
    }

    /**
     * Returns the set of third-party enchantment plugin adapters supported by this NMS version.
     *
     * @return a list of supported plugin adapters
     */
    default List<ISupportedPlugin> getSupportedPlugins() {
        return new ArrayList<>();
    }

    /**
     * Reads the current repair cost from an anvil inventory.
     *
     * @param anvilInventory the anvil inventory to read from
     * @param inventoryView  the player's view of the inventory
     * @return the current repair cost in experience levels
     */
    default int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        return 0;
    }

    /**
     * Sets the repair cost stored on an item's NBT data.
     *
     * @param item       the item whose repair cost should be updated
     * @param repairCost the new repair cost value
     */
    default void setItemRepairCost(ItemStack item, int repairCost) {
    }

    /**
     * Sets the repair cost displayed in an anvil inventory.
     *
     * @param anvilInventory the anvil inventory to update
     * @param inventoryView  the player's view of the inventory
     * @param repairCost     the new repair cost in experience levels
     */
    default void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
    }

    /**
     * Applies a Base64-encoded skin texture to a {@link HeadBuilder} for player head items.
     *
     * @param headBuilder the head builder to modify
     * @param texture     the Base64-encoded texture value
     * @return the modified head builder with the texture applied
     */
    default HeadBuilder setTexture(HeadBuilder headBuilder, String texture) {
        return headBuilder;
    }
}
