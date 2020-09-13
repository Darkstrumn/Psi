/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.item.component;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICADComponent;
import vazkii.psi.api.internal.TooltipHelper;
import vazkii.psi.common.Psi;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.List;

public abstract class ItemCADComponent extends Item implements ICADComponent {

	private final HashMap<EnumCADStat, Integer> stats = new HashMap<>();

	public ItemCADComponent(Item.Properties properties) {
		super(properties.maxStackSize(1));
		registerStats();
	}

	public void registerStats() {
		// NO-OP
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
		TooltipHelper.tooltipIfShift(tooltip, () -> {
			EnumCADComponent componentType = getComponentType(stack);

			TranslationTextComponent componentName = new TranslationTextComponent(componentType.getName());
			tooltip.add(new TranslationTextComponent("psimisc.component_type", componentName));
			for (EnumCADStat stat : EnumCADStat.class.getEnumConstants()) {
				if (stat.getSourceType() == componentType) {
					int statVal = getCADStatValue(stack, stat);
					String statValStr = statVal == -1 ? "\u221E" : "" + statVal;

					ITextComponent name = new TranslationTextComponent(stat.getName()).formatted(TextFormatting.AQUA);
					tooltip.add(new StringTextComponent(" ").append(name).append(": " + statValStr));
				}
			}
		});
	}

	public void addStat(HashMap<EnumCADStat, Integer> stats) {
		stats.forEach(this::addStat);
	}

	public void addStat(EnumCADStat stat, int value) {
		stats.put(stat, value);
	}

	public static void addStatToStack(ItemStack stack, EnumCADStat stat, int value) {
		if (stack.getItem() instanceof ItemCADComponent) {
			((ItemCADComponent) stack.getItem()).addStat(stat, value);
		} else {
			Psi.logger.error("Tried to add stats to non-component Item: " + stack.getItem().getName());
		}
	}

	public static void addStatToStack(Item item, EnumCADStat stat, int value) {
		if (item instanceof ItemCADComponent) {
			((ItemCADComponent) item).addStat(stat, value);
		} else {
			Psi.logger.error("Tried to add stats to non-component Item: " + item.getName());
		}
	}

	@Override
	public int getCADStatValue(ItemStack stack, EnumCADStat stat) {
		if (stats.containsKey(stat)) {
			return stats.get(stat);
		}

		return 0;
	}

}
