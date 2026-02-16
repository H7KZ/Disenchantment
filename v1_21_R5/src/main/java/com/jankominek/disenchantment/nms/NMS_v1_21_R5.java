package com.jankominek.disenchantment.nms;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jankominek.disenchantment.guis.HeadBuilder;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.net.URL;
import java.util.*;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static org.bukkit.Bukkit.getServer;

/**
 * NMS implementation for Minecraft 1.21.8 - 1.21.+ (v1_21_R5).
 *
 * <p>Provides version-specific logic for enchantment checks, registry access,
 * anvil repair cost manipulation, and player head texture application using
 * the {@link AnvilView} API available in this version range.</p>
 */
public class NMS_v1_21_R5 implements NMS {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canItemBeEnchanted(ItemStack item) {
        Enchantment[] checkers = {
                Enchantment.BINDING_CURSE,
                Enchantment.UNBREAKING,
                Enchantment.MENDING
        };

        return Arrays.stream(checkers).anyMatch(e -> e.canEnchantItem(item));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Enchantment> getRegisteredEnchantments() {
        return new ArrayList<>(Registry.ENCHANTMENT.stream().toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Material> getMaterials() {
        return new ArrayList<>(Registry.MATERIAL.stream().toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ISupportedPlugin> getSupportedPlugins() {
        return new ArrayList<>() {
            {
                add(new plugins.AdvancedEnchantments_v1_21_R5());
                add(new plugins.EcoEnchants_v1_21_R5());
                add(new plugins.EnchantsSquared_v1_21_R5());
                add(new plugins.ExcellentEnchants_v1_21_R5());
                add(new plugins.UberEnchant_v1_21_R5());
                add(new plugins.Vane_v1_21_R5());
                add(new plugins.Zenchantments_v1_21_R5());
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView) {
        AnvilView anvilView = (AnvilView) inventoryView;

        return anvilView.getRepairCost();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setItemRepairCost(ItemStack item, int repairCost) {
        if (item.getItemMeta() instanceof Repairable meta) {
            meta.setRepairCost(0);
            item.setItemMeta(meta);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAnvilRepairCost(AnvilInventory anvilInventory, InventoryView inventoryView, int repairCost) {
        AnvilView anvilView = (AnvilView) inventoryView;

        anvilView.setRepairCost(repairCost);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeadBuilder setTexture(HeadBuilder headBuilder, String texture) {
        PlayerProfile profile = getServer().createPlayerProfile(UUID.randomUUID(), "");

        PlayerTextures textures = profile.getTextures();

        String decodedTextureAsJson = new String(Base64.getDecoder().decode(texture));

        URL url;

        try {
            JsonObject textureJson = JsonParser.parseString(decodedTextureAsJson).getAsJsonObject();

            url = new URI(
                    textureJson
                            .get("textures")
                            .getAsJsonObject()
                            .get("SKIN")
                            .getAsJsonObject()
                            .get("url")
                            .getAsString()
            ).toURL();
        } catch (Exception e) {
            logger.warning("Failed to parse texture as JSON: " + e.getMessage());

            return headBuilder;
        }

        textures.setSkin(url);

        SkullMeta meta = (SkullMeta) headBuilder.getItemMeta();

        try {
            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            logger.warning("Failed to set texture for skull item: " + e.getMessage());
        }

        headBuilder.setItemMeta(meta);

        return headBuilder;
    }
}
