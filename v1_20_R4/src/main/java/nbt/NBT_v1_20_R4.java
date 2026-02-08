package nbt;

import org.bukkit.inventory.ItemStack;

/**
 * NBT utility class for Minecraft 1.20.5-1.20.6.
 * Provides convenience methods for modifying NBT data on ItemStacks,
 * delegating to {@link NBTEditor_v1_20_R4} for the actual NBT manipulation.
 */
public class NBT_v1_20_R4 {
    /**
     * Sets the RepairCost NBT tag on an item stack.
     *
     * @param item       the item stack to modify
     * @param repairCost the repair cost value to set
     * @return the modified item stack with the updated RepairCost tag
     */
    public static ItemStack setNBTRepairCost(ItemStack item, int repairCost) {
        return NBTEditor_v1_20_R4.set(item, repairCost, "RepairCost");
    }
}
