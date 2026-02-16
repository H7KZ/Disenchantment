package plugins;

import com.jankominek.disenchantment.plugins.IPluginEnchantment;
import com.jankominek.disenchantment.plugins.ISupportedPlugin;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import zedly.zenchantments.Zenchantment;
import zedly.zenchantments.configuration.WorldConfiguration;
import zedly.zenchantments.configuration.WorldConfigurationProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter for the Zenchantments plugin, targeting Minecraft 1.21.8 - 1.21.9 (v1_21_R5).
 *
 * <p>Uses the Zenchantments API to read, add, and remove enchantments,
 * mapping them into the Disenchantment plugin's common {@link IPluginEnchantment} format.</p>
 */
public class Zenchantments_v1_21_R5 implements ISupportedPlugin {
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "Zenchantments";
    }

    /**
     * {@inheritDoc}
     */
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item) {
        // This method is not used in the current implementation, as we need the world context to read enchantments properly.
        // However, we provide a fallback that returns an empty list to avoid potential issues if called without a world.
        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    public List<IPluginEnchantment> getItemEnchantments(ItemStack item, World world) {
        WorldConfiguration worldConfig = WorldConfigurationProvider.getInstance().getConfigurationForWorld(world);

        // Get all Zenchantments on the item (supports both regular items and enchanted books)
        Map<Zenchantment, Integer> zenchantments = Zenchantment.getZenchantmentsOnItemStack(
                item,
                true, // acceptBooks - allow reading from enchanted books
                worldConfig
        );

        return zenchantments
                .entrySet()
                .stream()
                .map(entry -> remapEnchantment(entry.getKey(), entry.getValue(), worldConfig))
                .collect(Collectors.toList());
    }

    /**
     * Wraps a Zenchantment into an {@link IPluginEnchantment} that delegates to the Zenchantments API.
     *
     * @param enchantment the Zenchantment to wrap
     * @param level       the enchantment level
     * @param worldConfig the world configuration for this enchantment
     * @return a plugin enchantment adapter for the given Zenchantment
     */
    private static IPluginEnchantment remapEnchantment(Zenchantment enchantment, int level, WorldConfiguration worldConfig) {
        return new IPluginEnchantment() {
            @Override
            public String getKey() {
                return enchantment.getKey().getKey().toLowerCase();
            }

            @Override
            public int getLevel() {
                return level;
            }

            @Override
            public ItemStack addToBook(ItemStack book) {
                ItemStack result = book.clone();

                enchantment.setForItemStack(result, level, worldConfig);

                return result;
            }

            @Override
            public ItemStack removeFromBook(ItemStack book) {
                ItemStack result = book.clone();

                // Remove the Zenchantment by setting level to 0
                enchantment.setForItemStack(result, 0, worldConfig);

                return result;
            }

            @Override
            public ItemStack addToItem(ItemStack item) {
                ItemStack result = item.clone();

                enchantment.setForItemStack(result, level, worldConfig);

                return result;
            }

            @Override
            public ItemStack removeFromItem(ItemStack item) {
                ItemStack result = item.clone();

                // Remove the Zenchantment by setting level to 0
                enchantment.setForItemStack(result, 0, worldConfig);

                return result;
            }
        };
    }
}
