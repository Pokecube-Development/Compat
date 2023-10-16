package pokecube.mod_compat.cobblemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import pokecube.core.client.render.mobs.overlays.Health;

public class ClientImpl
{
    public ClientImpl()
    {
        synchronized (Health.RENDER_HEALTH)
        {
            var old = Health.RENDER_HEALTH;
            Health.RENDER_HEALTH = (entity, viewPoint) -> {
                if (entity instanceof PokemonEntity) return false;
                return old.apply(entity, viewPoint);
            };
        }
    }
}