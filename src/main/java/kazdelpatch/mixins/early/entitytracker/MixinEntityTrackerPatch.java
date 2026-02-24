package kazdelpatch.mixins.early.entitytracker;

import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(EntityTracker.class)
public abstract class MixinEntityTrackerPatch {

    @Shadow
    private Set trackedEntities;

    @Shadow
    private IntHashMap trackedEntityIDs;

    @Shadow
    @Final
    private WorldServer theWorld;

    @Unique
    private static final Logger TRACKER_PATCH_LOGGER = LogManager.getLogger("KazdelPatch");

    @Inject(method = "<init>", at = @At("TAIL"))
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void kazdelpatch$replaceTrackedSet(WorldServer world, CallbackInfo ci) {
        Set<EntityTrackerEntry> replacement = Collections.newSetFromMap(
            new ConcurrentHashMap<EntityTrackerEntry, Boolean>());
        int copied = 0;
        for (Object value : this.trackedEntities) {
            if (value instanceof EntityTrackerEntry) {
                replacement.add((EntityTrackerEntry) value);
                copied++;
            }
        }
        this.trackedEntities = (Set) replacement;
        TRACKER_PATCH_LOGGER.info(
            "[KazdelPatch] EntityTracker.trackedEntities swapped to concurrent set (copied={}).",
            Integer.valueOf(copied));
    }

    @Inject(method = "updateTrackedEntities", at = @At("HEAD"), cancellable = true)
    private void kazdelpatch$patchUpdateTrackedEntities(CallbackInfo ci) {
        List<EntityTrackerEntry> snapshot = this.kazdelpatch$snapshotEntries();
        ArrayList<EntityPlayerMP> playersToRefresh = new ArrayList<EntityPlayerMP>(snapshot.size());

        for (EntityTrackerEntry entitytrackerentry : snapshot) {
            if (entitytrackerentry == null) {
                continue;
            }
            entitytrackerentry.sendLocationToAllClients(this.theWorld.playerEntities);
            if (entitytrackerentry.playerEntitiesUpdated && entitytrackerentry.myEntity instanceof EntityPlayerMP) {
                playersToRefresh.add((EntityPlayerMP) entitytrackerentry.myEntity);
            }
        }

        for (EntityPlayerMP entityplayermp : playersToRefresh) {
            for (EntityTrackerEntry entitytrackerentry : snapshot) {
                if (entitytrackerentry != null && entitytrackerentry.myEntity != entityplayermp) {
                    entitytrackerentry.tryStartWachingThis(entityplayermp);
                }
            }
        }

        ci.cancel();
    }

    @Inject(method = "func_85172_a(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/world/chunk/Chunk;)V", at = @At("HEAD"), cancellable = true)
    private void kazdelpatch$patchChunkWatch(EntityPlayerMP player, Chunk chunk, CallbackInfo ci) {
        List<EntityTrackerEntry> snapshot = this.kazdelpatch$snapshotEntries();
        for (EntityTrackerEntry entitytrackerentry : snapshot) {
            if (entitytrackerentry != null
                && entitytrackerentry.myEntity != player
                && entitytrackerentry.myEntity.chunkCoordX == chunk.xPosition
                && entitytrackerentry.myEntity.chunkCoordZ == chunk.zPosition) {
                entitytrackerentry.tryStartWachingThis(player);
            }
        }
        ci.cancel();
    }

    @Inject(method = "removeEntityFromAllTrackingPlayers", at = @At("HEAD"), cancellable = true)
    private void kazdelpatch$patchRemoveEntityFromAllTrackingPlayers(Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            List<EntityTrackerEntry> snapshot = this.kazdelpatch$snapshotEntries();
            for (EntityTrackerEntry entitytrackerentry : snapshot) {
                if (entitytrackerentry != null) {
                    entitytrackerentry.removeFromWatchingList(player);
                }
            }
        }

        EntityTrackerEntry removed = (EntityTrackerEntry) this.trackedEntityIDs.removeObject(entity.getEntityId());
        if (removed != null) {
            this.trackedEntities.remove(removed);
            removed.informAllAssociatedPlayersOfItemDestruction();
        }
        ci.cancel();
    }

    @Inject(method = "removePlayerFromTrackers", at = @At("HEAD"), cancellable = true)
    private void kazdelpatch$patchRemovePlayerFromTrackers(EntityPlayerMP player, CallbackInfo ci) {
        List<EntityTrackerEntry> snapshot = this.kazdelpatch$snapshotEntries();
        for (EntityTrackerEntry entitytrackerentry : snapshot) {
            if (entitytrackerentry != null) {
                entitytrackerentry.removePlayerFromTracker(player);
            }
        }
        ci.cancel();
    }

    @Unique
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<EntityTrackerEntry> kazdelpatch$snapshotEntries() {
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                return new ArrayList<EntityTrackerEntry>((Collection<EntityTrackerEntry>) (Collection) this.trackedEntities);
            } catch (ConcurrentModificationException ignored) {
                Thread.yield();
            }
        }
        TRACKER_PATCH_LOGGER.warn(
            "[KazdelPatch] Failed to snapshot trackedEntities after retries; returning empty snapshot.");
        return Collections.emptyList();
    }
}
