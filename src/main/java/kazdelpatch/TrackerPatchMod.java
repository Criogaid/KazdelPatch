package kazdelpatch;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import kazdelpatch.guard.ContainerAccessGuard;

import java.util.Map;

@Mod(
    modid = Tags.MOD_ID,
    name = Tags.MOD_NAME,
    version = Tags.VERSION,
    acceptableRemoteVersions = "*"
)
public final class TrackerPatchMod {

    private static final Logger LOGGER = LogManager.getLogger("KazdelPatch");
    private static final ContainerAccessGuard CONTAINER_ACCESS_GUARD = new ContainerAccessGuard();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(CONTAINER_ACCESS_GUARD);
        LOGGER.info("[KazdelPatch] preInit complete. EntityTracker patch and container access guard are active.");
    }

    @NetworkCheckHandler
    public boolean networkCheck(Map<String, String> remoteMods, Side remoteSide) {
        if (remoteSide == Side.CLIENT) {
            // Server side: allow vanilla / missing client mod.
            return true;
        }
        // Client side (accidental install): require server to have this mod too.
        return remoteMods != null && remoteMods.containsKey(Tags.MOD_ID);
    }
}
