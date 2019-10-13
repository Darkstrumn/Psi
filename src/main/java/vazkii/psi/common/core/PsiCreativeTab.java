/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [08/01/2016, 21:49:46 (GMT)]
 */
package vazkii.psi.common.core;

import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import vazkii.psi.common.block.base.ModBlocks;
import vazkii.psi.common.item.base.ModItems;
import vazkii.psi.common.lib.LibMisc;
import vazkii.psi.common.lib.LibResources;

import javax.annotation.Nonnull;

public class PsiCreativeTab extends ItemGroup {

	public static final PsiCreativeTab INSTANCE = new PsiCreativeTab();
	private NonNullList<ItemStack> list;

	public PsiCreativeTab() {
		super(LibMisc.MOD_ID);
		setNoTitle();
		setBackgroundImageName(LibResources.GUI_CREATIVE);
	}

	@Nonnull
	@Override
	public ItemStack createIcon() {
		return new ItemStack(ModItems.cadAssembly);
	}

	@Override
	public boolean hasSearchBar() {
		return true;
	}

	@Override
	public void displayAllRelevantItems(@Nonnull NonNullList<ItemStack> stacks) {
		list = stacks;

		addBlock(ModBlocks.cadAssembler);
		addBlock(ModBlocks.programmer);

		addItem(ModItems.material);

		addItem(ModItems.cadAssembly);
		addItem(ModItems.cadCore);
		addItem(ModItems.cadSocket);
		addItem(ModItems.cadBattery);
		addItem(ModItems.cadColorizer);

		addItem(ModItems.spellBullet);
		addItem(ModItems.detonator);
		addItem(ModItems.spellDrive);
		addItem(ModItems.exosuitController);
		addItem(ModItems.exosuitSensor);
		addItem(ModItems.vectorRuler);

		addItem(ModItems.cad);

		addItem(ModItems.psimetalShovel);
		addItem(ModItems.psimetalPickaxe);
		addItem(ModItems.psimetalAxe);
		addItem(ModItems.psimetalSword);
		addItem(ModItems.psimetalExosuitHelmet);
		addItem(ModItems.psimetalExosuitChestplate);
		addItem(ModItems.psimetalExosuitLeggings);
		addItem(ModItems.psimetalExosuitBoots);

		addBlock(ModBlocks.psiDecorative);
	}

	private void addItem(Item item) {
		item.getSubItems(this, list);
	}

	private void addBlock(Block block) {
		addItem(Item.getItemFromBlock(block));
	}

}
