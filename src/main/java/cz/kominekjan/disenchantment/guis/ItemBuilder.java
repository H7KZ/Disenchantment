package cz.kominekjan.disenchantment.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    protected final ItemStack stack;

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

    public ItemBuilder setAmount(int amount) {
        this.stack.setAmount(amount);
        return this;
    }

    public ItemBuilder setDisplayName(String displayname) {
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(displayname);
        this.setItemMeta(meta);
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
