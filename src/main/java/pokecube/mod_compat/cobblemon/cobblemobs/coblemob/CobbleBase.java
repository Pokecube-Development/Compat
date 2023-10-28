package pokecube.mod_compat.cobblemon.cobblemobs.coblemob;

import java.util.List;
import java.util.Set;

import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.DataKeys;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.events.pokemobs.InitAIEvent;
import pokecube.api.utils.PokeType;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.logic.LogicInLiquid;
import pokecube.core.ai.logic.LogicInMaterials;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.ai.logic.LogicMovesUpdates;
import pokecube.core.database.Database;
import pokecube.core.impl.capabilities.impl.PokemobSaves;
import pokecube.core.utils.CapHolders;
import pokecube.mod_compat.cobblemon.cobblemobs.ai.Tasks;
import thut.api.ThutCaps;
import thut.api.entity.ai.IAIRunnable;
import thut.core.common.ThutCore;

public abstract class CobbleBase extends PokemobSaves implements ICapabilitySerializable<CompoundTag>
{
    private final LazyOptional<IPokemob> holder = LazyOptional.of(() -> this);
    private Pokemon cobbled;
    protected PokemonEntity cobblemon;
    private PokedexEntry cobbleEntry = Database.missingno;
    private String[] moves = new String[4];

    public CobbleBase(final PokemonEntity mob)
    {
        for (final AIRoutine routine : AIRoutine.values()) this.setRoutineState(routine, routine.getDefault());
        this.cobblemon = mob;
        this.setEntity(mob);
        this.cobbleEntry = Database.getEntry("cobble_missingno");
        if (cobbleEntry == null)
        {
            cobbleEntry = new PokedexEntry(0, "cobble_missingno", true);
            cobbleEntry.stock = false;
            cobbleEntry.type1 = PokeType.unknown;
            cobbleEntry.type2 = PokeType.unknown;
            PokedexEntry base = Database.missingno;
            if (base != null)
            {
                cobbleEntry.setBaseForme(base);
                base.copyToForm(cobbleEntry);
            }
        }
    }

    public Pokemon getCobbled()
    {
        PokemonEntity e = (PokemonEntity) this.getEntity();
        this.cobbled = e.getPokemon();
        if (this.cobbled == null)
        {
            this.cobbled = new Pokemon();
            System.out.println("New cobbled??");
            Thread.dumpStack();
            return cobbled;
        }
        String name = "cobble_" + cobbled.getSpecies().getName();
        if (!name.equals(cobbleEntry.getName()))
        {
            cobbleEntry = Database.getEntry(name);
            if (cobbleEntry == null)
            {
                cobbleEntry = new PokedexEntry(0, name, true);
                cobbleEntry.type1 = PokeType.unknown;
                cobbleEntry.type2 = PokeType.unknown;
                cobbleEntry.stock = false;
            }
            if (cobbleEntry.getBaseForme() == null)
            {
                PokedexEntry base = Database.getEntry(cobbled.getSpecies().getName());
                if (base == null) base = Database.missingno;
                cobbleEntry.setBaseForme(base);
                base.copyToForm(cobbleEntry);
            }
        }
        return cobbled;
    }

    private List<IAIRunnable> tasks = Lists.newArrayList();

    @Override
    public List<IAIRunnable> getTasks()
    {
        return this.tasks;
    }

    /**
     * We override this so that we can call our own version of Tasks. the
     * default implementation does not apply combat AI to non-stock pokemobs, as
     * it expects them to fight back. Since Cobblemon do not fight back, we need
     * to apply combat AI.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void preInitAI()
    {
        final Brain<?> brain = this.getEntity().getBrain();
        // If brain was cleared at some point, this memory is removed.
        if (brain.checkMemory(MemoryModules.ATTACKTARGET.get(), MemoryStatus.REGISTERED)) return;

        final Mob entity = this.getEntity();

        this.guardCap = entity.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        this.genes = entity.getCapability(ThutCaps.GENETICS_CAP).orElse(null);
        if (this.getOwnerHolder() == null)
            PokecubeAPI.LOGGER.warn("Pokemob without ownable cap, this is a bug! " + this.getPokedexEntry());
        if (this.guardCap == null)
            PokecubeAPI.LOGGER.warn("Pokemob without guard cap, this is a bug! " + this.getPokedexEntry());
        if (this.genes == null)
            PokecubeAPI.LOGGER.warn("Pokemob without genetics cap, this is a bug! " + this.getPokedexEntry());

        this.getTickLogic().clear();

        // // Add in the various logic AIs that are needed on both client and
        // // server, so it is done here instead of in initAI.
        this.getTickLogic().add(new LogicInLiquid(this));
        this.getTickLogic().add(new LogicMovesUpdates(this));
        this.getTickLogic().add(new LogicInMaterials(this));
        this.getTickLogic().add(new LogicMiscUpdate(this));

        // If the mob was constructed without a world somehow (during init for
        // JEI, etc), do not bother with AI stuff.
        if (entity.level() == null || ThutCore.proxy.isClientSide())
        {
            if (entity.level() != null) PokecubeAPI.POKEMOB_BUS.post(new InitAIEvent.Post(this));
            return;
        }

        this.tasks = Lists.newArrayList();
        Tasks.initBrain(brain);

        final Set<Pair<MemoryModuleType<?>, MemoryStatus>> idleMems = Sets.newHashSet();
        final Set<Pair<MemoryModuleType<?>, MemoryStatus>> workMems = Sets.newHashSet();
        final Set<Pair<MemoryModuleType<?>, MemoryStatus>> coreMems = Sets.newHashSet();

        idleMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryStatus.VALUE_ABSENT));
        workMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryStatus.VALUE_ABSENT));
        coreMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT));

        brain.addActivityWithConditions(Activity.IDLE, Tasks.idle(this, 1), idleMems);
        brain.addActivityWithConditions(Activity.WORK, Tasks.utility(this, 1), workMems);
        brain.addActivityWithConditions(Activity.CORE, Tasks.combat(this, 1), coreMems);
        brain.setCoreActivities(Sets.newHashSet(Activity.IDLE, Activity.WORK, Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
        brain.updateActivityFromSchedule(entity.level().getDayTime(), entity.level().getGameTime());

        if (this.loadedTasks != null) for (final IAIRunnable task : this.tasks)
            if (this.loadedTasks.contains(task.getIdentifier()) && task instanceof INBTSerializable<?>)
                INBTSerializable.class.cast(task).deserializeNBT(this.loadedTasks.get(task.getIdentifier()));
        // Send notification event of AI initilization, incase anyone wants to
        // affect it.
        PokecubeAPI.POKEMOB_BUS.post(new InitAIEvent.Post(this));
    }

    @Override
    public PokedexEntry getPokedexEntry()
    {
        getCobbled();
        return cobbleEntry;
    }

    @Override
    public PokedexEntry getBasePokedexEntry()
    {
        getCobbled();
        return cobbleEntry;
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        if (capability == ThutCaps.COLOURABLE) return this.holder.cast();
        if (capability == ThutCaps.BREEDS) return this.holder.cast();
        if (capability == ThutCaps.OWNABLE_CAP) return this.holder.cast();
        return PokemobCaps.POKEMOB_CAP.orEmpty(capability, this.holder);
    }

    @Override
    public int getExp()
    {
        return getCobbled().getExperience();
    }

    @Override
    public int getLevel()
    {
        return getCobbled().getLevel();
    }

    @Override
    public byte getSexe()
    {
        Pokemon cobbled = this.getCobbled();
        Gender gender = cobbled.getGender();
        byte sexe = IPokemob.NOSEXE;
        sexe = gender == Gender.FEMALE ? IPokemob.FEMALE : gender == Gender.MALE ? IPokemob.MALE : sexe;
        return sexe;
    }

    private Ability _ability;

    @Override
    public Ability getAbility()
    {
        Pokemon cobbled = this.getCobbled();
        var a = cobbled.getAbility();
        if (_ability == null && a != null)
        {
            _ability = AbilityManager.getAbility(a.getName());
        }
        return _ability;
    }

    @Override
    public String[] getMoves()
    {
        Pokemon cobbled = this.getCobbled();
        var _moves = cobbled.getMoveSet();
        for (int i = 0; i < _moves.getMoves().size() && i < 4; i++)
        {
            moves[i] = _moves.get(i).getName();
        }

        return moves;
    }

    @Override
    public Mob getEntity()
    {
        return this.entity;
    }

    @Override
    public int getStat(Stats stat, boolean modified)
    {
        try
        {
            return super.getStat(stat, modified);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 5;
        }
    }

    @Override
    public int getBaseStat(Stats stat)
    {
        try
        {
            return super.getBaseStat(stat);
        }
        catch (Exception e)
        {
            System.out.println(this.getPokedexEntry().getBaseForme()+" "+e);
            return 5;
        }
    }

    @Override
    public void read(CompoundTag arg0)
    {
        String name = arg0.getString("pokedexentry");
        cobbleEntry = Database.getEntry(name);
        if (cobbleEntry == null)
        {
            cobbleEntry = new PokedexEntry(0, name, true);
            cobbleEntry.stock = false;
        }
        PokedexEntry base = Database.getEntry(name.replaceFirst("cobble_", ""));
        if (base != null)
        {
            cobbleEntry.setBaseForme(base);
            base.copyToForm(cobbleEntry);
        }
        if (arg0.contains("Pokemon"))
        {
            CompoundTag pokemon = arg0.getCompound("Pokemon");
            cobblemon.getPokemon().loadFromNBT(pokemon);
            cobblemon.setPersistenceRequired();
        }
        super.read(arg0);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return this.write();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        this.read(nbt);
    }

    @Override
    public CompoundTag write()
    {
        CompoundTag tag = super.write();
        tag.putString("pokedexentry", this.getPokedexEntry().name);
        tag.put("Pokemon", this.getCobbled().saveToNBT(new CompoundTag()));
        return tag;
    }

    @Override
    public boolean isSheared()
    {
        FlagSpeciesFeature feature = getCobbled().getFeature(DataKeys.HAS_BEEN_SHEARED);
        if (feature == null) return false;
        return feature.getEnabled();
    }

}
