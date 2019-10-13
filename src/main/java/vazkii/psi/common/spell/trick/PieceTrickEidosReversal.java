/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [10/02/2016, 00:33:09 (GMT)]
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.entity.player.ServerPlayerEntity;
import vazkii.arl.network.NetworkHandler;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler.PlayerData;
import vazkii.psi.common.network.message.MessageEidosSync;

public class PieceTrickEidosReversal extends PieceTrick {

	SpellParam time;

	public PieceTrickEidosReversal(Spell spell) {
		super(spell);
	}

	@Override
	public void initParams() {
		addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.RED, false, true));
	}

	@Override
	public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
		super.addToMetadata(meta);
		Double timeVal = this.<Double>getParamEvaluation(time);

		if(timeVal == null ||  timeVal <= 0 || timeVal != timeVal.intValue())
			throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, x, y);

		meta.addStat(EnumSpellStat.POTENCY, (int) (timeVal * 11 + 20));
		meta.addStat(EnumSpellStat.COST, timeVal.intValue() * 40);
	}

	@Override
	public Object execute(SpellContext context) {
		Double timeVal = this.<Double>getParamValue(context, time);
		PlayerData data = PlayerDataHandler.get(context.caster);
		if(!data.isReverting) {
			data.eidosReversionTime = timeVal.intValue() * 10;
			data.isReverting = true;
			if (context.caster instanceof ServerPlayerEntity)
				NetworkHandler.INSTANCE.sendTo(new MessageEidosSync(data.eidosReversionTime), (ServerPlayerEntity) context.caster);
		}

		return null;
	}

}
