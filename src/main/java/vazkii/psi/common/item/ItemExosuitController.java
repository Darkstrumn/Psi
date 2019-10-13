/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [20/02/2016, 23:21:58 (GMT)]
 */
package vazkii.psi.common.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import vazkii.arl.item.ItemMod;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.psi.api.cad.ISocketableCapability;
import vazkii.psi.api.cad.ISocketableController;
import vazkii.psi.common.core.PsiCreativeTab;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.item.base.IPsiItem;
import vazkii.psi.common.lib.LibItemNames;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ItemExosuitController extends ItemMod implements ISocketableController, IPsiItem {

	private static final String TAG_SELECTED_CONTROL_SLOT = "selectedControlSlot";

	public ItemExosuitController() {
		super(LibItemNames.EXOSUIT_CONTROLLER);
		setMaxStackSize(1);
		setCreativeTab(PsiCreativeTab.INSTANCE);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, @Nonnull Hand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if(playerIn.isSneaking()) {
			if(!worldIn.isRemote)
				worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, PsiSoundHandler.compileError, SoundCategory.PLAYERS, 0.25F, 1F);
			else playerIn.swingArm(hand);

			ItemStack[] stacks = getControlledStacks(playerIn, itemStackIn);

			for(ItemStack stack : stacks) {
				ISocketableCapability socketable = ISocketableCapability.socketable(stack);
				socketable.setSelectedSlot(3);
			}
			
			return new ActionResult<>(ActionResultType.SUCCESS, itemStackIn);
		}

		return new ActionResult<>(ActionResultType.PASS, itemStackIn);
	}

	@Override
	public ItemStack[] getControlledStacks(PlayerEntity player, ItemStack stack) {
		List<ItemStack> stacks = new ArrayList<>();
		for(int i = 0; i < 4; i++) {
			ItemStack armor = player.inventory.armorInventory.get(3 - i);
			if(!armor.isEmpty() && ISocketableCapability.isSocketable(armor))
				stacks.add(armor);
		}

		return stacks.toArray(new ItemStack[0]);
	}

	@Override
	public int getDefaultControlSlot(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_SELECTED_CONTROL_SLOT, 0);
	}

	@Override
	public void setSelectedSlot(PlayerEntity player, ItemStack stack, int controlSlot, int slot) {
		ItemNBTHelper.setInt(stack, TAG_SELECTED_CONTROL_SLOT, controlSlot);

		ItemStack[] stacks = getControlledStacks(player, stack);
		if(controlSlot < stacks.length && !stacks[controlSlot].isEmpty()) {
			ISocketableCapability socketable = ISocketableCapability.socketable(stacks[controlSlot]);
			socketable.setSelectedSlot(slot);
		}
	}

}
