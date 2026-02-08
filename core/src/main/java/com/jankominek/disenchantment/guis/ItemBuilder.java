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

/**
 * Fluent builder for constructing and customizing Bukkit {@link ItemStack} instances.
 * Supports setting display name, lore, enchantments, item flags, color, glow effect, and more.
 */
public class ItemBuilder {
    protected ItemStack stack;

    /**
     * Constructs a new ItemBuilder for the given material.
     *
     * @param mat the material type for the item
     */
    public ItemBuilder(Material mat) {
        this.stack = new ItemStack(mat);
    }

    /**
     * Returns the item meta for the current item stack, creating a default one if none exists.
     *
     * @return the {@link ItemMeta}
     */
    public ItemMeta getItemMeta() {
        ItemMeta meta = this.stack.getItemMeta();

        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(this.stack.getType());

        return meta;
    }

    /**
     * Sets the item meta on the underlying item stack.
     *
     * @param meta the {@link ItemMeta} to apply
     * @return this builder for chaining
     */
    public ItemBuilder setItemMeta(ItemMeta meta) {
        this.stack.setItemMeta(meta);

        return this;
    }

    /**
     * Sets the leather armor color. Only applicable to leather armor items.
     *
     * @param color the {@link Color} to apply
     * @return this builder for chaining
     */
    public ItemBuilder setColor(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) this.getItemMeta();

        meta.setColor(color);
        this.setItemMeta(meta);

        return this;
    }

    /**
     * Adds or removes a glow effect on the item. When enabled, applies a hidden knockback
     * enchantment to produce the visual glow.
     *
     * @param glow true to add glow, false to remove all enchantments
     * @return this builder for chaining
     */
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

    /**
     * Sets whether the item is unbreakable.
     *
     * @param unbreakable true to make the item unbreakable
     * @return this builder for chaining
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        ItemMeta meta = this.getItemMeta();

        meta.setUnbreakable(unbreakable);
        this.setItemMeta(meta);

        return this;
    }

    /**
     * Sets the stack amount for the item.
     *
     * @param amount the number of items in the stack
     * @return this builder for chaining
     */
    public ItemBuilder setAmount(int amount) {
        this.stack.setAmount(amount);

        return this;
    }

    /**
     * Sets the skull owner for player head items.
     *
     * @param owner the name of the skull owner
     * @return this builder for chaining
     */
    public ItemBuilder setHead(String owner) {
        SkullMeta meta = (SkullMeta) this.getItemMeta();

        meta.setOwner(owner);
        this.setItemMeta(meta);

        return this;
    }

    /**
     * Sets the display name of the item.
     *
     * @param displayname the display name
     * @return this builder for chaining
     */
    public ItemBuilder setDisplayName(String displayname) {
        ItemMeta meta = this.getItemMeta();

        meta.setDisplayName(displayname);
        this.setItemMeta(meta);

        return this;
    }

    /**
     * Replaces the underlying item stack.
     *
     * @param stack the new {@link ItemStack}
     * @return this builder for chaining
     */
    public ItemBuilder setItemStack(ItemStack stack) {
        this.stack = stack;

        return this;
    }

    /**
     * Sets the item lore from a list of strings.
     *
     * @param lore the lore lines
     * @return this builder for chaining
     */
    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = this.getItemMeta();

        meta.setLore(lore);
        this.setItemMeta(meta);

        return this;
    }

    /**
     * Sets the item lore from a single string.
     *
     * @param lore the lore line
     * @return this builder for chaining
     */
    public ItemBuilder setLore(String lore) {
        ArrayList<String> loreList = new ArrayList<>();
        loreList.add(lore);

        ItemMeta meta = this.getItemMeta();

        meta.setLore(loreList);
        this.setItemMeta(meta);

        return this;
    }

    /**
     * Adds an enchantment to the item, ignoring level restrictions.
     *
     * @param enchantment the enchantment to add
     * @param level       the enchantment level
     * @return this builder for chaining
     */
    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        ItemMeta meta = this.getItemMeta();

        meta.addEnchant(enchantment, level, true);
        this.setItemMeta(meta);

        return this;
    }

    /**
     * Adds a single item flag to the item.
     *
     * @param flag the {@link ItemFlag} to add
     * @return this builder for chaining
     */
    public ItemBuilder addItemFlag(ItemFlag flag) {
        ItemMeta meta = this.getItemMeta();

        meta.addItemFlags(flag);
        this.setItemMeta(meta);

        return this;
    }

    /**
     * Adds all available item flags to the item, hiding all extra information.
     *
     * @return this builder for chaining
     */
    public ItemBuilder addAllFlags() {
        ItemMeta meta = this.getItemMeta();

        meta.addItemFlags(ItemFlag.values());
        this.setItemMeta(meta);

        return this;

    }

    /**
     * Builds and returns the final {@link ItemStack}.
     *
     * @return the constructed item stack
     */
    public ItemStack build() {
        return this.stack;
    }
}
