package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.IColor;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.block.property.PropertyConnection;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;

public class BlockStateHelper {

    public static final PropertyDirection facingProperty = PropertyDirection.create("facing");
    public static final PropertyDirection horizontalFacingProperty = PropertyDirection.create("facing", Plane.HORIZONTAL);
    public static final PropertyBool activeProperty = PropertyBool.create("active");
    //NOTE: This currently is only using the set of colors the transporter supports as it is the only thing that needs this
    // There is a method to create this supporting all colors but it is currently unused
    public static final PropertyColor colorProperty = PropertyColor.createTransporter("color");
    //Sided pipe properties
    public static final PropertyConnection downConnectionProperty = PropertyConnection.create("down");
    public static final PropertyConnection upConnectionProperty = PropertyConnection.create("up");
    public static final PropertyConnection northConnectionProperty = PropertyConnection.create("north");
    public static final PropertyConnection southConnectionProperty = PropertyConnection.create("south");
    public static final PropertyConnection westConnectionProperty = PropertyConnection.create("west");
    public static final PropertyConnection eastConnectionProperty = PropertyConnection.create("east");
    //Cardboard Box storage
    public static final PropertyBool storageProperty = PropertyBool.create("storage");

    public static BlockStateContainer getBlockState(Block block) {
        List<IProperty> properties = new ArrayList<>();
        if (block instanceof IStateFacing) {
            if (((IStateFacing) block).supportsAll()) {
                properties.add(facingProperty);
            } else {
                properties.add(horizontalFacingProperty);
            }
        }
        if (block instanceof IStateActive) {
            properties.add(activeProperty);
        }
        if (block instanceof IStateColor) {
            properties.add(colorProperty);
        }
        if (block instanceof IStateStorage) {
            properties.add(storageProperty);
        }
        if (block instanceof IStateConnection) {
            properties.add(downConnectionProperty);
            properties.add(upConnectionProperty);
            properties.add(northConnectionProperty);
            properties.add(southConnectionProperty);
            properties.add(westConnectionProperty);
            properties.add(eastConnectionProperty);
        }
        if (properties.isEmpty()) {
            return new BlockStateContainer(block);
        }
        return new BlockStateContainer(block, properties.toArray(new IProperty[0]));
    }

    public static BlockState getActualState(@Nonnull Block block, @Nonnull BlockState state, @Nonnull TileEntity tile) {
        if (block instanceof IStateFacing) {
            Direction facing = getFacing(tile);
            if (facing != null) {
                if (((IStateFacing) block).supportsAll()) {
                    state = state.withProperty(facingProperty, facing);
                } else if (facing != Direction.DOWN && facing != Direction.UP) {
                    state = state.withProperty(horizontalFacingProperty, facing);
                }
            }
        }
        if (block instanceof IStateActive) {
            state = state.withProperty(activeProperty, ((IStateActive) block).isActive(tile));
        }
        if (block instanceof IStateColor) {
            state = state.withProperty(BlockStateHelper.colorProperty, getColor(tile));
        }
        if (block instanceof IStateStorage) {
            state = state.withProperty(storageProperty, isStoring(tile));
        }
        if (block instanceof IStateConnection) {
            //Add all the different connection types
            state = state.withProperty(downConnectionProperty, getStateConnection(tile, Direction.DOWN));
            state = state.withProperty(upConnectionProperty, getStateConnection(tile, Direction.UP));
            state = state.withProperty(northConnectionProperty, getStateConnection(tile, Direction.NORTH));
            state = state.withProperty(southConnectionProperty, getStateConnection(tile, Direction.SOUTH));
            state = state.withProperty(westConnectionProperty, getStateConnection(tile, Direction.WEST));
            state = state.withProperty(eastConnectionProperty, getStateConnection(tile, Direction.EAST));
        }
        return state;
    }

    @Nullable
    private static Direction getFacing(@Nonnull TileEntity tile) {
        //TODO: Make Glow Panel implement ITileDirectional
        if (tile instanceof ITileDirectional) {
            ITileDirectional directional = (ITileDirectional) tile;
            if (directional.isDirectional()) {
                return directional.getDirection();
            }
        } else if (tile instanceof TileEntityGlowPanel) {
            return ((TileEntityGlowPanel) tile).side;
        }
        return null;
    }

    @Nonnull
    private static IColor getColor(@Nonnull TileEntity tile) {
        EnumColor color = null;
        if (tile instanceof TileEntitySidedPipe) {
            color = ((TileEntitySidedPipe) tile).getRenderColor();
        }
        return color == null ? EnumColor.NONE : color;
    }

    private static boolean isStoring(@Nonnull TileEntity tile) {
        if (tile instanceof TileEntityCardboardBox) {
            return ((TileEntityCardboardBox) tile).storedData != null;
        }
        return false;
    }

    @Nonnull
    private static ConnectionType getStateConnection(@Nonnull TileEntity tile, @Nonnull Direction side) {
        if (tile instanceof TileEntitySidedPipe) {
            return ((TileEntitySidedPipe) tile).getConnectionType(side);
        }
        return ConnectionType.NONE;
    }
}