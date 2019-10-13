/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [21/02/2016, 15:14:36 (GMT)]
 */
package vazkii.psi.common.spell.selector;

import net.minecraft.entity.LivingEntity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorAttacker extends PieceSelector {

	public PieceSelectorAttacker(Spell spell) {
		super(spell);
	}

	@Override
	public Class<?> getEvaluationType() {
		return LivingEntity.class;
	}

	@Override
	public Object execute(SpellContext context) throws SpellRuntimeException {
		if(context.attackingEntity == null)
			throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);

		return context.attackingEntity;
	}

}
