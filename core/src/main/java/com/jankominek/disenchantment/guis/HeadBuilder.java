package com.jankominek.disenchantment.guis;

import org.bukkit.Material;
import org.bukkit.inventory.meta.SkullMeta;

import static com.jankominek.disenchantment.Disenchantment.nms;

/**
 * Extension of {@link ItemBuilder} for building player head items with custom skull textures.
 */
public class HeadBuilder extends ItemBuilder {
    /**
     * Constructs a new HeadBuilder with a PLAYER_HEAD material.
     */
    public HeadBuilder() {
        super(Material.PLAYER_HEAD);
    }

    /**
     * Sets the skull texture using a Base64-encoded texture string via NMS.
     *
     * @param texture the Base64-encoded texture value
     * @return this builder for chaining
     */
    public HeadBuilder setTexture(String texture) {
        return nms.setTexture(this, texture);
    }

    /**
     * Returns the item meta cast as {@link SkullMeta}.
     *
     * @return the skull meta
     */
    public SkullMeta getSkullMeta() {
        return (SkullMeta) this.getItemMeta();
    }

    /**
     * Sets the skull meta on the underlying item stack.
     *
     * @param meta the {@link SkullMeta} to apply
     * @return this builder for chaining
     */
    public HeadBuilder setSkullMeta(SkullMeta meta) {
        this.setItemMeta(meta);

        return this;
    }
}
