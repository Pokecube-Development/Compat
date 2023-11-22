package pokecube.mod_compat.curios;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.api.events.init.CompatEvent;
import pokecube.mod_compat.CompatMod;

@Mod.EventBusSubscriber
public class Compat
{
    static
    {
        pokecube.compat.Compat.BUS.register(Compat.class);
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Compat::onIMC);
    }

    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        if (CompatMod.config.allowCompat("curios")) Impl.register();
    }

    private static void onIMC(final InterModEnqueueEvent event)
    {
        if (CompatMod.config.allowCompat("curios")) Impl.onIMC(event);
    }
}
