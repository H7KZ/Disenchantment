package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.jankominek.disenchantment.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

/**
 * Plugin adapter for EcoEnchants on Minecraft 1.18-1.20.4.
 * Integrates EcoEnchants custom enchantments with Disenchantment's system and
 * re-registers the EcoEnchants anvil listener to ensure correct event ordering.
 */
public class EcoEnchants_v1_18_R1 implements ISupportedPlugin {
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
     * <p>Schedules a delayed task to fix EcoEnchants 13+ anvil event ordering.
     *
     * <p>EcoEnchants 13 introduced two breaking changes for third-party anvil plugins:
     * <ol>
     *   <li>Its {@link PrepareAnvilEvent} handler at HIGHEST synchronously clears the result
     *       slot, then re-computes it asynchronously. For disenchant operations (enchanted item
     *       + blank book) the async result is FAIL, so it leaves the slot alone — meaning
     *       Disenchantment's result survives as long as EcoEnchants fires <em>first</em>.
     *       Since Disenchantment declares EcoEnchants as a soft-dependency, EcoEnchants always
     *       loads (and registers its listeners) before Disenchantment, giving us the correct
     *       natural order with no extra work needed here.</li>
     *   <li>Its {@link InventoryClickEvent} handler at HIGHEST cancels any click on anvil slot 2
     *       that EcoEnchants did not produce itself (by comparing generation counters). This
     *       prevents Disenchantment from ever processing its own results. Re-registering all
     *       EcoEnchants click listeners to the end of the handler list lets Disenchantment run
     *       first; by the time EcoEnchants tries to cancel, the item is already on the player's
     *       cursor.</li>
     * </ol></p>
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
            // Avoid sending warning if CustomAnvil is present as it disables the listener itself
            if (!pm.isPluginEnabled("CustomAnvil")) {
                logger.warning("Could not find eco enchant pre anvil listener");
            }
        } else {
            // unregister then re register event so it is executed before disenchantment events
            preAnvilHandler.unregister(ecoListener);
            preAnvilHandler.register(ecoListener);
        }

        // Re-register all EcoEnchants InventoryClickEvent listeners to fire after Disenchantment.
        // EcoEnchants 13+ cancels clicks on anvil slot 2 that it didn't produce, blocking
        // Disenchantment from processing its own results. Firing after Disenchantment means
        // Disenchantment handles the click first; EcoEnchants' cancellation is then a no-op.
        List<RegisteredListener> ecoClickListeners = new ArrayList<>();
        HandlerList clickHandler = InventoryClickEvent.getHandlerList();

        for (RegisteredListener listener : clickHandler.getRegisteredListeners()) {
            if (ecoEnchant == listener.getPlugin()) {
                ecoClickListeners.add(listener);
            }
        }

        if (ecoClickListeners.isEmpty()) {
            if (!pm.isPluginEnabled("CustomAnvil")) {
                logger.warning("Could not find EcoEnchants click listener");
            }

            return;
        }

        for (RegisteredListener listener : ecoClickListeners) {
            clickHandler.unregister(listener);
            clickHandler.register(listener);
        }
    }
}
