package kazdelpatch.mixins.early.inventory;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public abstract class MixinContainerStackSanitizer {

    @Shadow
    public List<Slot> inventorySlots;

    @Unique
    private static final Logger KAZDELPATCH$LOGGER = LogManager.getLogger("KazdelPatch");

    @Inject(method = "slotClick", at = @At("HEAD"))
    private void kazdelpatch$sanitizeBeforeSlotClick(
        int slotId,
        int clickedButton,
        int mode,
        EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        this.kazdelpatch$sanitizeCursorStack(player);
        this.kazdelpatch$sanitizeClickedSlot(slotId, player);
    }

    @Unique
    private void kazdelpatch$sanitizeCursorStack(EntityPlayer player) {
        if (player == null) {
            return;
        }

        InventoryPlayer inventory = player.inventory;
        if (inventory == null) {
            return;
        }

        ItemStack cursorStack = inventory.getItemStack();
        if (!this.kazdelpatch$isInvalidStack(cursorStack)) {
            return;
        }

        inventory.setItemStack(null);
        KAZDELPATCH$LOGGER.warn(
            "[KazdelPatch] Cleared invalid cursor stack before slotClick for player {} (count={}).",
            player.getCommandSenderName(),
            Integer.valueOf(cursorStack.stackSize)
        );
    }

    @Unique
    private void kazdelpatch$sanitizeClickedSlot(int slotId, EntityPlayer player) {
        if (slotId < 0 || slotId >= this.inventorySlots.size()) {
            return;
        }

        Slot slot = this.inventorySlots.get(slotId);
        if (slot == null) {
            return;
        }

        ItemStack slotStack = slot.getStack();
        if (!this.kazdelpatch$isInvalidStack(slotStack)) {
            return;
        }

        slot.putStack(null);
        KAZDELPATCH$LOGGER.warn(
            "[KazdelPatch] Cleared invalid slot stack before slotClick for player {} at slot {} (count={}).",
            player == null ? "unknown" : player.getCommandSenderName(),
            Integer.valueOf(slotId),
            Integer.valueOf(slotStack.stackSize)
        );
    }

    @Unique
    private boolean kazdelpatch$isInvalidStack(ItemStack stack) {
        return stack != null && (stack.getItem() == null || stack.stackSize <= 0);
    }
}
