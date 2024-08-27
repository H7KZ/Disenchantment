package cz.kominekjan.disenchantment.utils;

import cz.kominekjan.disenchantment.plugins.IPlugin;
import cz.kominekjan.disenchantment.plugins.PluginManager;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DisenchantUtils {
    private static final Enchantment[] enchantmentCheck = {
            Enchantment.BINDING_CURSE,
            Enchantment.UNBREAKING,
            Enchantment.MENDING
    };

    public static void addEnchantment(ItemStack item, Enchantment enchantment, Integer level) {
        item.addUnsafeEnchantment(enchantment, level);
    }

    public static void addStoredEnchantment(ItemStack item, Enchantment enchantment, Integer level) {
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof EnchantmentStorageMeta storage)
            storage.addStoredEnchant(enchantment, level, true);
        else meta.addEnchant(enchantment, level, true);

        item.setItemMeta(meta);
    }

    public static void removeEnchantment(ItemStack item, Enchantment enchantment) {
        item.removeEnchantment(enchantment);
    }

    public static void removeStoredEnchantment(ItemStack item, Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof EnchantmentStorageMeta storage)
            storage.removeStoredEnchant(enchantment);
        else meta.removeEnchant(enchantment);

        item.setItemMeta(meta);
    }

    public static Boolean canBeEnchanted(Material material) {
        return canBeEnchanted(new ItemStack(material));
    }

    public static Boolean canBeEnchanted(ItemStack item) {
        return Arrays.stream(enchantmentCheck).anyMatch(e -> e.canEnchantItem(item));
    }

    public static HashMap<Enchantment, Integer> fetchEnchantments(ItemStack item){
        HashMap<Enchantment, Integer> enchantments;

        if (item.hasItemMeta() && item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            enchantments = new HashMap<>(meta.getStoredEnchants());
        } else {
            enchantments = new HashMap<>(item.getEnchantments());
        }

        HashMap<String, IPlugin> activatedPlugins = PluginManager.getActivatedPlugins();

        if (!activatedPlugins.isEmpty()) {
            for (IPlugin plugin : activatedPlugins.values()) {
                plugin.fetchComplementaryEnchantment(item, enchantments);
            }
        }

        return enchantments;
    }

    public static List<Enchantment> everyEnchantments(){
        HashMap<String, IPlugin> activatedPlugins = PluginManager.getActivatedPlugins();

        if (activatedPlugins.isEmpty()) {
            // Return registered enchantments
            return Registry.ENCHANTMENT.stream().toList();

        } else {
            // return registered enchantment and other if any
            List<Enchantment> enchantments = new ArrayList<>(Registry.ENCHANTMENT.stream().toList());

            for (IPlugin plugin : activatedPlugins.values()) {
                enchantments.addAll(plugin.everyComplementaryEnchantments());
            }

            return enchantments;
        }

    }


}
