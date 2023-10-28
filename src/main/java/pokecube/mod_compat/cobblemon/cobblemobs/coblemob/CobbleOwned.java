package pokecube.mod_compat.cobblemon.cobblemobs.coblemob;

import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.ThutCore;
import thut.core.common.world.mobs.data.types.Data_UUID;

public abstract class CobbleOwned extends CobbleBase
{
    private int OWNERID;

    LivingEntity ownerMob;

    boolean playerOwned = false;

    public CobbleOwned(PokemonEntity mob)
    {
        super(mob);
    }

    @Override
    public void setDataSync(DataSync sync)
    {
        super.setDataSync(sync);
        OWNERID = sync.register(new Data_UUID(), null);
    }

    @Override
    public LivingEntity getOwner()
    {
        var id = this.getOwnerId();
        if (ownerMob == null && id != null)
        {
            var owner = this.getEntity().level().getPlayerByUUID(id);
            if (owner != null) this.ownerMob = owner;
            else if (this.getEntity().level() instanceof ServerLevel level)
            {
                var entity = level.getEntity(id);
                if (entity instanceof LivingEntity living) ownerMob = living;
            }
        }
        return this.ownerMob;
    }

    @Override
    public UUID getOwnerId()
    {
        return this.dataSync().get(OWNERID);
    }

    @Override
    public void setOwner(LivingEntity e)
    {
        this.playerOwned = e instanceof Player;
        this.ownerMob = e;
        if (e != null) this.setOwner(e.getUUID());
        else this.setOwner((UUID) null);
    }

    @Override
    public void setOwner(UUID id)
    {
        var all = this.dataSync().getAll();
        all.get(OWNERID).setDirty(true);
        this.dataSync().set(OWNERID, id);
    }

    @Override
    public boolean isPlayerOwned()
    {
        this.playerOwned = this.playerOwned || this.getOwner() instanceof Player;
        return this.playerOwned;
    }

    @Override
    public CompoundTag write()
    {
        var nbt = super.write();
        if (this.getOwnerId() != null)
        {
            nbt.putUUID("o", this.getOwnerId());
            nbt.putBoolean("p", this.playerOwned);
        }
        return nbt;
    }

    @Override
    public void read(CompoundTag nbt)
    {
        super.read(nbt);
        if (nbt.contains("p"))
        {
            this.playerOwned = nbt.getBoolean("p");
            try
            {
                this.setOwner(nbt.getUUID("o"));
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.error("Error loading in UUID");
            }
        }
    }
}
