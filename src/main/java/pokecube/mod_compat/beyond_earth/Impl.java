package pokecube.mod_compat.beyond_earth;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrscauthd.beyond_earth.events.Methods;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.database.Database;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;

public class Impl
{

    static PokedexEntry megaray;

    static BiMap<ResourceKey<Level>, ResourceKey<Level>> ORBITMAP = HashBiMap.create();

    public static void register()
    {
        megaray = Database.getEntry("rayquaza_mega");
        MinecraftForge.EVENT_BUS.register(Impl.class);
    }

    @SubscribeEvent
    public static void toOrbit(final PlayerTickEvent event)
    {
        final Level tworld = event.player.getLevel();
        if (!(tworld instanceof ServerLevel level)) return;

        final Entity riding = event.player.getRootVehicle();
        final IPokemob pokemob = PokemobCaps.getPokemobFor(riding);
        if (pokemob == null || pokemob.getPokedexEntry() != megaray) return;
        ResourceKey<Level> other = null;
        ResourceKey<Level> here = riding.getLevel().dimension();
        int newY = -1;
        if (here.location().getPath().contains("earth_orbit") && riding.getY() < 20)
        {
            other = Level.OVERWORLD;
            newY = 400;
        }
        else if (here.location().toString().equals("minecraft:overworld") && riding.getY() > 500)
        {
            other = Methods.earth_orbit;
            newY = 100;
        }
        if (other != null && newY != -1)
        {
            TeleDest dest = new TeleDest();
            dest.setPos(GlobalPos.of(other, riding.getOnPos().atY(newY)));
            ThutTeleporter.transferTo(riding, dest);
        }
    }

}
