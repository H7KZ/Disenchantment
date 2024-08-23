package cz.kominekjan.disenchantment.utils;

import com.google.common.util.concurrent.AtomicDouble;
import cz.kominekjan.disenchantment.config.Config;
import cz.kominekjan.disenchantment.types.EventType;
import org.bukkit.enchantments.Enchantment;

import java.util.Comparator;
import java.util.Map;

public class AnvilCostUtils {
    public static int countAnvilCost(Map<Enchantment, Integer> enchantments, EventType eventType) {
        double enchantmentCost = 0;
        double baseMultiplier = 0;

        switch (eventType) {
            case EventType.DISENCHANTMENT:
                if (!Config.Disenchantment.Anvil.Repair.isCostEnabled()) return 0;

                enchantmentCost = Config.Disenchantment.Anvil.Repair.getBaseCost();
                baseMultiplier = Config.Disenchantment.Anvil.Repair.getCostMultiplier();

                break;
            case EventType.SHATTERMENT:
                if (!Config.Shatterment.Anvil.Repair.isCostEnabled()) return 0;

                enchantmentCost = Config.Shatterment.Anvil.Repair.getBaseCost();
                baseMultiplier = Config.Shatterment.Anvil.Repair.getCostMultiplier();

                break;
            default:
                return 0;
        }

        AtomicDouble multiplier = new AtomicDouble(baseMultiplier);

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).toList()) {
            enchantmentCost += entry.getValue() * multiplier.get();
            multiplier.set(multiplier.get() + baseMultiplier);
        }

        return (int) Math.round(enchantmentCost);
    }
}
