package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.SchedulerUtils;
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

/**
 * Adapter for the EcoEnchants plugin, targeting Minecraft 1.21.8 - 1.21.9 (v1_21_R5).
 *
 * <p>Maps EcoEnchants enchantments (which register as Bukkit {@link Enchantment} instances)
 * into the Disenchantment plugin's common {@link IPluginEnchantment} format. Also re-orders
 * the EcoEnchants anvil event listener on activation to ensure proper event execution order.</p>
 */
public class EcoEnchants_v1_21_R5 implements ISupportedPlugin {
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "EcoEnchants";
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Wraps a Bukkit {@link Enchantment} (including EcoEnchants custom ones) into
     * an {@link IPluginEnchantment} using standard enchantment utilities.
     *
     * @param enchantment the enchantment to wrap
     * @param level       the enchantment level
     * @return a plugin enchantment adapter for the given enchantment
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>Schedules a delayed task to re-register the EcoEnchants {@link PrepareAnvilEvent}
     * listener so that it fires before the Disenchantment listener.</p>
     */
    public void activate() {
        SchedulerUtils.runGlobal(plugin, this::delayActivation);
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
