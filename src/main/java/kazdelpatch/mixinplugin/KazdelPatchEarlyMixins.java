package kazdelpatch.mixinplugin;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.Name("KazdelPatchEarlyMixins")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"kazdelpatch"})
public final class KazdelPatchEarlyMixins implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.kazdelpatch.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return Arrays.asList(
            "server.MixinMinecraftServerThreadCapture",
            "server.MixinServerConfigurationManagerStopReason",
            "entitytracker.MixinEntityTrackerPatch",
            "world.MixinWorldTileEntityThreadGuard"
        );
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
