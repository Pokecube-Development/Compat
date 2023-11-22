package pokecube.mod_compat.beyond_earth;

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
    public static void loadComplete(final CompatEvent event)
    {
        if (CompatMod.config.allowCompat("beyond_earth")) Impl.register();
    }
}
