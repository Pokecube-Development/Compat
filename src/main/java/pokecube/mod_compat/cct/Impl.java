package pokecube.mod_compat.cct;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.blocks.commander.CommanderTile;
import pokecube.adventures.blocks.genetics.extractor.ExtractorTile;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.warp_pad.WarpPadTile;
import pokecube.api.PokecubeAPI;
import pokecube.core.blocks.tms.TMTile;
import pokecube.mod_compat.cct.modules.Commander;
import pokecube.mod_compat.cct.modules.Extractor;
import pokecube.mod_compat.cct.modules.Siphon;
import pokecube.mod_compat.cct.modules.Splicer;
import pokecube.mod_compat.cct.modules.TM;
import pokecube.mod_compat.cct.modules.Warppad;

public class Impl
{

    public static void register()
    {
        PokecubeAPI.LOGGER.info("Registering Pokecube CC Peripherals.");
        ComputerCraftAPI.registerPeripheralProvider(new PokecubePeripherals());
    }

    public static class PokecubePeripherals implements IPeripheralProvider
    {
        public IPeripheral getPeri(final Level world, final BlockPos pos, final Direction side)
        {
            final BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof CommanderTile) return new Commander((CommanderTile) tile);
            if (tile instanceof TMTile) return new TM((TMTile) tile);
            if (tile instanceof SplicerTile) return new Splicer((SplicerTile) tile);
            if (tile instanceof ExtractorTile) return new Extractor((ExtractorTile) tile);
            if (tile instanceof WarpPadTile) return new Warppad((WarpPadTile) tile);
            if (tile instanceof SiphonTile) return new Siphon((SiphonTile) tile);
            return null;
        }

        @Override
        public LazyOptional<IPeripheral> getPeripheral(final Level world, final BlockPos pos, final Direction side)
        {
            final IPeripheral peri = this.getPeri(world, pos, side);
            return peri == null ? LazyOptional.empty() : LazyOptional.of(() -> peri);
        }
    }
}
