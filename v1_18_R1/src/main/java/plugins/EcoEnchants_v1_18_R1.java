package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import java.util.List;
import java.util.stream.Collectors;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

public class EcoEnchants_v1_18_R1 implements ISupportedPlugin {
    public String getName() {
        return "EcoEnchants";
    }

    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        List<IPluginEnchantment> enchantments;

        if (item.hasItemMeta() && item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            enchantments = meta.getStoredEnchants()
                    .entrySet()
                    .stream()
                    .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        } else {
            enchantments = item.getEnchantments()
                    .entrySet()
                    .stream()
                    .map(entry -> remapEnchantment(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        return enchantments;
    }

    private static IPluginEnchantment remapEnchantment(Enchantment enchantment, int level) {
        return new IPluginEnchantment() {
            @Override
            public String getKey() {
                return enchantment.getKey().getKey().toLowerCase();
            }

            @Override
            public int getLevel() {
                return level;
            }

            @Override
            public ItemStack addToBook(ItemStack book) {
                ItemStack item = book.clone();

                EnchantmentUtils.addStoredEnchantment(item, enchantment, level);

                return item;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack item = book.clone();

                EnchantmentUtils.removeStoredEnchantment(item, enchantment);

                return item;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();

                EnchantmentUtils.addEnchantment(result, enchantment, level);

                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();

                EnchantmentUtils.removeEnchantment(result, enchantment);

                return result;
            }
        };
    }

    public void activate() {
        Bukkit.getScheduler().runTask(plugin, this::delayActivation);
    }

    // Just activate but later bc eco enchant is a... weirdly constructed plugin...
    private void delayActivation() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        Plugin ecoEnchant = pm.getPlugin(this.getName());

        if (ecoEnchant == null) {
            logger.warning("Could not find eco enchant plugin but eco compatibility is enabled");

            return;
        }

        // Get the annoying listener
        RegisteredListener ecoListener = null;
        HandlerList preAnvilHandler = PrepareAnvilEvent.getHandlerList();

        for (RegisteredListener listener : preAnvilHandler.getRegisteredListeners()) {
            if (ecoEnchant != listener.getPlugin()) continue;

            ecoListener = listener;

            break;
        }

        if (ecoListener == null) {
            // Avoid sending warning if CustomAnvil is present as it disable the listener itself
            if (pm.isPluginEnabled("CustomAnvil")) return;

            logger.warning("Could not find eco enchant pre anvil listener");

            return;
        }

        // unregister then re register event so it is executed before disenchantment events
        preAnvilHandler.unregister(ecoListener);
        preAnvilHandler.register(ecoListener);
    }
}
