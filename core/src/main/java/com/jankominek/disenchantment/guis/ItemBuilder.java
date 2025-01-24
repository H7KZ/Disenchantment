package com.jankominek.disenchantment.guis;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    protected ItemStack stack;

    public ItemBuilder(Material mat) {
        this.stack = new ItemStack(mat);
    }

    public ItemMeta getItemMeta() {
        ItemMeta meta = this.stack.getItemMeta();

        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(this.stack.getType());

        return meta;
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        this.stack.setItemMeta(meta);

        return this;
    }

    public ItemBuilder setColor(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) this.getItemMeta();

        meta.setColor(color);
        this.setItemMeta(meta);

        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (glow) {
            this.addEnchantment(Enchantment.KNOCKBACK, 1);
            this.addItemFlag(ItemFlag.HIDE_ENCHANTS);
        } else {
            ItemMeta meta = this.getItemMeta();

            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                meta.removeEnchant(enchantment);
            }
        }

        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        ItemMeta meta = this.getItemMeta();

        meta.setUnbreakable(unbreakable);
        this.setItemMeta(meta);

        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.stack.setAmount(amount);

        return this;
    }

    public ItemBuilder setHead(String owner) {
        SkullMeta meta = (SkullMeta) this.getItemMeta();

        meta.setOwner(owner);
        this.setItemMeta(meta);

        return this;
    }

    public ItemBuilder setDisplayName(String displayname) {
        ItemMeta meta = this.getItemMeta();

        meta.setDisplayName(displayname);
        this.setItemMeta(meta);

        return this;
    }

    public ItemBuilder setItemStack(ItemStack stack) {
        this.stack = stack;

        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = this.getItemMeta();

        meta.setLore(lore);
        this.setItemMeta(meta);

        return this;
    }

    public ItemBuilder setLore(String lore) {
        ArrayList<String> loreList = new ArrayList<>();
        loreList.add(lore);

        ItemMeta meta = this.getItemMeta();

        meta.setLore(loreList);
        this.setItemMeta(meta);

        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        ItemMeta meta = this.getItemMeta();

        meta.addEnchant(enchantment, level, true);
        this.setItemMeta(meta);

        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        ItemMeta meta = this.getItemMeta();

        meta.addItemFlags(flag);
        this.setItemMeta(meta);

        return this;
    }

    public ItemBuilder addAllFlags() {
        ItemMeta meta = this.getItemMeta();

        meta.addItemFlags(ItemFlag.values());
        this.setItemMeta(meta);

        return this;

    }

    public ItemStack build() {
        return this.stack;
    }
}
