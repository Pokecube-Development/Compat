package pokecube.mod_compat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import thut.api.util.JsonUtil;

public class CompatConfig
{
    public static CompatConfig loadConfig()
    {
        // We put the config option in config/pokecube/
        Path folder = FMLPaths.CONFIGDIR.get().resolve("pokecube");
        // Ensure the folder exists for it
        folder.toFile().mkdirs();
        Path config_path = folder.resolve("compat.json");
        final File dir = config_path.toFile();

        CompatConfig config = new CompatConfig();

        if (config_path.toFile().exists())
        {
            try
            {
                FileInputStream inS = new FileInputStream(dir);
                var inSR = new InputStreamReader(inS);
                config = JsonUtil.gson.fromJson(inSR, CompatConfig.class);
                config.init();
                inSR.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        saveConfig(config);
        return config;
    }

    public static void saveConfig(CompatConfig config)
    {
        // We put the config option in config/pokecube/
        Path folder = FMLPaths.CONFIGDIR.get().resolve("pokecube");
        // Ensure the folder exists for it
        folder.toFile().mkdirs();
        Path config_path = folder.resolve("compat.json");
        final File dir = config_path.toFile();

        // Re-save the config file to ensure standard format, etc
        final String json = JsonUtil.gson.toJson(config);
        try
        {
            FileOutputStream outS = new FileOutputStream(dir);
            outS.write(json.getBytes());
            outS.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    List<String> disabled_compats = new ArrayList<>();

    Set<String> _disabled = new HashSet<>();

    private void init()
    {
        _disabled.clear();
        _disabled.addAll(disabled_compats);
    }

    public boolean allowCompat(String modid)
    {
        if (_disabled.contains(modid)) return false;
        return ModList.get().isLoaded(modid);
    }
}
