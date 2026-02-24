package kazdelpatch.mixins.early.server;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import kazdelpatch.runtime.ServerThreadContext;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServerThreadCapture {

    @Inject(method = "run", at = @At("HEAD"))
    private void kazdelpatch$captureServerThread(CallbackInfo ci) {
        ServerThreadContext.captureCurrentThread();
    }
}
