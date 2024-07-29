package cz.kominekjan.disenchantment.utils;

import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cz.kominekjan.disenchantment.config.Config.*;

public class AnvilCostUtil {
    public static int countAnvilCost(Map<Enchantment, Integer> enchantments) {
        if (!getEnableRepairCost()) return 0;

        double enchantmentCost = getBaseRepairCost();
        double baseMultiplier = 1;

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).toList()) {
            enchantmentCost += entry.getValue() * baseMultiplier;
            baseMultiplier += getRepairCostMultiplier();
        }

        return (int) Math.round(enchantmentCost);
    }
}
