package mekanism.common.config;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mekanism.common.Mekanism;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.TempType;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MekanismConfig
{
	public static class general
	{
		public static TypeConfigManager machinesManager = new TypeConfigManager();
		
		public static boolean updateNotifications = true;
		public static boolean controlCircuitOreDict = true;
		public static boolean logPackets = false;
		public static boolean dynamicTankEasterEgg = false;
		public static boolean voiceServerEnabled = true;
		public static boolean cardboardSpawners = true;
		public static boolean enableWorldRegeneration = true;
		public static boolean spawnBabySkeletons = true;
		public static int obsidianTNTBlastRadius = 12;
		public static int osmiumPerChunk = 12;
		public static int copperPerChunk = 16;
		public static int tinPerChunk = 14;
		public static int saltPerChunk = 2;
		public static int obsidianTNTDelay = 100;
		public static int UPDATE_DELAY = 10;
		public static int VOICE_PORT = 36123;
		public static int maxUpgradeMultiplier = 10;
		public static int userWorldGenVersion = 0;
		public static double ENERGY_PER_REDSTONE = 10000;
		public static int ETHENE_BURN_TIME = 40;
		public static double DISASSEMBLER_USAGE = 10;
		public static EnergyType energyUnit = EnergyType.J;
		public static TempType tempUnit =	TempType.K;
		public static double TO_IC2;
		public static double TO_RF;
		public static double TO_TESLA;
		public static double TO_FORGE;
		public static double FROM_H2;
		public static double FROM_IC2;
		public static double FROM_RF;
		public static double FROM_TESLA;
		public static double FROM_FORGE;
		public static int laserRange;
		public static double laserEnergyNeededPerHardness;
		public static double minerSilkMultiplier = 6;
		public static boolean blacklistIC2;
		public static boolean blacklistRF;
		public static boolean blacklistTesla;
		public static boolean blacklistForge;
		public static boolean destroyDisabledBlocks;
		public static boolean prefilledGasTanks;
		public static double armoredJetpackDamageRatio;
		public static int armoredJetpackDamageMax;
		public static boolean aestheticWorldDamage;
		public static boolean opsBypassRestrictions;
		public static double thermalEvaporationSpeed;
		public static int maxJetpackGas;
		public static int maxScubaGas;
		public static int maxFlamethrowerGas;
		public static int maxPumpRange;
		public static boolean pumpWaterSources;
		public static int maxPlenisherNodes;
		public static double evaporationHeatDissipation = 0.02;
		public static double evaporationTempMultiplier = 0.1;
		public static double evaporationSolarMultiplier = 0.2;
		public static double evaporationMaxTemp = 3000;
		public static double energyPerHeat = 1000;
		public static double maxEnergyPerSteam = 100;
		public static double superheatingHeatTransfer = 10000;
		public static double heatPerFuelTick = 4;
		public static boolean allowTransmitterAlloyUpgrade;
		public static boolean allowChunkloading;
		public static boolean allowProtection = true;
		public static int portableTeleporterDelay;
		public static double quantumEntangloporterEnergyTransfer;
	}

	public static class client
	{
		public static boolean enablePlayerSounds = true;
		public static boolean enableMachineSounds = true;
		public static boolean holidays = true;
		public static float baseSoundVolume = 1F;
		public static boolean machineEffects = true;
		public static boolean oldTransmitterRender = false;
		public static boolean replaceSoundsWhenResuming = true;
		public static boolean enableAmbientLighting;
		public static int ambientLightingLevel;
		public static boolean opaqueTransmitters = false;
		public static boolean allowConfiguratorModeScroll;
	}

	public static class usage
	{
		public static double enrichmentChamberUsage;
		public static double osmiumCompressorUsage;
		public static double combinerUsage;
		public static double crusherUsage;
		public static double metallurgicInfuserUsage;
		public static double purificationChamberUsage;
		public static double energizedSmelterUsage;
		public static double digitalMinerUsage;
		public static double electricPumpUsage;
		public static double rotaryCondensentratorUsage;
		public static double oxidationChamberUsage;
		public static double chemicalInfuserUsage;
		public static double chemicalInjectionChamberUsage;
		public static double precisionSawmillUsage;
		public static double chemicalDissolutionChamberUsage;
		public static double chemicalWasherUsage;
		public static double chemicalCrystallizerUsage;
		public static double seismicVibratorUsage;
		public static double pressurizedReactionBaseUsage;
		public static double fluidicPlenisherUsage;
		public static double laserUsage;
		public static double gasCentrifugeUsage;
		public static double heavyWaterElectrolysisUsage;
		public static double formulaicAssemblicatorUsage;
	}

	public static class generators
	{
		public static TypeConfigManager generatorsManager = new TypeConfigManager();
		
		public static double advancedSolarGeneration;
		public static double bioGeneration;
		public static double heatGeneration;
		public static double heatGenerationLava;
		public static double heatGenerationNether;
		public static double solarGeneration;

		public static double windGenerationMin;
		public static double windGenerationMax;

		public static int windGenerationMinY;
		public static int windGenerationMaxY;
		
		public static int turbineBladesPerCoil;
		public static double turbineVentGasFlow;
		public static double turbineDisperserGasFlow;
		public static int condenserRate;
		
		public static double energyPerFusionFuel;
	}

	public static class tools
	{
		public static double armorSpawnRate;
	}

	protected enum SyncAdaptors {
		BOOLEAN(Boolean.class, ByteBuf::readBoolean, ByteBuf::writeBoolean),
		INT(Integer.class, ByteBuf::readInt, ByteBuf::writeInt),
		DOUBLE(Double.class, ByteBuf::readDouble, ByteBuf::writeDouble),
		FLOAT(Float.class, ByteBuf::readFloat, ByteBuf::writeFloat),
		ENERGY_TYPE(EnergyType.class, buf->EnergyType.values()[buf.readByte()], (byteBuf, energyType) -> byteBuf.writeByte(energyType.ordinal())),
		TEMP_TYPE(TempType.class, buf->TempType.values()[buf.readByte()], (byteBuf, tempType) -> byteBuf.writeByte(tempType.ordinal())),
		TYPE_CONFIG_MANAGER(TypeConfigManager.class, TypeConfigManager::readFromBuffer, ((byteBuf, typeConfigManager) -> typeConfigManager.writeToBuffer(byteBuf))),
		//MAP(Map.class, ),
		;

		private Function<ByteBuf,Object> readHandler;
		private BiFunction<ByteBuf, Object, ByteBuf> writeHandler;
		private Class<?> clazz;

		@SuppressWarnings("unchecked")
		<T> SyncAdaptors(Class<T> clazz, Function<ByteBuf,T> read, BiFunction<ByteBuf, T, ByteBuf> write){
			this.readHandler = (Function<ByteBuf,Object>)read;
			this.writeHandler = (BiFunction<ByteBuf, Object, ByteBuf>)write;
			this.clazz = clazz;
		}

		public static <T> void writeToPacket(T val, ByteBuf buf){
			for (SyncAdaptors adaptor : values()){
				if (adaptor.clazz.isInstance(val)){
					buf.writeByte(adaptor.ordinal());
					adaptor.writeHandler.apply(buf, val);
					return;
				}
			}
			throw new IllegalArgumentException("Handler not found for supplied type; "+val.getClass());
		}
	}

	public static ByteBuf writeToBuffer(Class<?> clazz, ByteBuf data){
		Field[] fields = clazz.getDeclaredFields();
		int count = 0;
		int countPos = data.writerIndex();
		data.writeInt(0);//dummy val, updated later
		for (Field f : fields){
			int modifiers = f.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)){
				ByteBufUtils.writeUTF8String(data, clazz.getName()+"."+f.getName());
				try {
					SyncAdaptors.writeToPacket(f.get(null), data);
				} catch (IllegalAccessException e){
					throw new IllegalStateException("Could not read field value, though it should be public", e);
				}
				count++;
			}
		}
		int endPos = data.writerIndex();
		data.writerIndex(countPos);
		data.writeInt(count);
		data.writerIndex(endPos);
		return data;
	}

	public static ByteBuf writeAllToBuffer(ByteBuf data){
		writeToBuffer(MekanismConfig.general.class, data);
		writeToBuffer(MekanismConfig.usage.class, data);
		writeToBuffer(MekanismConfig.generators.class, data);
		writeToBuffer(MekanismConfig.tools.class, data);
		return data;
	}

	public static void readFromBuffer(Class<?> clazz, ByteBuf data){
		Map<String, Object> values = new HashMap<>();
		int count = data.readInt();
		for (int i = 0; i<count; i++){
			String name = ByteBufUtils.readUTF8String(data);
			int type = data.readByte();
			values.put(name, SyncAdaptors.values()[type].readHandler.apply(data));
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields){
			int modifiers = f.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)){
				String name = clazz.getName()+"."+f.getName();
				if (!values.containsKey(name)){
					//System.err.printf("Key %s not found in data\n", name);
					Mekanism.logger.error("Key {} not found in data", name);
					continue;
				}
				try {
					Object val = values.get(name);
					if (!f.getType().isInstance(val) && !(f.getType().isPrimitive() && val.getClass().getField("TYPE").get(val) == f.getType())){
						//System.err.printf("Value class is not the same for key %s, found %s, expected %s\n", name, f.getType().getName(), val.getClass().toGenericString());
						Mekanism.logger.error("Value class is not the same for key {}, found {}, expected {}", name, f.getType().getName(), values.get(name).getClass().toGenericString());
						continue;
					}
					f.set(null, val);
				} catch (NoSuchFieldException e) {
					//System.err.println("Could not get primitive type of what should be a Boxed class");
					Mekanism.logger.error("Could not get primitive type of what should be a Boxed class. {}", name);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Could not set our value, though it should be public.", e);
				}
			}
		}
	}

	public static void readAllFromBuffer(ByteBuf data){
		readFromBuffer(MekanismConfig.general.class, data);
		readFromBuffer(MekanismConfig.usage.class, data);
		readFromBuffer(MekanismConfig.generators.class, data);
		readFromBuffer(MekanismConfig.tools.class, data);
	}

	/*public static void main(String[] args){
		ByteBuf buf = Unpooled.buffer();
		long start = System.nanoTime();
		System.out.println(writeAllToBuffer(buf).toString());
		long end = System.nanoTime();
		System.out.printf("Took %dms\n", TimeUnit.NANOSECONDS.toMillis(end-start));
		//buf.resetReaderIndex()
		start = System.nanoTime();
		readAllFromBuffer(buf);
		end = System.nanoTime();
		System.out.printf("Took %dms\n", TimeUnit.NANOSECONDS.toMillis(end-start));
	}*/

}
