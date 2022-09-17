package pokecube.mod_compat.thutessentials;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnBiomeMatcher.StructureMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.TeamManager.ITeamProvider;
import pokecube.api.events.pokemobs.SpawnCheckEvent;
import pokecube.core.eventhandlers.PCEventsHandler;
import pokecube.core.utils.PokemobTracker;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.entity.TeleLoadEvent;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.essentials.Essentials;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.world.IHasStructures;
import thut.essentials.util.world.WorldStructures;

public class Impl
{
    private static class StructChecker implements StructureMatcher
    {
        @Override
        public MatchResult structuresMatch(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
        {
            if (matcher._validStructures.isEmpty()) return MatchResult.PASS;
            if (checker.world instanceof ServerLevel)
            {
                final LazyOptional<IHasStructures> opt = ((ServerLevel) checker.world)
                        .getCapability(WorldStructures.CAPABILITY);
                if (opt.isPresent())
                {
                    final IHasStructures holder = opt.orElseGet(null);
                    final Collection<ResourceLocation> hits = holder.getStructures(checker.location.getPos());
                    for (final ResourceLocation hit : hits)
                        if (matcher._validStructures.contains(hit.toString())) return MatchResult.SUCCEED;
                }
            }
            return MatchResult.FAIL;
        }
    }

    private static class TeamProvider implements ITeamProvider
    {
        final ITeamProvider parent;

        public TeamProvider(final ITeamProvider parent)
        {
            this.parent = parent;
        }

        @Override
        public String getTeam(final Entity entityIn)
        {
            if (entityIn.getLevel().isClientSide) return "";
            final IOwnable ownable = OwnableCaps.getOwnable(entityIn);
            UUID id = ownable != null ? ownable.getOwnerId() : null;
            if (id == null) id = entityIn.getUUID();
            final LandTeam team = LandManager.getTeam(id);
            if (team != LandManager.getDefaultTeam()) return team.teamName;
            return this.parent.getTeam(entityIn);
        }

        @Override
        public boolean areAllied(final String team, final Entity target)
        {
            // This checks if the team strings are the same.
            if (this.parent.areAllied(team, target)) return true;
            final LandTeam teamA = LandManager.getInstance().getTeam(team, false);
            final String targTeam = this.getTeam(target);
            final LandTeam teamB = LandManager.getInstance().getTeam(targTeam, false);
            if (teamA != null && teamB != null)
            {
                if (teamA == teamB) return true;
                if (teamA.isAlly(teamB)) return true;
                if (teamA.isAlly(target.getUUID())) return true;
                return false;
            }
            return false;
        }
    }

    private static class TeleDestManager
    {
        public static void initMatcher(final TeleLoadEvent event)
        {
            // We handle this identically to the equivalent in Essentials for
            // its own type of TeleDest.
            final TeleDest dest = event.getOverride();
            if (dest == null) return;
            if (dest.version != Essentials.config.dim_verison)
            {
                if (!Essentials.config.versioned_dim_keys.contains(dest.getPos().dimension().location())) return;
                Essentials.LOGGER.info("Invalidating stale teledest {} ({})", dest.getName(), dest.getPos());
                event.setCanceled(true);
                event.setOverride(null);
            }
        }
    }

    public static void register()
    {
        PokecubeAPI.LOGGER.debug("Registering ThutEssentials Support");
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, TeleDestManager::initMatcher);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, Impl::init);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, Impl::recallOutMobsOnLogout);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, Impl::recallOutMobsOnUnload);
        TeamManager.provider = new TeamProvider(TeamManager.provider);
    }

    public static void init(final SpawnCheckEvent.Init event)
    {
        event.matcher._structs = StructureMatcher.or(new StructChecker(), event.matcher._structs);
    }

    public static void recallOutMobsOnLogout(final PlayerLoggedOutEvent event)
    {
        if (!(event.getEntity().getLevel() instanceof ServerLevel world)) return;
        if (!Essentials.config.versioned_dim_keys.contains(world.dimension().location())) return;
        final List<Entity> mobs = PokemobTracker.getMobs(event.getEntity(),
                e -> Essentials.config.versioned_dim_keys.contains(e.getLevel().dimension().location()));
        PCEventsHandler.recallAll(mobs, true);
    }

    public static void recallOutMobsOnUnload(final ChunkEvent.Unload event)
    {
        if (event.getLevel() == null || event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel world && event.getChunk() instanceof LevelChunk)) return;
        if (!Essentials.config.versioned_dim_keys.contains(world.dimension().location())) return;
        // FIXME decide on how to best deal with this, now that EntitySections
        // are separate!
        // final List<Entity> mobs = Lists.newArrayList();
        // final LevelChunk chunk = (LevelChunk) event.getChunk();
        // for (final ClassInstanceMultiMap<Entity> list : chunk.getSections())
        // list.forEach(e ->
        // {
        // final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
        // if (pokemob != null && pokemob.getOwnerId() != null || e instanceof
        // EntityPokecube) mobs.add(e);
        // });
        // PCEventsHandler.recallAll(mobs, true);
    }
}
