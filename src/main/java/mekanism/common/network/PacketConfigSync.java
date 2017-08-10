package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.base.IModule;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.network.PacketConfigSync.ConfigSyncMessage;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketConfigSync implements IMessageHandler<ConfigSyncMessage, IMessage>
{
	@Override
	public IMessage onMessage(ConfigSyncMessage message, MessageContext context) 
	{
		return null;
	}
	
	public static class ConfigSyncMessage implements IMessage
	{
		public ConfigSyncMessage() {}
		
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			MekanismConfig.writeToBuffer(MekanismConfig.general.class, dataStream);
			MekanismConfig.writeToBuffer(MekanismConfig.usage.class, dataStream);

			Tier.writeConfig(dataStream);
	
			try {
				for(IModule module : Mekanism.modulesLoaded)
				{
					module.writeConfig(dataStream);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			MekanismConfig.readFromBuffer(MekanismConfig.general.class, dataStream);
			MekanismConfig.readFromBuffer(MekanismConfig.usage.class, dataStream);

			Tier.readConfig(dataStream);
	
			try {
				for(IModule module : Mekanism.modulesLoaded)
				{
					module.readConfig(dataStream);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
	
			Mekanism.proxy.onConfigSync(true);
		}
	}
}
