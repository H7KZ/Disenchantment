package com.jankominek.disenchantment.guis;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

import static com.jankominek.disenchantment.Disenchantment.logger;

public class HeadBuilder extends ItemBuilder {
    public HeadBuilder() {
        super(Material.PLAYER_HEAD);
    }

    public HeadBuilder setTexture(String texture) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");

        profile.getProperties().put("textures", new Property("textures", texture));

        SkullMeta meta = (SkullMeta) this.getItemMeta();

        Field profileField;

        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            logger.warning("Failed to set texture for skull item: " + e.getMessage());
        }

        this.setItemMeta(meta);

        return this;
    }

    public SkullMeta getSkullMeta() {
        return (SkullMeta) this.getItemMeta();
    }

    public HeadBuilder setSkullMeta(SkullMeta meta) {
        this.setItemMeta(meta);

        return this;
    }
}
