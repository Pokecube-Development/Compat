package pokecube.mod_compat.cct;

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
    public static void serverAboutToStart(final CompatEvent event)
    {
        if (CompatMod.config.allowCompat("computercraft")) Impl.register();
    }

}
