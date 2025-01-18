package nbt;

import org.bukkit.inventory.ItemStack;

public class NBT {
    public static ItemStack setNBTRepairCost(ItemStack item, int repairCost) {
        return NBTEditor.set(item, repairCost, "RepairCost");
    }
}
