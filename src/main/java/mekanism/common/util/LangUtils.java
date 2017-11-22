package mekanism.common.util;

import java.util.IllegalFormatException;

import mekanism.api.gas.GasStack;
import mekanism.common.Mekanism;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;

public final class LangUtils
{
	public static String transOnOff(boolean b)
	{
		return LangUtils.localize("gui." + (b ? "on" : "off"));
	}
	
	public static String transYesNo(boolean b)
	{
		return LangUtils.localize("tooltip." + (b ? "yes" : "no"));
	}
	
	public static String transOutputInput(boolean b)
	{
		return LangUtils.localize("gui." + (b ? "output" : "input"));
	}

	public static String localizeFluidStack(FluidStack fluidStack)
	{
		return (fluidStack == null || fluidStack.getFluid() == null ) ? null : fluidStack.getFluid().getLocalizedName(fluidStack);
	}

	public static String localizeGasStack(GasStack gasStack)
	{
		return (gasStack == null || gasStack.getGas() == null ) ? null : gasStack.getGas().getLocalizedName();
	}

	/**
	 * Localizes the defined string.
	 * @param s - string to localized
	 * @return localized string
	 */
	public static String localize(String s)
	{
		String t = Mekanism.proxy.localise(s);
		if (t.startsWith("Format error: ")){
			throw new IllegalStateException(t);
		}
		return t;
	}

	public static String localizeWithFormat(String key, Object... format)
	{
		return Mekanism.proxy.localiseFormatted(key, format);
	}
}
