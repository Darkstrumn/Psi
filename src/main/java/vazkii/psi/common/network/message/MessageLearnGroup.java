/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [23/01/2016, 19:54:46 (GMT)]
 */
package vazkii.psi.common.network.message;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import vazkii.arl.network.NetworkMessage;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.PieceGroup;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler.PlayerData;

public class MessageLearnGroup extends NetworkMessage<MessageLearnGroup> {

	public String group;

	public MessageLearnGroup() { }

	public MessageLearnGroup(String group) {
		this.group = group;
	}

	@Override
	public IMessage handleMessage(MessageContext context) {
		PlayerEntity player = context.getServerHandler().player;
		PlayerData data = PlayerDataHandler.get(player);
		PieceGroup group = PsiAPI.groupsForName.get(this.group);
		if(data.getLevelPoints() > 0 && group.isAvailable(data))
			data.unlockPieceGroup(group.name);

		return null;
	}

}	
