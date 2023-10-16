package pokecube.mod_compat.cobblemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.DistExecutor;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.SpawnEvent.SendOut;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.commands.Kill.KillCommandEvent;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager.GeneticsProvider;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.mod_compat.cobblemon.cobblemobs.coblemob.CobbleBase;
import pokecube.mod_compat.cobblemon.cobblemobs.coblemob.CobblePokemob;
import thut.api.OwnableCaps;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class Impl
{
    public static final Object o = DistExecutor.safeRunForDist(() -> ClientImpl::new, () -> Object::new);

    public static void register()
    {
        PokecubeAPI.POKEMOB_BUS.addListener(Impl::preSendOut);
        PokecubeAPI.POKEMOB_BUS.addListener(Impl::postSendOut);
        PokecubeAPI.POKEMOB_BUS.addListener(Impl::onKillCommand);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, Impl::onEntityCaps);
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
}
