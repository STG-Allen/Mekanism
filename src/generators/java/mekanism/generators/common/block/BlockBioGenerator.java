package mekanism.generators.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IBlockSound;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.inventory.container.BioGeneratorContainer;
import mekanism.generators.common.tile.GeneratorsTileEntityTypes;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockBioGenerator extends BlockMekanism implements IHasGui<TileEntityBioGenerator>, IBlockElectric, IStateFacing, IHasInventory, IHasSecurity,
      IBlockSound, IHasTileEntity<TileEntityBioGenerator>, ISupportsComparator {

    private static final SoundEvent SOUND_EVENT = new SoundEvent(new ResourceLocation(Mekanism.MODID, "tile.gen.bio"));
    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape generator = MultipartUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 7, 16),//base
              makeCuboidShape(2, 7, 8, 14, 15, 15),//glass
              makeCuboidShape(0, 7, 0, 16, 16, 8),//back
              makeCuboidShape(3, 14.5, 14.5, 13, 15.5, 15.5),//bar
              makeCuboidShape(13, 7, 8, 16, 16, 16),//sideLeft
              makeCuboidShape(0, 7, 8, 3, 16, 16)//sideRight
        );
        //TODO: VoxelShapes, decide if we want to use the below method instead, it produces the same overall VoxelShape
        /*VoxelShape generator = MultipartUtils.combine(
              MultipartUtils.exclude(
                    makeCuboidShape(3, 15, 8, 13, 16, 16),
                    makeCuboidShape(3, 7, 15, 13, 16, 16)
              ),
              makeCuboidShape(3, 14.5, 14.5, 13, 15.5, 15.5)
        );*/
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = MultipartUtils.rotateHorizontal(generator, side);
        }
    }

    public BlockBioGenerator() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return SecurityUtils.canAccess(player, MekanismUtils.getTileEntity(world, pos)) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile != null && MekanismUtils.isActive(world, pos)) {
            if (tile.getDirection() == Direction.WEST) {
                world.addParticle(ParticleTypes.SMOKE, pos.getX() + .25, pos.getY() + .2, pos.getZ() + .5, 0.0D, 0.0D, 0.0D);
            } else if (tile.getDirection() == Direction.EAST) {
                world.addParticle(ParticleTypes.SMOKE, pos.getX() + .75, pos.getY() + .2, pos.getZ() + .5, 0.0D, 0.0D, 0.0D);
            } else if (tile.getDirection() == Direction.NORTH) {
                world.addParticle(ParticleTypes.SMOKE, pos.getX() + .5, pos.getY() + .2, pos.getZ() + .25, 0.0D, 0.0D, 0.0D);
            } else if (tile.getDirection() == Direction.SOUTH) {
                world.addParticle(ParticleTypes.SMOKE, pos.getX() + .5, pos.getY() + .2, pos.getZ() + .75, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tileEntity == null) {
            return false;
        }
        if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        return tileEntity.openGui(player);
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return bounds[getDirection(state).ordinal() - 2];
    }

    @Override
    public double getStorage() {
        return 160000;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SOUND_EVENT;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityBioGenerator tile) {
        return new ContainerProvider("mekanismgenerators.container.bio_generator", (i, inv, player) -> new BioGeneratorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityBioGenerator> getTileType() {
        return GeneratorsTileEntityTypes.BIO_GENERATOR.getTileEntityType();
    }
}