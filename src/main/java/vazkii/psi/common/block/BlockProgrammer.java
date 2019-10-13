/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [12/01/2016, 17:41:48 (GMT)]
 */
package vazkii.psi.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Rarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import vazkii.arl.block.BlockFacing;
import vazkii.psi.api.internal.VanillaPacketDispatcher;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.base.IPsiBlock;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.core.PsiCreativeTab;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler.PlayerData;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.lib.LibBlockNames;
import vazkii.psi.common.lib.LibGuiIDs;

import javax.annotation.Nonnull;
import java.util.UUID;

public class BlockProgrammer extends BlockFacing implements IPsiBlock {

	public static final PropertyBool ENABLED = PropertyBool.create("enabled");

	public BlockProgrammer() {
		super(LibBlockNames.PROGRAMMER, Material.IRON);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.METAL);
		setCreativeTab(PsiCreativeTab.INSTANCE);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = playerIn.getHeldItem(hand);
		TileProgrammer programmer = (TileProgrammer) worldIn.getTileEntity(pos);
		if (programmer == null)
			return true;

		if(!playerIn.capabilities.isCreativeMode) {
			PlayerData data = PlayerDataHandler.get(playerIn);
			if(data.spellGroupsUnlocked.isEmpty()) {
				if(!worldIn.isRemote)
					playerIn.sendMessage(new TranslationTextComponent("psimisc.cantUseProgrammer").setStyle(new Style().setColor(TextFormatting.RED)));
				return true;
			}
		}
		
		ActionResultType result = setSpell(worldIn, pos, playerIn, heldItem);
		if(result == ActionResultType.SUCCESS)
			return true;

		boolean enabled = programmer.isEnabled();
		if(!enabled || programmer.playerLock.isEmpty())
			programmer.playerLock = playerIn.getName();

		if(playerIn instanceof ServerPlayerEntity)
			VanillaPacketDispatcher.dispatchTEToPlayer(programmer, (ServerPlayerEntity) playerIn);
		playerIn.openGui(Psi.instance, LibGuiIDs.PROGRAMMER, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	public ActionResultType setSpell(World worldIn, BlockPos pos, PlayerEntity playerIn, ItemStack heldItem) {
		TileProgrammer programmer = (TileProgrammer) worldIn.getTileEntity(pos);
		if (programmer == null)
			return ActionResultType.FAIL;

		boolean enabled = programmer.isEnabled();
		
		if(enabled && !heldItem.isEmpty() && ISpellAcceptor.isAcceptor(heldItem) && programmer.spell != null && (playerIn.isSneaking() || !ISpellAcceptor.acceptor(heldItem).requiresSneakForSpellSet())) {
			if(programmer.canCompile()) {
				ISpellAcceptor settable = ISpellAcceptor.acceptor(heldItem);
				if(!worldIn.isRemote)
					worldIn.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, PsiSoundHandler.bulletCreate, SoundCategory.BLOCKS, 0.5F, 1F);

				programmer.spell.uuid = UUID.randomUUID();
				settable.setSpell(playerIn, programmer.spell);
				if(playerIn instanceof ServerPlayerEntity)
					VanillaPacketDispatcher.dispatchTEToPlayer(programmer, (ServerPlayerEntity) playerIn);
				return ActionResultType.SUCCESS;
			} else {
				if(!worldIn.isRemote)
					worldIn.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, PsiSoundHandler.compileError, SoundCategory.BLOCKS, 0.5F, 1F);
				return ActionResultType.FAIL;
			}
		}
		
		return ActionResultType.PASS;
	}
	
	@Override
	public BlockState makeDefaultState() {
		return super.makeDefaultState().withProperty(ENABLED, false);
	}

	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public BlockState getActualState(@Nonnull BlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		return state.withProperty(ENABLED, tile instanceof TileProgrammer && ((TileProgrammer) tile).isEnabled());
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, ENABLED);
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
	public Rarity getBlockRarity(ItemStack stack) {
		return Rarity.UNCOMMON;
	}

	@Override
	public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
		return new TileProgrammer();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileProgrammer) {
			TileProgrammer programmer = (TileProgrammer) tile;

			if (programmer.canCompile())
				return 2;
			else if (programmer.isEnabled())
				return 1;
			else
				return 0;
		}

		return 0;
	}
}
