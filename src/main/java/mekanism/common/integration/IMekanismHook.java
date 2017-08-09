package mekanism.common.integration;

/**
 * A module that is run during the startup process in order to register something with another mod,
 * or to register another mod's things with Mekanism.
 *
 * An instance will be created if the MODID supplied in the {@link MekanismHooks} enum is loaded.
 */
public interface IMekanismHook {

    /**
     * Perform functions that are required in the Init phase (send IMC, call other mod's APIs, etc).
     */
    default void init() {}

    /**
     * Perform functions that are required in the Post Init phase (add/remove things from Mekanism).
     */
    default void postInit() {}
}
