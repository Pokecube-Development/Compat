package pokecube.mod_compat.cobblemon.cobblemobs.coblemob;

import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.world.entity.LivingEntity;
import pokecube.core.impl.PokecubeMod;

public abstract class CobbleOwned extends CobbleBase
{

    public CobbleOwned(PokemonEntity mob)
    {
        super(mob);
    }

    @Override
    public LivingEntity getOwner()
    {
        return cobblemon.getOwner();
    }

    @Override
    public UUID getOwnerId()
    {
        UUID owner = cobblemon.getOwnerUUID();
        return owner == null ? PokecubeMod.fakeUUID : owner;
    }

    @Override
    public void setOwner(LivingEntity arg0)
    {
        if (arg0 != null) cobblemon.setOwnerUUID(arg0.getUUID());
        else cobblemon.setOwnerUUID(null);
    }

    @Override
    public void setOwner(UUID arg0)
    {
        cobblemon.setOwnerUUID(arg0);
    }

    @Override
    public boolean isPlayerOwned()
    {
        Pokemon cobbled = this.getCobbled();
        return cobbled.isPlayerOwned();
    }
}
