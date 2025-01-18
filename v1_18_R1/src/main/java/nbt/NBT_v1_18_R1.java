package nbt;

import org.bukkit.inventory.ItemStack;

public class NBT_v1_18_R1 {
    public static ItemStack setNBTRepairCost(ItemStack item, int repairCost) {
        return NBTEditor_v1_18_R1.set(item, repairCost, "RepairCost");
    }
}
