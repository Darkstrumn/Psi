/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 * 
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 * 
 * File Created @ [22/02/2016, 15:30:13 (GMT)]
 */
package vazkii.psi.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.item.ItemMod;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.common.core.PsiCreativeTab;
import vazkii.psi.common.item.base.IHUDItem;
import vazkii.psi.common.item.base.IPsiItem;
import vazkii.psi.common.lib.LibItemNames;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemVectorRuler extends ItemMod implements IHUDItem, IPsiItem {

	private static final String TAG_SRC_X = "srcX";
	private static final String TAG_SRC_Y = "srcY";
	private static final String TAG_SRC_Z = "srcZ";
	
	private static final String TAG_DST_X = "dstX";
	private static final String TAG_DST_Y = "dstY";
	private static final String TAG_DST_Z = "dstZ";
	
	public ItemVectorRuler() {
		super(LibItemNames.VECTOR_RULER);
		setMaxStackSize(1);
		setCreativeTab(PsiCreativeTab.INSTANCE);
	}
	
	@Nonnull
	@Override
	public ActionResultType onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		int srcY = ItemNBTHelper.getInt(stack, TAG_SRC_Y, -1);
		
		if(srcY == -1 || playerIn.isSneaking()) {
			ItemNBTHelper.setInt(stack, TAG_SRC_X, pos.getX());
			ItemNBTHelper.setInt(stack, TAG_SRC_Y, pos.getY());
			ItemNBTHelper.setInt(stack, TAG_SRC_Z, pos.getZ());
			ItemNBTHelper.setInt(stack, TAG_DST_Y, -1);
		} else {
			ItemNBTHelper.setInt(stack, TAG_DST_X, pos.getX());
			ItemNBTHelper.setInt(stack, TAG_DST_Y, pos.getY());
			ItemNBTHelper.setInt(stack, TAG_DST_Z, pos.getZ());
		}
		
		return ActionResultType.SUCCESS;
	}

    @OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World playerIn, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add(getVector(stack).toString());
	}
	
	public Vector3 getVector(ItemStack stack) {
		int srcX = ItemNBTHelper.getInt(stack, TAG_SRC_X, 0);
		int srcY = ItemNBTHelper.getInt(stack, TAG_SRC_Y, 0);
		int srcZ = ItemNBTHelper.getInt(stack, TAG_SRC_Z, 0);
		
		int dstY = ItemNBTHelper.getInt(stack, TAG_DST_Y, -1);
		if(dstY == -1)
			return new Vector3(srcX, srcY, srcZ);

		int dstX = ItemNBTHelper.getInt(stack, TAG_DST_X, 0);
		int dstZ = ItemNBTHelper.getInt(stack, TAG_DST_Z, 0);
		
		return new Vector3(dstX - srcX, dstY - srcY, dstZ - srcZ);
	}
	
	public static Vector3 getRulerVector(PlayerEntity player) {
		for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if(!stack.isEmpty() && stack.getItem() instanceof ItemVectorRuler)
				return ((ItemVectorRuler) stack.getItem()).getVector(stack);
		}
		
		return Vector3.zero;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void drawHUD(ScaledResolution res, float partTicks, ItemStack stack) {
		String s = getVector(stack).toString();
		
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		int w = font.getStringWidth(s);
		font.drawStringWithShadow(s, res.getScaledWidth() / 2f - w / 2f, res.getScaledHeight() / 2f + 10, 0xFFFFFFFF);
	}
}
