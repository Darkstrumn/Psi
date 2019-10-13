/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [17/02/2016, 18:23:26 (GMT)]
 */
package vazkii.psi.common.block;

import com.google.common.collect.Sets;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.block.BlockModContainer;
import vazkii.psi.common.block.base.IPsiBlock;
import vazkii.psi.common.block.tile.TileConjured;
import vazkii.psi.common.lib.LibBlockNames;
import vazkii.psi.common.lib.LibMisc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(modid = LibMisc.MOD_ID)
public class BlockConjured extends BlockModContainer implements IPsiBlock {

	public static final PropertyBool SOLID = PropertyBool.create("solid");
	public static final PropertyBool LIGHT = PropertyBool.create("light");
	public static final PropertyBool BLOCK_UP = PropertyBool.create("block_up");
	public static final PropertyBool BLOCK_DOWN = PropertyBool.create("block_down");
	public static final PropertyBool BLOCK_NORTH = PropertyBool.create("block_north");
	public static final PropertyBool BLOCK_SOUTH = PropertyBool.create("block_south");
	public static final PropertyBool BLOCK_WEST = PropertyBool.create("block_west");
	public static final PropertyBool BLOCK_EAST = PropertyBool.create("block_east");

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private static final Set<BlockPos> needsParticleUpdate = Sets.newHashSet();

	protected static final AxisAlignedBB LIGHT_AABB = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
	
	public BlockConjured() {
		super(LibBlockNames.CONJURED, Material.GLASS);
		setDefaultState(makeDefaultState());
		setLightOpacity(0);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		TileEntity inWorld = worldIn.getTileEntity(pos);
		if (inWorld instanceof TileConjured)
			needsParticleUpdate.add(pos.toImmutable());
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void fireParticles(TickEvent.ClientTickEvent event) {
		if (event.side.isClient() && event.phase == TickEvent.Phase.END) {
			World world = Minecraft.getMinecraft().world;
			Entity viewPoint = Minecraft.getMinecraft().getRenderViewEntity();
			if (viewPoint == null)
				viewPoint = Minecraft.getMinecraft().player;
			final Entity view = viewPoint;

			needsParticleUpdate.removeIf((pos) -> {
				if (view == null || world == null)
					return true;

				if (world.isBlockLoaded(pos) && view.getDistanceSq(pos) <= 64 * 64) {
					TileEntity inWorld = world.getTileEntity(pos);
					if (inWorld instanceof TileConjured) {
						((TileConjured) inWorld).doParticles();
						return false;
					}
				}

				return true;
			});
		}
	}

	@SubscribeEvent
	public static void ignoreMissingItem(RegistryEvent.MissingMappings<Item> event) {
		for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getMappings())
			if (mapping.key.getPath().equals(LibBlockNames.CONJURED))
				mapping.ignore();
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) {
		worldIn.setBlockToAir(pos);
	}

	@Override
	public BlockItem createItemBlock(ResourceLocation res) {
		return null;
	}

	public BlockState makeDefaultState() {
		return getStateFromMeta(0);
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, getAllProperties());
	}

	@Override
	public IProperty[] getIgnoredProperties() {
		return getAllProperties();
	}

	public IProperty[] getAllProperties() {
		return new IProperty[] { SOLID, LIGHT, BLOCK_UP, BLOCK_DOWN, BLOCK_NORTH, BLOCK_SOUTH, BLOCK_WEST, BLOCK_EAST };
	}

	@Nonnull
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isFullCube(BlockState state) {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isFullBlock(BlockState state) {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}

	@Override
	public int quantityDropped(Random random) {
		return 0;
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, @Nonnull BlockState state, PlayerEntity player) {
		return false;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public BlockState getStateFromMeta(int meta) {
		BlockState state = getDefaultState();
		return state.withProperty(SOLID, (meta & 0b01) != 0).withProperty(LIGHT, (meta & 0b10) != 0);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return (state.getValue(SOLID) ? 0b01 : 0) | (state.getValue(LIGHT) ? 0b10 : 0);
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public BlockState getActualState(@Nonnull BlockState state, IBlockAccess worldIn, BlockPos pos) {
		BlockState origState = state;
		state = state.withProperty(BLOCK_UP, worldIn.getBlockState(pos.up()).equals(origState));
		state = state.withProperty(BLOCK_DOWN, worldIn.getBlockState(pos.down()).equals(origState));
		state = state.withProperty(BLOCK_NORTH, worldIn.getBlockState(pos.north()).equals(origState));
		state = state.withProperty(BLOCK_SOUTH, worldIn.getBlockState(pos.south()).equals(origState));
		state = state.withProperty(BLOCK_WEST, worldIn.getBlockState(pos.west()).equals(origState));
		state = state.withProperty(BLOCK_EAST, worldIn.getBlockState(pos.east()).equals(origState));

		return state;
	}

	@Override
	public int getLightValue(BlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(LIGHT) ? 15 : 0;
	}

	@Nullable
	@Override
	@SuppressWarnings("deprecation")
	public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		return blockState.getValue(SOLID) ? FULL_BLOCK_AABB : NULL_AABB;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(SOLID) ? FULL_BLOCK_AABB : LIGHT_AABB;
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face) {
		return BlockFaceShape.UNDEFINED;
	}
	
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull BlockState state) {
		return new TileConjured();
	}


}
