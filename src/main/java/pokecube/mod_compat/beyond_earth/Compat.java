package pokecube.mod_compat.beyond_earth;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import pokecube.api.events.init.CompatEvent;

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
        if (ModList.get().isLoaded("beyond_earth")) Impl.register();
    }
}
