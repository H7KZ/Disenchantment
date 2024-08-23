package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.Disenchantment;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.config.types.EnchantmentState;
import cz.kominekjan.disenchantment.plugins.IPlugin;
import cz.kominekjan.disenchantment.plugins.PluginManager;
import cz.kominekjan.disenchantment.plugins.impl.VanillaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;

import java.util.HashMap;

import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.config.Config.getDisabledWorlds;
import static cz.kominekjan.disenchantment.utils.AnvilCostUtils.countAnvilCost;
import static cz.kominekjan.disenchantment.utils.EventCheckUtils.isEventValidDisenchantItem;

public class ItemEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        AnvilView anvilView = e.getView();
        if (!(anvilView.getPlayer() instanceof Player p)) return;

        if (!enabled || getDisabledWorlds().contains(p.getWorld())) return;

        if (!(p.hasPermission("disenchantment.all") || p.hasPermission("disenchantment.anvil") || p.hasPermission("disenchantment.anvil.item")))
            return;

        if (!isEventValidDisenchantItem(e.getInventory().getItem(0), e.getInventory().getItem(1))) return;

        ItemStack firstItem = e.getInventory().getItem(0);

        if (firstItem == null) return;

        HashMap<Enchantment, Integer> enchantments = new HashMap<>(firstItem.getEnchantments());

        // Find enchantment to keep to remove them to result
        Config.getEnchantmentsStatus().forEach((enchantment, status) -> {
            if (EnchantmentState.KEEP.equals(status))
                enchantments.remove(enchantment);
        });

        if (enchantments.isEmpty()) return;

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        HashMap<String, IPlugin> activatedPlugins = PluginManager.getActivatedPlugins();

        if (activatedPlugins.isEmpty()) {
            book = VanillaPlugin.createEnchantedBook(enchantments);

        } else {
            for (IPlugin plugin : activatedPlugins.values()) {
                book = plugin.createEnchantedBook(enchantments);
            }

        }

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        anvilView.setRepairCost(countAnvilCost(enchantments));
        Bukkit.getScheduler().runTask(Disenchantment.plugin, () -> {
            // sadly, Spigot like to reset the changed price when vanilla do not expect it to be successful.
            anvilView.setRepairCost(countAnvilCost(enchantments));

            // Allow player to see the price even after writing in the rename textbox (fix it for spigot and paper)
            p.updateInventory();
        });

    }
}
