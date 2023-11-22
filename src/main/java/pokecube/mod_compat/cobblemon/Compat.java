package pokecube.mod_compat.cobblemon;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.api.events.init.CompatEvent;
import pokecube.mod_compat.CompatMod;

@Mod.EventBusSubscriber
public class Compat
{
    static
    {
        pokecube.compat.Compat.BUS.register(Compat.class);
    }

    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        if (CompatMod.config.allowCompat("cobblemon")) Impl.register();
    }

}
