package kazdelpatch.mixins.early.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import kazdelpatch.runtime.ShutdownReasonContext;

import java.util.List;

@Mixin(ServerConfigurationManager.class)
public abstract class MixinServerConfigurationManagerStopReason {

    @Shadow
    public List playerEntityList;

    @Inject(method = "removeAllPlayers", at = @At("HEAD"), cancellable = true)
    private void kazdelpatch$replaceShutdownMessage(CallbackInfo ci) {
        String reason = ShutdownReasonContext.consumeReason();
        if (reason == null || reason.isEmpty()) {
            return;
        }

        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            EntityPlayerMP player = (EntityPlayerMP) this.playerEntityList.get(i);
            if (player != null && player.playerNetServerHandler != null) {
                player.playerNetServerHandler.kickPlayerFromServer(reason);
            }
        }
        ci.cancel();
    }
}
