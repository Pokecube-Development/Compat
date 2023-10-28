package pokecube.mod_compat.cobblemon.cobblemobs.ai;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.InitAIEvent.Init;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.tasks.combat.attacks.SelectMoveTask;
import pokecube.core.ai.tasks.combat.attacks.UseAttacksTask;
import pokecube.core.ai.tasks.combat.management.CallForHelpTask;
import pokecube.core.ai.tasks.combat.management.FindTargetsTask;
import pokecube.core.ai.tasks.combat.management.ForgetTargetTask;
import pokecube.core.ai.tasks.combat.movement.CicleTask;
import pokecube.core.ai.tasks.combat.movement.DodgeTask;
import pokecube.core.ai.tasks.combat.movement.LeapTask;
import pokecube.core.ai.tasks.idle.ForgetHuntedByTask;
import pokecube.core.ai.tasks.misc.FollowOwnerTask;
import pokecube.core.ai.tasks.misc.WalkToTask;
import pokecube.core.ai.tasks.utility.GatherTask;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.ai.tasks.utility.UseMoveTask;
import thut.api.entity.ai.BrainUtil;
import thut.api.entity.ai.IAIRunnable;

public class Tasks
{

    private static final List<SensorType<?>> getSensors()
    {
        return List.of(SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, Sensors.VISIBLE_BLOCKS.get(),
                Sensors.INTERESTING_ENTITIES.get());
    }

    private static final List<MemoryModuleType<?>> getMemories()
    {
        return List.of(MemoryModules.ATTACKTARGET.get(), MemoryModules.HUNTTARGET.get(), MemoryModules.HUNTED_BY.get(),
                MemoryModules.MOVE_TARGET.get(), MemoryModules.LEAP_TARGET.get(), MemoryModules.PATH,
                MemoryModules.MATE_TARGET, MemoryModules.WALK_TARGET, MemoryModules.LOOK_TARGET,
                MemoryModules.EGG.get(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModules.NOT_FOUND_PATH,
                MemoryModuleType.DOORS_TO_CLOSE);
    }

    public static void initBrain(final Brain<?> brain)
    {
        BrainUtil.addToBrain(brain, Tasks.getMemories(), Tasks.getSensors());
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super LivingEntity>>> idle(
            final IPokemob pokemob, final float speed)
    {
        // Tasks for idle
        final List<IAIRunnable> aiList = Lists.newArrayList();
        Behavior<?> task;

        final List<Pair<Integer, ? extends BehaviorControl<? super LivingEntity>>> list = Lists.newArrayList();

        // Owner related tasks
        // Follow owner around
        aiList.add(new FollowOwnerTask(pokemob, 3, 8));
        // This one is outside as most things don't get this task.
        task = new WalkToTask(200);
        list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));

        // Send the event to let anyone edit the tasks if needed.
        PokecubeAPI.POKEMOB_BUS.post(new Init(pokemob, Init.Type.IDLE, aiList));

        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Behavior<LivingEntity> toAdd = null;
            if (run instanceof Behavior<?>) toAdd = (Behavior<LivingEntity>) run;
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super LivingEntity>>> combat(
            final IPokemob pokemob, final float speed)
    {
        // Tasks for combat
        final List<IAIRunnable> aiList = Lists.newArrayList();

        // combat tasks
        aiList.add(new SelectMoveTask(pokemob));
        // Attack stuff
        aiList.add(new UseAttacksTask(pokemob));
        // Dodge attacks
        aiList.add(new DodgeTask(pokemob));
        // Leap at things
        aiList.add(new LeapTask(pokemob));
        // Move around in combat
        aiList.add(new CicleTask(pokemob));
        // Attack stuff
        aiList.add(new ForgetTargetTask(pokemob));
        // Call for help task
        aiList.add(new CallForHelpTask(pokemob, (float) PokecubeCore.getConfig().hordeRateFactor));

        // Look for targets to kill
        final FindTargetsTask targetFind = new FindTargetsTask(pokemob);
        aiList.add(targetFind);
        pokemob.setTargetFinder(targetFind);

        final List<Pair<Integer, ? extends BehaviorControl<? super LivingEntity>>> list = Lists.newArrayList();
        // Send the event to let anyone edit the tasks if needed.
        PokecubeAPI.POKEMOB_BUS.post(new Init(pokemob, Init.Type.COMBAT, aiList));

        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Behavior<LivingEntity> toAdd = null;
            if (run instanceof Behavior<?>) toAdd = (Behavior<LivingEntity>) run;
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super LivingEntity>>> utility(
            final IPokemob pokemob, final float speed)
    {
        // Tasks for utilitiy
        final List<Pair<Integer, ? extends BehaviorControl<? super LivingEntity>>> list = Lists.newArrayList();
        final List<IAIRunnable> aiList = Lists.newArrayList();
        Behavior<?> task;

        // combat tasks
        final StoreTask ai = new StoreTask(pokemob);
        // Store things in chests
        aiList.add(ai);
        // Gather things from ground
        aiList.add(new GatherTask(pokemob, 32, ai));
        // Execute moves when told to
        aiList.add(new UseMoveTask(pokemob));
        // forget we were being hunted
        aiList.add(new ForgetHuntedByTask(pokemob, 100));

        task = new WalkToTask(200);
        list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
        // Send the event to let anyone edit the tasks if needed.
        PokecubeAPI.POKEMOB_BUS.post(new Init(pokemob, Init.Type.UTILITY, aiList));

        pokemob.getTasks().addAll(aiList);
        for (final IAIRunnable run : aiList)
        {
            Behavior<LivingEntity> toAdd = null;
            if (run instanceof Behavior<?>) toAdd = (Behavior<LivingEntity>) run;
            if (toAdd != null) list.add(Pair.of(run.getPriority(), toAdd));
        }
        return ImmutableList.copyOf(list);
    }

}
