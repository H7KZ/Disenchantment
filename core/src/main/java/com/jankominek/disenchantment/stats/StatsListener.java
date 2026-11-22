package com.jankominek.disenchantment.stats;

import com.jankominek.disenchantment.events.api.PostDisenchantEvent;
import com.jankominek.disenchantment.events.api.PostGrindstoneExtractEvent;
import com.jankominek.disenchantment.events.api.PostGrindstoneTransferEvent;
import com.jankominek.disenchantment.events.api.PostShatterEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Bukkit listener that forwards completed disenchantment and shatterment operations
 * to {@link StatsManager} for recording in the cache and database.
 */
public class StatsListener implements Listener {

    private final StatsManager manager;

    /**
     * Constructs a new StatsListener backed by the given manager.
     *
     * @param manager the stats manager to forward records to
     */
    public StatsListener(StatsManager manager) {
        this.manager = manager;
    }

    /**
     * Records a completed disenchantment operation from a {@link PostDisenchantEvent}.
     *
     * @param event the post-disenchant event
     */
    @EventHandler
    public void onPostDisenchant(PostDisenchantEvent event) {
        manager.record(
                OperationType.DISENCHANT,
                event.getPlayer(),
                event.getResultBook(),
                event.getModifiedSourceItem().getType().name(),
                event.getXpCost(),
                event.getEconomyCost()
        );
    }

    /**
     * Records a completed shatterment operation from a {@link PostShatterEvent}.
     *
     * @param event the post-shatter event
     */
    @EventHandler
    public void onPostShatter(PostShatterEvent event) {
        manager.record(
                OperationType.SHATTER,
                event.getPlayer(),
                event.getResultBook(),
                Material.ENCHANTED_BOOK.name(),
                event.getXpCost(),
                event.getEconomyCost()
        );
    }

    /**
     * Records a completed grindstone extraction from a {@link PostGrindstoneExtractEvent}.
     *
     * @param event the post-extract event
     */
    @EventHandler
    public void onPostGrindstoneExtract(PostGrindstoneExtractEvent event) {
        manager.record(
                OperationType.GRIND_EXTRACT,
                event.getPlayer(),
                event.getResultBook(),
                event.getModifiedSourceItem().getType().name(),
                event.getXpCost(),
                event.getEconomyCost()
        );
    }

    /**
     * Records a completed grindstone transfer from a {@link PostGrindstoneTransferEvent}. The recorded
     * keys are the enchantments that moved, not everything the target now carries.
     *
     * @param event the post-transfer event
     */
    @EventHandler
    public void onPostGrindstoneTransfer(PostGrindstoneTransferEvent event) {
        List<String> moved = event.getEnchantments().stream().map(e -> e.getKey().toLowerCase()).toList();
        List<String> keys = new ArrayList<>();
        event.getResultItem().getEnchantments().keySet().forEach(ench -> {
            if (moved.contains(ench.getKey().getKey().toLowerCase())) keys.add(ench.getKey().toString());
        });

        manager.record(
                OperationType.GRIND_TRANSFER,
                event.getPlayer(),
                keys,
                event.getResultItem().getType().name(),
                event.getXpCost(),
                event.getEconomyCost()
        );
    }

    /**
     * Extracts enchantment keys from the result book's EnchantmentStorageMeta.
     */
    static List<String> extractEnchantmentKeys(ItemStack resultBook) {
        if (resultBook == null) return List.of();
        if (!(resultBook.getItemMeta() instanceof EnchantmentStorageMeta meta)) return List.of();
        List<String> keys = new ArrayList<>();
        meta.getStoredEnchants().forEach((ench, level) -> keys.add(ench.getKey().toString()));
        return keys;
    }
}
