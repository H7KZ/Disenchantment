package com.jankominek.disenchantment.guis;

import org.bukkit.Material;
import org.bukkit.inventory.meta.SkullMeta;

import static com.jankominek.disenchantment.Disenchantment.nms;

public class HeadBuilder extends ItemBuilder {
    public HeadBuilder() {
        super(Material.PLAYER_HEAD);
    }

    public HeadBuilder setTexture(String texture) {
        return nms.setTexture(this, texture);
    }

    public SkullMeta getSkullMeta() {
        return (SkullMeta) this.getItemMeta();
    }

    public HeadBuilder setSkullMeta(SkullMeta meta) {
        this.setItemMeta(meta);

        return this;
    }
}
