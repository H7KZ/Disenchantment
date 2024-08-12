package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.plugins.IPlugin;
import cz.kominekjan.disenchantment.plugins.PluginManager;
import cz.kominekjan.disenchantment.plugins.impl.VanillaPlugin;
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

import java.util.*;

import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.config.Config.*;
import static cz.kominekjan.disenchantment.utils.AnvilCostUtils.countAnvilCost;
import static cz.kominekjan.disenchantment.utils.EventCheckUtils.isEventValidDisenchantSplitBook;

public class SplitBookEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        AnvilView anvilView = e.getView();

        if (!(anvilView.getPlayer() instanceof Player p)) return;

        if (!enabled || getDisabledWorlds().contains(p.getWorld()) || getDisableBookSplitting() || getDisabledBookSplittingWorlds().contains(p.getWorld()))
            return;

        if (!(p.hasPermission("disenchantment.all") || p.hasPermission("disenchantment.anvil") || p.hasPermission("disenchantment.anvil.split_book")))
            return;

        ItemStack firstItem = e.getInventory().getItem(0);
        ItemStack secondItem = e.getInventory().getItem(1);

        if (!isEventValidDisenchantSplitBook(firstItem, secondItem)) return;

        EnchantmentStorageMeta firstItemItemMeta = (EnchantmentStorageMeta) firstItem.getItemMeta();

        HashMap<Enchantment, Integer> enchantments = new HashMap<>(firstItemItemMeta.getStoredEnchants());

        for (Map.Entry<Enchantment, Boolean> disabledEnchantment : getDisabledBookSplittingEnchantments().entrySet()) {
            if (!disabledEnchantment.getValue()) continue;

            if (enchantments.keySet().stream().anyMatch(m -> m.equals(disabledEnchantment.getKey()))) {
                Optional<Enchantment> enchantment = enchantments.keySet().stream().filter(m -> m.equals(disabledEnchantment.getKey())).findFirst();

                enchantment.ifPresent(enchantments::remove);
            }
        }

        if (enchantments.isEmpty() || enchantments.size() == 1) return;

        HashMap<Enchantment, Integer> randomEnchantmentSplit = new HashMap<>();

        List<Enchantment> keys = new ArrayList<>(enchantments.keySet());
        Collections.shuffle(keys); // Shuffle the keys to ensure randomness

        int halfSize = keys.size() / 2; // Calculate half of the size
        for (int i = 0; i < halfSize; i++) {
            Enchantment key = keys.get(i);
            randomEnchantmentSplit.put(key, enchantments.get(key));
        }

        // ----------------------------------------------------------------------------------------------------
        // Disenchantment plugins

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

        HashMap<String, IPlugin> activatedPlugins = PluginManager.getActivatedPlugins();

        boolean atLeastOnePluginEnabled = false;

        for (IPlugin plugin : activatedPlugins.values()) {
            book = plugin.createEnchantedBook(randomEnchantmentSplit);
            atLeastOnePluginEnabled = true;
        }

        if (!atLeastOnePluginEnabled) book = VanillaPlugin.createEnchantedBook(randomEnchantmentSplit);

        // Disenchantment plugins
        // ----------------------------------------------------------------------------------------------------

        e.setResult(book);

        anvilView.setRepairCost(countAnvilCost(enchantments));
    }
}
