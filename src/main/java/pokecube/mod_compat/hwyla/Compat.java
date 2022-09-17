package pokecube.mod_compat.hwyla;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.overlays.Health;
import pokecube.core.entity.pokemobs.EntityPokemob;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin(value = PokecubeCore.MODID)
public class Compat implements IWailaPlugin
{
    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.registerEntityComponent(HUDHandlerMobs.INSTANCE, EntityPokemob.class);
    }

    public static class HUDHandlerMobs implements IEntityComponentProvider
    {
        private static final ResourceLocation HUDKEY = new ResourceLocation("pokecube", "pokemob_obfuscator");
        public static final HUDHandlerMobs INSTANCE = new HUDHandlerMobs();

        @Override
        public void appendTooltip(final ITooltip tooltip, final EntityAccessor accessor, final IPluginConfig config)
        {
            final Entity mob = accessor.getEntity();
            final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);

            if (pokemob != null && Health.obfuscateName(pokemob))
            {
                final Component name = Health.obfuscate(mob.getName());
                tooltip.clear();
                tooltip.add(0, name);
            }
        }

        @Override
        public ResourceLocation getUid()
        {
            return HUDKEY;
        }
    }

}
