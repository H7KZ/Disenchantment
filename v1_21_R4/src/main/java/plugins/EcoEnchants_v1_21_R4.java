package plugins;

import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import com.jankominek.disenchantment.plugins.VanillaPlugin;
import com.jankominek.disenchantment.utils.EnchantmentUtils;
import com.willfp.eco.core.items.builder.EnchantedBookBuilder;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import java.util.Map;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;

public class EcoEnchants_v1_21_R4 implements ISupportedPlugin {
    public String getName() {
        return "EcoEnchants";
    }

    public Map<Enchantment, Integer> getItemEnchantments(ItemStack item) {
        return EnchantmentUtils.getItemEnchantments(item);
    }

    public ItemStack createEnchantedBook(Map<Enchantment, Integer> enchantments) {
        ItemStack book;

        EnchantedBookBuilder builder = new EnchantedBookBuilder();

        enchantments.forEach(builder::addStoredEnchantment);

        book = builder.build();

        return book;
    }

    public ItemStack removeEnchantmentsFromItem(ItemStack firstItem, Map<Enchantment, Integer> enchantments) {
        return VanillaPlugin.removeEnchantments(firstItem, enchantments);
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
