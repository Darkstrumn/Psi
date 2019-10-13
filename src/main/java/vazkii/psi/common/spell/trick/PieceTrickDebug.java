/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [16/01/2016, 16:13:25 (GMT)]
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.param.ParamAny;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickDebug extends PieceTrick {

	SpellParam target;
	SpellParam number;

	public PieceTrickDebug(Spell spell) {
		super(spell);
	}

	@Override
	public void initParams() {
		addParam(target = new ParamAny(SpellParam.GENERIC_NAME_TARGET, SpellParam.BLUE, false));
		addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.RED, true, false));
	}

	@Override
	public void addToMetadata(SpellMetadata meta) {
		// NO-OP
	}

	@Override
	public Object execute(SpellContext context) {
		Double numberVal = this.<Double>getParamValue(context, number);
		Object targetVal = getParamValue(context, target);

		ITextComponent component = new StringTextComponent(String.valueOf(targetVal));

		if(numberVal != null) {
			String numStr = "" + numberVal;
			if(numberVal - numberVal.intValue() == 0) {
				int numInt = numberVal.intValue();
				numStr = "" + numInt;
			}

			component = new StringTextComponent("[" + numStr + "]")
					.setStyle(new Style().setColor(TextFormatting.AQUA))
					.appendSibling(new StringTextComponent(" ")
							.setStyle(new Style().setColor(TextFormatting.RESET)))
					.appendSibling(component);
		}

		context.caster.sendMessage(component);

		return null;
	}


}
