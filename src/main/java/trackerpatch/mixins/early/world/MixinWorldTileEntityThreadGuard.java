package trackerpatch.mixins.early.world;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorldTileEntityThreadGuard {

    @Shadow
    @Final
    public boolean isRemote;

    @Shadow
    protected IChunkProvider chunkProvider;

    @Unique
    private static final String TRACKERPATCH_SERVER_THREAD_NAME = "Server thread";

    @Inject(method = "getTileEntity", at = @At("HEAD"), cancellable = true)
    private void trackerpatch$guardAsyncTileEntityAccess(
        int x,
        int y,
        int z,
        CallbackInfoReturnable<TileEntity> cir
    ) {
        if (this.isRemote || y < 0 || y >= 256) {
            return;
        }
        if (TRACKERPATCH_SERVER_THREAD_NAME.equals(Thread.currentThread().getName())) {
            return;
        }

        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        Chunk chunk = this.trackerpatch$getLoadedChunk(chunkX, chunkZ);
        if (chunk == null) {
            cir.setReturnValue(null);
            return;
        }

        cir.setReturnValue(chunk.getTileEntityUnsafe(x & 15, y, z & 15));
    }

    @Unique
    private Chunk trackerpatch$getLoadedChunk(int chunkX, int chunkZ) {
        IChunkProvider provider = this.chunkProvider;
        if (!(provider instanceof ChunkProviderServer)) {
            return null;
        }
        ChunkProviderServer chunkProviderServer = (ChunkProviderServer) provider;
        long key = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
        return (Chunk) chunkProviderServer.loadedChunkHashMap.getValueByKey(key);
    }
}
