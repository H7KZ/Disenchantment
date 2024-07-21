package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.events.advanced.DisenchantmentAdvancedEvent;
import cz.kominekjan.disenchantment.events.eco.DisenchantmentEcoEvent;
import cz.kominekjan.disenchantment.events.excellent.DisenchantmentExcellentEvent;
import cz.kominekjan.disenchantment.events.normal.DisenchantmentNormalEvent;
import cz.kominekjan.disenchantment.types.DisabledEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Optional;

import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.config.Config.getDisabledWorlds;
import static cz.kominekjan.disenchantment.utils.EventCheckUtils.isEventValidDisenchantment;

public class DisenchantmentEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisenchantmentEvent(PrepareAnvilEvent e) {
        if (!(e.getView().getPlayer() instanceof Player p)) return;

        if (!p.hasPermission("disenchantment.anvil")) return;

        if (!enabled || getDisabledWorlds().contains(p.getWorld().getName())) return;

        if (!isEventValidDisenchantment(e.getInventory().getItem(0), e.getInventory().getItem(1))) return;

        ItemStack firstItem = e.getInventory().getItem(0);

        assert firstItem != null;

        HashMap<Enchantment, Integer> enchantments = new HashMap<>(firstItem.getEnchantments());

        for (DisabledEnchantment disabledEnchantment : getDisabledEnchantments()) {
            if (!disabledEnchantment.doKeep()) continue;

            if (enchantments.keySet().stream().anyMatch(m -> m.getKey().getKey().equalsIgnoreCase(disabledEnchantment.getEnchantmentKey()))) {
                Optional<Enchantment> enchantment = enchantments.keySet().stream().filter(m -> m.getKey().getKey().equalsIgnoreCase(disabledEnchantment.getEnchantmentKey())).findFirst();

                enchantment.ifPresent(enchantments::remove);
            }
        }

        if (enchantments.isEmpty()) return;

        if (Bukkit.getServer().getPluginManager().getPlugin("ExcellentEnchants") != null) {
            DisenchantmentExcellentEvent.onDisenchantmentEvent(e, enchantments);
            return;
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("EcoEnchants") != null && Bukkit.getServer().getPluginManager().getPlugin("eco") != null) {
            DisenchantmentEcoEvent.onDisenchantmentEvent(e, enchantments);
            return;
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("AdvancedEnchantments") != null) {
            DisenchantmentAdvancedEvent.onDisenchantmentEvent(e, enchantments);
            return;
        }

        DisenchantmentNormalEvent.onDisenchantmentEvent(e, enchantments);
    }
}
