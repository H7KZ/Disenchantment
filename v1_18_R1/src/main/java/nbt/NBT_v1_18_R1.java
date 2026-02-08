package nbt;

import org.bukkit.inventory.ItemStack;

/**
 * NBT utility class for Minecraft 1.18-1.20.4.
 * Provides convenience methods for modifying NBT data on ItemStacks,
 * delegating to {@link NBTEditor_v1_18_R1} for the actual NBT manipulation.
 */
public class NBT_v1_18_R1 {
    /**
     * Sets the RepairCost NBT tag on an item stack.
     *
     * @param item       the item stack to modify
     * @param repairCost the repair cost value to set
     * @return the modified item stack with the updated RepairCost tag
     */
    public static ItemStack setNBTRepairCost(ItemStack item, int repairCost) {
        return NBTEditor_v1_18_R1.set(item, repairCost, "RepairCost");
    }
}
