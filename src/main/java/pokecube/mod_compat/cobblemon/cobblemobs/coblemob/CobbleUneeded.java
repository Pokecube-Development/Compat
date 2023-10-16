package pokecube.mod_compat.cobblemon.cobblemobs.coblemob;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import net.minecraft.world.item.ItemStack;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;

public abstract class CobbleUneeded extends CobbleOwned
{

    public CobbleUneeded(PokemonEntity mob)
    {
        super(mob);
    }

    @Override
    public boolean canEvolve(ItemStack arg0)
    {
        return false;
    }

    @Override
    public void setSexe(byte sexe)
    {}

    // This section is for things which are un-needed here, as are handled by
    // the cobblemon itself, or via getCobbled()

    @Override
    public FormeHolder getCustomHolder()
    {
        return null;
    }

    @Override
    public void setCustomHolder(FormeHolder holder)
    {}

    @Override
    public IPokemob setPokedexEntry(PokedexEntry arg0)
    {
        return this;
    }

    @Override
    public void setBasePokedexEntry(PokedexEntry newEntry)
    {}

    // Below are un-used by us, as are handled elsewhere by the mob

    @Override
    public float getHeading()
    {
        return 0;
    }

    @Override
    public void setHeading(float arg0)
    {}

}
