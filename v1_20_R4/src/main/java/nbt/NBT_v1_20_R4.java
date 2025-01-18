package nbt;

import org.bukkit.inventory.ItemStack;

public class NBT_v1_20_R4 {
    public static ItemStack setNBTRepairCost(ItemStack item, int repairCost) {
        return NBTEditor_v1_20_R4.set(item, repairCost, "RepairCost");
    }
}
