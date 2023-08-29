package mekanism.common.integration.computer;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileRedstone;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FactoryTest {
    @BeforeAll
    static void loadRegistry() {
        FactoryRegistry.load();
    }

    @Test
    void testBasicBinding() {
        Set<Class<?>> classes = FactoryRegistry.getKnownClasses();
        Assertions.assertFalse(classes.isEmpty(), "Factory not loaded");
        DummySubject subject = new DummySubject();
        ComputerMethodFactory.IS_MOD_LOADED = s -> true;
        for (Class<?> aClass : classes) {
            NoOpMethodHolder holder = new NoOpMethodHolder();
            FactoryRegistry.bindTo(holder, subject, aClass);
            Assertions.assertFalse(holder.methods.isEmpty(), ()-> aClass.getSimpleName()+" handler was empty");
            System.out.println(aClass.getSimpleName()+": "+holder.methods.size());
        }
    }

    private static class NoOpMethodHolder extends BoundMethodHolder {}

    private static class DummySubject implements ITileDirectional, IMekanismStrictEnergyHandler, ITileRedstone, IComparatorSupport {

        @Override
        public void onContentsChanged() {

        }

        @Override
        public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
            return Collections.emptyList();
        }

        @Override
        public int getRedstoneLevel() {
            return 0;
        }

        @Override
        public int getCurrentRedstoneLevel() {
            return 0;
        }

        @Override
        public RedstoneControl getControlType() {
            return RedstoneControl.DISABLED;
        }

        @Override
        public void setControlType(RedstoneControl type) {

        }

        @Override
        public boolean isPowered() {
            return false;
        }

        @Override
        public boolean wasPowered() {
            return false;
        }

        @Override
        public void setFacing(@NotNull Direction direction) {

        }

        @Override
        public @NotNull Direction getDirection() {
            return Direction.NORTH;
        }
    }
}
