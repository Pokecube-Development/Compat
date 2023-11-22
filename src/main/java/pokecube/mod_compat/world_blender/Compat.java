package pokecube.mod_compat.world_blender;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.api.PokecubeAPI;
import pokecube.mod_compat.CompatMod;

@Mod.EventBusSubscriber
public class Compat
{
    public static final String MODID = "world_blender";

    static
    {
        PokecubeAPI.LOGGER.debug("Checking World Blender: " + CompatMod.config.allowCompat(Compat.MODID));
        if (CompatMod.config.allowCompat(Compat.MODID)) FMLJavaModLoadingContext.get().getModEventBus().register(
                Compat.class);
    }
}
