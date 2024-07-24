package cz.kominekjan.disenchantment.events;

import cz.kominekjan.disenchantment.config.DisabledConfigEnchantment;
import cz.kominekjan.disenchantment.events.advanced.DisenchantmentAdvancedEvent;
import cz.kominekjan.disenchantment.events.eco.DisenchantmentEcoEvent;
import cz.kominekjan.disenchantment.events.excellent.DisenchantmentExcellentEvent;
import cz.kominekjan.disenchantment.events.normal.DisenchantmentNormalEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Optional;

import static cz.kominekjan.disenchantment.Disenchantment.activatedPlugins;
import static cz.kominekjan.disenchantment.Disenchantment.enabled;
import static cz.kominekjan.disenchantment.config.Config.getDisabledEnchantments;
import static cz.kominekjan.disenchantment.config.Config.getDisabledWorlds;
import static cz.kominekjan.disenchantment.utils.EventCheckUtil.isEventValidDisenchantment;

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

        for (DisabledConfigEnchantment disabledConfigEnchantment : getDisabledEnchantments()) {
            if (!disabledConfigEnchantment.doKeep()) continue;

            if (enchantments.keySet().stream().anyMatch(m -> m.getKey().getKey().equalsIgnoreCase(disabledConfigEnchantment.getEnchantmentKey()))) {
                Optional<Enchantment> enchantment = enchantments.keySet().stream().filter(m -> m.getKey().getKey().equalsIgnoreCase(disabledConfigEnchantment.getEnchantmentKey())).findFirst();

                enchantment.ifPresent(enchantments::remove);
            }
        }

        if (enchantments.isEmpty()) return;

        if (activatedPlugins.contains("ExcellentEnchants"))
            DisenchantmentExcellentEvent.onDisenchantmentEvent(e, enchantments);

        else if (activatedPlugins.contains("EcoEnchants"))
            DisenchantmentEcoEvent.onDisenchantmentEvent(e, enchantments);

        else if (activatedPlugins.contains("AdvancedEnchantments"))
            DisenchantmentAdvancedEvent.onDisenchantmentEvent(e, enchantments);

        else
            DisenchantmentNormalEvent.onDisenchantmentEvent(e, enchantments);
    }
}
