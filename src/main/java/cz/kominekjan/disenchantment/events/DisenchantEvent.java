package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.Disenchantment;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.permission.PermissionGoal;
import cz.kominekjan.disenchantment.plugins.IPlugin;
import cz.kominekjan.disenchantment.plugins.PluginManager;
import cz.kominekjan.disenchantment.plugins.impl.VanillaPlugin;
import cz.kominekjan.disenchantment.types.EventType;
import cz.kominekjan.disenchantment.utils.EventCheckUtils;
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
import java.util.Map;

import static cz.kominekjan.disenchantment.utils.AnvilCostUtils.countAnvilCost;

public class DisenchantEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        AnvilView anvilView = e.getView();
        if (!(anvilView.getPlayer() instanceof Player p)) return;

        if (!Config.isPluginEnabled() || !Config.Disenchantment.isEnabled() || Config.Disenchantment.getDisabledWorlds().contains(p.getWorld()))
            return;

        if (!PermissionGoal.DISENCHANTMENT_EVENT.checkPermission(p)) return;

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        Map<Enchantment, Integer> enchantments = EventCheckUtils.Disenchantment.getValidEnchantments(firstItem, secondItem);

        if (enchantments.isEmpty()) return;

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        HashMap<String, IPlugin> activatedPlugins = PluginManager.getActivatedPlugins();

        if (activatedPlugins.isEmpty()) {
            book = VanillaPlugin.createEnchantedBook(enchantments);
        } else {
            for (IPlugin plugin : activatedPlugins.values()) {
                // won't 2 plugin activated on the same time would only use the last book ?
                book = plugin.createEnchantedBook(enchantments);
            }
        }

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        anvilView.setRepairCost(countAnvilCost(enchantments, EventType.DISENCHANTMENT));

        Bukkit.getScheduler().runTask(Disenchantment.plugin, () -> {
            // sadly, Spigot like to reset the changed price when vanilla do not expect it to be successful.
            anvilView.setRepairCost(countAnvilCost(enchantments, EventType.DISENCHANTMENT));

            // Allow player to see the price even after writing in the rename textbox (fix it for spigot and paper)
            p.updateInventory();
        });

    }
}
