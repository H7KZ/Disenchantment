package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.Disenchantment;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.plugins.IPlugin;
import cz.kominekjan.disenchantment.plugins.PluginManager;
import cz.kominekjan.disenchantment.plugins.impl.VanillaPlugin;
import cz.kominekjan.disenchantment.types.EnchantmentState;
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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.view.AnvilView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static cz.kominekjan.disenchantment.utils.AnvilCostUtils.countAnvilCost;

public class ShatterEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        AnvilView anvilView = e.getView();

        if (!(anvilView.getPlayer() instanceof Player p)) return;

        if (!Config.isPluginEnabled() || !Config.Shatterment.isEnabled() || Config.Shatterment.getDisabledWorlds().contains(p.getWorld()))
            return;

        if (!(p.hasPermission("disenchantment.all") || p.hasPermission("disenchantment.anvil") || p.hasPermission("disenchantment.anvil.shatter")))
            return;

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        if (!EventCheckUtils.Shatterment.isEventValid(firstItem, secondItem)) return;

        EnchantmentStorageMeta firstItemItemMeta = (EnchantmentStorageMeta) firstItem.getItemMeta();

        HashMap<Enchantment, Integer> enchantments = new HashMap<>(firstItemItemMeta.getStoredEnchants());

        if (enchantments.size() < 2) return;

        Config.Shatterment.getEnchantmentStates().forEach((enchantment, state) -> {
            if (EnchantmentState.KEEP.equals(state)) enchantments.remove(enchantment);
        });

        if (enchantments.isEmpty()) return;

        HashMap<Enchantment, Integer> randomEnchantmentShatter = new HashMap<>();

        List<Enchantment> keys = new ArrayList<>(enchantments.keySet());
        Collections.shuffle(keys);

        int halfSize = keys.size() / 2;

        if (halfSize == 0) halfSize = 1;

        for (int i = 0; i < halfSize; i++) {
            Enchantment key = keys.get(i);
            randomEnchantmentShatter.put(key, enchantments.get(key));
        }

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

        anvilView.setRepairCost(countAnvilCost(enchantments, EventType.SHATTERMENT));

        Bukkit.getScheduler().runTask(Disenchantment.plugin, () -> {
            // sadly, Spigot like to reset the changed price when vanilla do not expect it to be successful.
            anvilView.setRepairCost(countAnvilCost(enchantments, EventType.SHATTERMENT));

            // Allow player to see the price even after writing in the rename textbox (fix it for spigot and paper)
            p.updateInventory();
        });
    }
}
