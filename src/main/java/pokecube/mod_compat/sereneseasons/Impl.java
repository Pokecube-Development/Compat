package pokecube.mod_compat.sereneseasons;

import net.minecraft.world.level.Level;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import pokecube.api.data.spawns.matchers.BaseMatcher;
import pokecube.api.data.spawns.matchers.MatcherLoaders;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.Season.SubSeason;
import sereneseasons.api.season.Season.TropicalSeason;
import sereneseasons.api.season.SeasonHelper;

public class Impl
{
    public static class SeasonMatch extends BaseMatcher
    {
        public String season = "";
        public String sub_season = "";
        public String tropical_season = "";

        private Season _season = null;
        private SubSeason _sub_season = null;
        private TropicalSeason _tropical_season = null;

        @Override
        public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
        {
            if (!(checker.world instanceof Level level)) return MatchResult.FAIL;
            ISeasonState seasons = SeasonHelper.getSeasonState(level);
            if (_season != null && seasons.getSeason() != _season)
            {
                return MatchResult.FAIL;
            }
            if (_sub_season != null && seasons.getSubSeason() != _sub_season)
            {
                return MatchResult.FAIL;
            }
            if (_tropical_season != null && seasons.getTropicalSeason() != _tropical_season)
            {
                return MatchResult.FAIL;
            }
            return MatchResult.SUCCEED;
        }

        @Override
        public void init()
        {
            _season = getSeason(season);
            _sub_season = getSSeason(sub_season);
            _tropical_season = getTSeason(tropical_season);
        }

        private Season getSeason(final String name)
        {
            for (final Season c : Season.values()) if (c.name().equalsIgnoreCase(name)) return c;
            return null;
        }

        private SubSeason getSSeason(final String name)
        {
            for (final SubSeason c : SubSeason.values()) if (c.name().equalsIgnoreCase(name)) return c;
            return null;
        }

        private TropicalSeason getTSeason(final String name)
        {
            for (final TropicalSeason c : TropicalSeason.values()) if (c.name().equalsIgnoreCase(name)) return c;
            return null;
        }
    }

    public static void register()
    {
        MatcherLoaders.matchClasses.put("serene_season", SeasonMatch.class);
    }
}
