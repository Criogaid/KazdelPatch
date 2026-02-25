package kazdelpatch.hat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public final class HatFeature {

    private HatFeature() {
    }

    public static boolean trySwapHeldWithHelmet(EntityPlayer player) {
        if (player == null) {
            return false;
        }

        InventoryPlayer inventory = player.inventory;
        ItemStack held = inventory.getCurrentItem();
        if (held != null && held.stackSize != 1) {
            return false;
        }

        ItemStack helmet = inventory.armorInventory[3];
        inventory.armorInventory[3] = held;
        inventory.setInventorySlotContents(inventory.currentItem, helmet);
        inventory.markDirty();
        player.inventoryContainer.detectAndSendChanges();
        player.openContainer.detectAndSendChanges();
        return true;
    }
}
