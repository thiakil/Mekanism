package mekanism.common.chunkloading;

import java.util.List;

import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.component.TileComponentChunkLoader;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ChunkManager implements LoadingCallback
{
	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{
		if (MekanismConfig.general.allowChunkloading)
		{
			for (Ticket ticket : tickets)
			{
				NBTTagCompound data = ticket.getModData();
				int x = data.getInteger("xCoord");
				int y = data.getInteger("yCoord");
				int z = data.getInteger("zCoord");

				TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

				if (tileEntity instanceof IChunkLoader)
				{
					TileComponentChunkLoader chunkLoader = ((IChunkLoader) tileEntity).getChunkLoader();
					if (chunkLoader.canOperate())
					{
						chunkLoader.initTicketIfNeeded();
						chunkLoader.refreshChunkSet();
						chunkLoader.forceChunks(ticket);
					}
				}
			}
		}
	}
}
