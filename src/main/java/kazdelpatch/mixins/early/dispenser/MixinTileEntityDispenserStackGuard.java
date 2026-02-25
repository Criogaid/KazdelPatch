package kazdelpatch.mixins.early.dispenser;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityDispenser.class)
public abstract class MixinTileEntityDispenserStackGuard {

    @Shadow
    private ItemStack[] field_146022_i;

    @Unique
    private static final Logger KAZDELPATCH$LOGGER = LogManager.getLogger("KazdelPatch");

    @Inject(method = "setInventorySlotContents", at = @At("TAIL"))
    private void kazdelpatch$sanitizeAfterSet(int index, ItemStack stack, CallbackInfo ci) {
        this.kazdelpatch$sanitizeSlot(index);
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void kazdelpatch$sanitizeAfterRead(NBTTagCompound compound, CallbackInfo ci) {
        this.kazdelpatch$sanitizeAllSlots();
    }

    @Inject(method = "func_146017_i", at = @At("HEAD"))
    private void kazdelpatch$sanitizeBeforeDispenseSelection(CallbackInfoReturnable<Integer> cir) {
        this.kazdelpatch$sanitizeAllSlots();
    }

    @Unique
    private void kazdelpatch$sanitizeAllSlots() {
        boolean changed = false;
        for (int i = 0; i < this.field_146022_i.length; i++) {
            if (this.kazdelpatch$isInvalidStack(this.field_146022_i[i])) {
                this.kazdelpatch$logAndClear(i, this.field_146022_i[i]);
                this.field_146022_i[i] = null;
                changed = true;
            }
        }
        if (changed) {
            ((TileEntity) (Object) this).markDirty();
        }
    }

    @Unique
    private void kazdelpatch$sanitizeSlot(int index) {
        if (index < 0 || index >= this.field_146022_i.length) {
            return;
        }
        ItemStack stack = this.field_146022_i[index];
        if (!this.kazdelpatch$isInvalidStack(stack)) {
            return;
        }

        this.kazdelpatch$logAndClear(index, stack);
        this.field_146022_i[index] = null;
        ((TileEntity) (Object) this).markDirty();
    }

    @Unique
    private boolean kazdelpatch$isInvalidStack(ItemStack stack) {
        return stack != null && (stack.getItem() == null || stack.stackSize <= 0);
    }

    @Unique
    private void kazdelpatch$logAndClear(int slot, ItemStack stack) {
        int count = stack == null ? 0 : stack.stackSize;
        KAZDELPATCH$LOGGER.warn(
            "[KazdelPatch] Cleared invalid dispenser stack at {} {} {} slot {} (count={}).",
            Integer.valueOf(((TileEntityDispenser) (Object) this).xCoord),
            Integer.valueOf(((TileEntityDispenser) (Object) this).yCoord),
            Integer.valueOf(((TileEntityDispenser) (Object) this).zCoord),
            Integer.valueOf(slot),
            Integer.valueOf(count)
        );
    }
}
