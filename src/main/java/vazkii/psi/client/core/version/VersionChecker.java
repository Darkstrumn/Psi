/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [May 31, 2014, 9:58:26 PM (GMT)]
 */
package vazkii.psi.client.core.version;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import vazkii.psi.api.internal.TooltipHelper;
import vazkii.psi.common.lib.LibMisc;

public final class VersionChecker {

	private static final int FLAVOUR_MESSAGES = 10;

	public static boolean doneChecking = false;
	public static String onlineVersion = "";
	public static boolean triedToWarnPlayer = false;

	public static boolean startedDownload = false;
	public static boolean downloadedFile = false;

	public void init() {
		new ThreadVersionChecker();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	@SuppressWarnings("ConstantConditions")
	public void onTick(ClientTickEvent event) {
		if(doneChecking && event.phase == Phase.END && Minecraft.getMinecraft().player != null && !triedToWarnPlayer) {
			if(!onlineVersion.isEmpty()) {
				PlayerEntity player = Minecraft.getMinecraft().player;
				int onlineBuild = Integer.parseInt(onlineVersion.split("-")[1]);
				int clientBuild = LibMisc.BUILD.contains("GRADLE") ? Integer.MAX_VALUE : Integer.parseInt(LibMisc.BUILD);
				if(onlineBuild > clientBuild) {
					player.sendMessage(new TranslationTextComponent("psi.versioning.flavour" + player.getEntityWorld().rand.nextInt(FLAVOUR_MESSAGES)).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)));
					player.sendMessage(new TranslationTextComponent("psi.versioning.outdated", clientBuild, onlineBuild));

					ITextComponent component = ITextComponent.Serializer.jsonToComponent(TooltipHelper.local("psi.versioning.updateMessage").replaceAll("%version%", onlineVersion));
					player.sendMessage(component);
				}
			}

			triedToWarnPlayer = true;
		}
	}

}
