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

        AtomicReference<Integer> enchantmentCost = new AtomicReference<>(getBaseRepairCost());
        AtomicReference<Double> baseMultiplier = new AtomicReference<>(1 - getRepairCostMultiplier());

        List<Map.Entry<Enchantment, Integer>> enchantmentsListed = new ArrayList<>(enchantments.entrySet());

        enchantmentsListed.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        enchantmentsListed.forEach((entry) -> {
            baseMultiplier.updateAndGet(v -> v + getRepairCostMultiplier());
            enchantmentCost.updateAndGet(v -> (int) (v + entry.getValue() * baseMultiplier.get()));
        });

        return enchantmentCost.get();
    }
}
