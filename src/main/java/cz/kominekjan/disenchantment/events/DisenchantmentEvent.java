package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.plugins.DisenchantmentPluginManager;
import cz.kominekjan.disenchantment.plugins.IDisenchantmentPlugin;
import cz.kominekjan.disenchantment.plugins.impl.VanillaPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.config.Config.getDisabledWorlds;
import static cz.kominekjan.disenchantment.utils.AnvilCostUtils.countAnvilCost;
import static cz.kominekjan.disenchantment.utils.EventCheckUtils.isEventValidDisenchantment;

public class DisenchantmentEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        if (!(e.getView().getPlayer() instanceof Player p)) return;

        if (!(p.hasPermission("disenchantment.all") || p.hasPermission("disenchantment.anvil"))) return;

        if (!enabled || getDisabledWorlds().contains(p.getWorld())) return;

        if (!isEventValidDisenchantment(e.getInventory().getItem(0), e.getInventory().getItem(1))) return;

        ItemStack firstItem = e.getInventory().getItem(0);

        if (firstItem == null) return;

        HashMap<Enchantment, Integer> enchantments = new HashMap<>(firstItem.getEnchantments());

        for (Map.Entry<Enchantment, Boolean> disabledEnchantment : getDisabledEnchantments().entrySet()) {
            if (!disabledEnchantment.getValue()) continue;

            if (enchantments.keySet().stream().anyMatch(m -> m.equals(disabledEnchantment.getKey()))) {
                Optional<Enchantment> enchantment = enchantments.keySet().stream().filter(m -> m.equals(disabledEnchantment.getKey())).findFirst();

                enchantment.ifPresent(enchantments::remove);
            }
        }

        if (enchantments.isEmpty()) return;

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        HashMap<String, IDisenchantmentPlugin> activatedPlugins = DisenchantmentPluginManager.getActivatedPlugins();

        boolean atLeastOnePluginEnabled = false;

        for (IDisenchantmentPlugin plugin : activatedPlugins.values()) {
            book = plugin.createEnchantedBook(enchantments);
            atLeastOnePluginEnabled = true;
        }

        if (!atLeastOnePluginEnabled) book = VanillaPlugin.createEnchantedBook(enchantments);

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        e.getInventory().setRepairCost(countAnvilCost(enchantments));
    }
}
