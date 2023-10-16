package pokecube.mod_compat.cobblemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.SpawnEvent.SendOut;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.client.render.mobs.overlays.Health;
import pokecube.core.commands.Kill.KillCommandEvent;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager.GeneticsProvider;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.mod_compat.cobblemon.cobblemobs.coblemob.CobbleBase;
import pokecube.mod_compat.cobblemon.cobblemobs.coblemob.CobblePokemob;
import thut.api.OwnableCaps;
import thut.core.common.world.mobs.data.DataSync_Impl;

@Mod.EventBusSubscriber(modid = "cobblemon")
public class Compat
{
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        PokecubeAPI.POKEMOB_BUS.addListener(Compat::preSendOut);
        PokecubeAPI.POKEMOB_BUS.addListener(Compat::postSendOut);
        PokecubeAPI.POKEMOB_BUS.addListener(Compat::onKillCommand);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, Compat::onEntityCaps);
    }

    /**
     * Prevents /pokecube kill from killing these.
     */
    private static void preSendOut(final SendOut.Pre event)
    {
        try
        {
            IPokemob mob = PokemobCaps.getPokemobFor(event.entity);
            System.out.println(event.entity + " " + mob.getPokecube());
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Prevents /pokecube kill from killing these.
     */
    private static void postSendOut(final SendOut.Pre event)
    {

    }

    /**
     * Prevents /pokecube kill from killing these.
     */
    private static void onKillCommand(final KillCommandEvent event)
    {
        if (event.getEntity() instanceof PokemonEntity) event.setCanceled(true);
    }

    private static void onEntityCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        // Only consider mobEntity, IPokemob requires that
        if (!(event.getObject() instanceof PokemonEntity mob)) return;
        // If someone already added it, lets skip
        if (!event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP))
        {
            final CobbleBase pokemob = new CobblePokemob(mob);
            final GeneticsProvider genes = new GeneticsProvider();
            final DataSync_Impl data = new DataSync_Impl();
            pokemob.setDataSync(data);
            pokemob.genes = genes.wrapped;
            event.addCapability(GeneticsManager.POKECUBEGENETICS, genes);
            event.addCapability(EventsHandler.POKEMOBCAP, pokemob);
            event.addCapability(EventsHandler.DATACAP, data);
            IGuardAICapability.addCapability(event);
            final ICapabilitySerializable<?> own = OwnableCaps.makeMobOwnable(mob, true);
            event.addCapability(OwnableCaps.LOCBASE, own);
        }
    }

    // You can use EventBusSubscriber to automatically register all static
    // methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = "cobblemon", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            event.enqueueWork(() -> {
                var old = Health.RENDER_HEALTH;
                Health.RENDER_HEALTH = (entity, viewPoint) -> {
                    if (entity instanceof PokemonEntity) return false;
                    return old.apply(entity, viewPoint);
                };
            });
        }
    }
}
