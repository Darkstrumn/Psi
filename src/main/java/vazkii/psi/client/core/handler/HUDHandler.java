/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [11/01/2016, 00:30:54 (GMT)]
 */
package vazkii.psi.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.ScaledResolution;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.*;
import vazkii.psi.api.internal.TooltipHelper;
import vazkii.psi.api.internal.PsiRenderHelper;
import vazkii.psi.client.gui.GuiLeveling;
import vazkii.psi.common.core.handler.ConfigHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PlayerDataHandler.PlayerData;
import vazkii.psi.common.core.handler.PlayerDataHandler.PlayerData.Deduction;
import vazkii.psi.common.core.handler.PsiSoundHandler;
import vazkii.psi.common.item.base.IHUDItem;
import vazkii.psi.common.lib.LibMisc;
import vazkii.psi.common.lib.LibObfuscation;
import vazkii.psi.common.lib.LibResources;

import java.util.function.Consumer;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = LibMisc.MOD_ID)
public final class HUDHandler {

	private static final ResourceLocation psiBar = new ResourceLocation(LibResources.GUI_PSI_BAR);
	private static final ResourceLocation psiBarMask = new ResourceLocation(LibResources.GUI_PSI_BAR_MASK);
	private static final ResourceLocation psiBarShatter = new ResourceLocation(LibResources.GUI_PSI_BAR_SHATTER);

	private static final int secondaryTextureUnit = 7;

	private static boolean registeredMask = false;
	private static final int maxRemainingTicks = 30;

	public static boolean showLevelUp = false;
	public static int levelDisplayTime = 0;
	public static int levelValue = 0;

	private static ItemStack remainingDisplayStack;
	private static int remainingTime;
	private static int remainingCount;

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onDraw(RenderGameOverlayEvent.Post event) {
		if (event.getType() == ElementType.ALL) {
			ScaledResolution resolution = event.getResolution();
			float partialTicks = event.getPartialTicks();

			drawPsiBar(resolution, partialTicks);
			renderSocketableEquippedName(resolution, partialTicks);
			renderLevelUpIndicator(resolution);
			renderRemainingItems(resolution, partialTicks);
			renderHUDItem(resolution, partialTicks);
		}
	}

	public static void tick() {
		if (showLevelUp)
			levelDisplayTime++;

		if (remainingTime > 0)
			--remainingTime;
	}

	private static boolean showsBar(PlayerData data, ItemStack stack) {
		if (stack.isEmpty() || !IPsiBarDisplay.isDisplay(stack))
			return false;
		else
			return IPsiBarDisplay.display(stack).shouldShow(data);
	}

	@OnlyIn(Dist.CLIENT)
	public static void drawPsiBar(ScaledResolution res, float pticks) {
		Minecraft mc = Minecraft.getMinecraft();
		ItemStack cadStack = PsiAPI.getPlayerCAD(mc.player);

		if (cadStack.isEmpty())
			return;

		ICAD cad = (ICAD) cadStack.getItem();
		PlayerData data = PlayerDataHandler.get(mc.player);
		if (data.level == 0 && !mc.player.capabilities.isCreativeMode)
			return;

		int totalPsi = data.getTotalPsi();
		int currPsi = data.getAvailablePsi();

		if (ConfigHandler.contextSensitiveBar && currPsi == totalPsi &&
				!showsBar(data, mc.player.getHeldItemMainhand()) &&
						!showsBar(data, mc.player.getHeldItemOffhand()))
			return;

		GlStateManager.pushMatrix();
		int scaleFactor = res.getScaleFactor();

		if (scaleFactor > ConfigHandler.maxPsiBarScale) {
			int guiScale = mc.gameSettings.guiScale;

			mc.gameSettings.guiScale = ConfigHandler.maxPsiBarScale;
			res = new ScaledResolution(mc);
			mc.gameSettings.guiScale = guiScale;

			float s = (float) ConfigHandler.maxPsiBarScale / (float) scaleFactor;
			GlStateManager.scale(s, s, s);
		}

		boolean right = ConfigHandler.psiBarOnRight;

		int pad = 3;
		int width = 32;
		int height = 140;

		int x = -pad;
		if (right)
			x = res.getScaledWidth() + pad - width;
		int y = res.getScaledHeight() / 2 - height / 2;

		if (!registeredMask) {
			mc.renderEngine.bindTexture(psiBarMask);
			mc.renderEngine.bindTexture(psiBarShatter);
			registeredMask = true;
		}

		GlStateManager.enableBlend();
		mc.renderEngine.bindTexture(psiBar);
		AbstractGui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, 64, 256);

		x += 8;
		y += 26;

		width = 16;
		height = 106;

		float r = 0.6F;
		float g = 0.65F;
		float b = 1F;

		int origHeight = height;
		int origY = y;
		int v = 0;

		int texture = 0;
		boolean shaders = ShaderHandler.useShaders();

		if (shaders) {
			OpenGlHelper.setActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB + secondaryTextureUnit);
			texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		}

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		for (Deduction d : data.deductions) {
			float a = d.getPercentile(pticks);
			GlStateManager.color(r, g, b, a);
			height = (int) Math.ceil(origHeight * (double) d.deduct / totalPsi);
			int effHeight = (int) (origHeight * (double) d.current / totalPsi);
			v = origHeight - effHeight;
			y = origY + v;

			ShaderHandler.useShader(ShaderHandler.psiBar, generateCallback(a, d.shatter, data.overflowed));
			AbstractGui.drawModalRectWithCustomSizedTexture(x, y, 32, v, width, height, 64, 256);
		}

		float textY = origY;
		if (totalPsi > 0) {
			height = (int) ((double) origHeight * (double) data.availablePsi / totalPsi);
			v = origHeight - height;
			y = origY + v;

			if (data.availablePsi != data.lastAvailablePsi) {
				float textHeight = (float) (origHeight
						* (data.availablePsi * pticks + data.lastAvailablePsi * (1.0 - pticks)) / totalPsi);
				textY = origY + (origHeight - textHeight);
			} else
				textY = y;
		} else
			height = 0;

		GlStateManager.color(r, g, b);
		ShaderHandler.useShader(ShaderHandler.psiBar, generateCallback(1F, false, data.overflowed));
		AbstractGui.drawModalRectWithCustomSizedTexture(x, y, 32, v, width, height, 64, 256);
		ShaderHandler.releaseShader();

		if (shaders) {
			OpenGlHelper.setActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB + secondaryTextureUnit);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			OpenGlHelper.setActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB);
		}

		GlStateManager.color(1F, 1F, 1F);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0F, textY, 0F);
		width = 44;
		height = 3;

		int storedPsi = cad.getStoredPsi(cadStack);

		String s1 = storedPsi == -1 ? "\u221E" : "" + data.availablePsi;
		String s2 = "" + storedPsi;

		int offBar = 22;
		int offStr1 = 7 + mc.fontRenderer.getStringWidth(s1);
		int offStr2 = 7 + mc.fontRenderer.getStringWidth(s2);

		if (!right) {
			offBar = 6;
			offStr1 = -23;
			offStr2 = -23;
		}

		int color = cad.getSpellColor(cadStack);
		GlStateManager.color(PsiRenderHelper.r(color) / 255F,
				PsiRenderHelper.g(color) / 255F,
				PsiRenderHelper.b(color) / 255F, 1F);

		AbstractGui.drawModalRectWithCustomSizedTexture(x - offBar, -2, 0, 140, width, height, 64, 256);
		mc.fontRenderer.drawStringWithShadow(s1, x - offStr1, -11, 0xFFFFFF);
		GlStateManager.popMatrix();

		if (storedPsi != -1) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, Math.max(textY + 3, origY + 100), 0F);
			mc.fontRenderer.drawStringWithShadow(s2, x - offStr2, 0, 0xFFFFFF);
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
	}

	@OnlyIn(Dist.CLIENT)
	private static void renderSocketableEquippedName(ScaledResolution res, float pticks) {
		Minecraft mc = Minecraft.getMinecraft();
		ItemStack stack = mc.player.getHeldItem(Hand.MAIN_HAND);
		String name = ISocketable.getSocketedItemName(stack, "");
		if (stack.isEmpty() || name == null || name.trim().isEmpty())
			return;

		int ticks = ObfuscationReflectionHelper.getPrivateValue(IngameGui.class, mc.ingameGUI,
				LibObfuscation.REMAINING_HIGHLIGHT_TICKS);
		ticks -= 10;

		if (ticks > 0) {
			ISocketableCapability socketable = ISocketableCapability.socketable(stack);
			ItemStack bullet = socketable.getBulletInSocket(socketable.getSelectedSlot());

			int alpha = Math.min(255, (int) ((ticks - pticks) * 256.0F / 10.0F));
			int color = ICADColorizer.DEFAULT_SPELL_COLOR + (alpha << 24);

			int x = res.getScaledWidth() / 2 - mc.fontRenderer.getStringWidth(name) / 2;
			int y = res.getScaledHeight() - 71;
			if (mc.player.capabilities.isCreativeMode)
				y += 14;

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			mc.fontRenderer.drawStringWithShadow(name, x, y, color);

			int w = mc.fontRenderer.getStringWidth(name);
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + w, y - 6, 0);
			GlStateManager.scale(alpha / 255F, 1F, 1);
			GlStateManager.color(1F, 1F, 1F);
			mc.getRenderItem().renderItemIntoGUI(bullet, 0, 0);
			GlStateManager.popMatrix();
			GlStateManager.disableBlend();
		}
	}

	public static void levelUp(int level) {
		levelValue = level;
		levelDisplayTime = 0;
		showLevelUp = true;
	}

	@OnlyIn(Dist.CLIENT)
	private static void renderLevelUpIndicator(ScaledResolution res) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentScreen instanceof GuiLeveling)
			showLevelUp = false;

		if (!showLevelUp)
			return;

		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		int time = 100;
		int fadeTime = time / 10;
		int fadeoutTime = fadeTime * 2;

		String levelUp = TooltipHelper.local("psimisc.levelup");
		int len = levelUp.length();
		int effLen = Math.min(len, len * levelDisplayTime / fadeTime);
		levelUp = levelUp.substring(0, effLen);

		int swidth = mc.fontRenderer.getStringWidth(levelUp);
		int x = res.getScaledWidth() / 4 - swidth / 2;
		int y = 25;
		float a = 1F - Math.max(0F, Math.min(1F, (float) (levelDisplayTime - time) / fadeoutTime));
		int alphaOverlay = (int) (a * 0xFF) << 24;

		GlStateManager.pushMatrix();
		GlStateManager.scale(2F, 2F, 2F);
		mc.fontRenderer.drawStringWithShadow(levelUp, x, y, 0x0013C5FF + alphaOverlay);

		String currLevel = "" + levelValue;
		x = res.getScaledWidth() / 4;
		y += 10;

		if (levelDisplayTime > fadeTime) {
			if (levelDisplayTime - fadeTime == 1)
				mc.getSoundHandler().playSound(SimpleSound.getMasterRecord(PsiSoundHandler.levelUp, 0.5F));

			float a1 = Math.min(1F, (float) (levelDisplayTime - fadeTime) / fadeTime) * a;
			int color1 = 0x00FFFFFF + ((int) (a1 * 0xFF) << 24);
			mc.fontRenderer.drawStringWithShadow(TextFormatting.GOLD + currLevel, x, y, color1);
		}
		GlStateManager.popMatrix();

		if (levelDisplayTime > fadeTime * 2) {
			String s = TooltipHelper.local("psimisc.levelUpInfo1");
			swidth = mc.fontRenderer.getStringWidth(s);
			len = s.length();
			effLen = Math.min(len, len * (levelDisplayTime - fadeTime * 2) / fadeTime);
			s = s.substring(0, effLen);
			x = res.getScaledWidth() / 2 - swidth / 2;
			y += 65;

			mc.fontRenderer.drawStringWithShadow(s, x, y, 0x00FFFFFF + alphaOverlay);
		}

		if (levelDisplayTime > fadeTime * 3) {
			String s = TooltipHelper.local("psimisc.levelUpInfo2", TextFormatting.GREEN + TooltipHelper.local(KeybindHandler.keybind.getDisplayName())
					+ TextFormatting.RESET);
			swidth = mc.fontRenderer.getStringWidth(s);
			len = s.length();
			effLen = Math.min(len, len * (levelDisplayTime - fadeTime * 3) / fadeTime);
			s = s.substring(0, effLen);
			x = res.getScaledWidth() / 2 - swidth / 2;
			y += 10;

			mc.fontRenderer.drawStringWithShadow(s, x, y, 0x00FFFFFF + alphaOverlay);
		}

		GlStateManager.enableAlpha();
		if (levelValue > 1 && levelDisplayTime >= time + fadeoutTime)
			showLevelUp = false;
	}

	@OnlyIn(Dist.CLIENT)
	private static void renderRemainingItems(ScaledResolution resolution, float partTicks) {
		if (remainingTime > 0 && !remainingDisplayStack.isEmpty()) {
			int pos = maxRemainingTicks - remainingTime;
			Minecraft mc = Minecraft.getMinecraft();
			int remainingLeaveTicks = 20;
			int x = resolution.getScaledWidth() / 2 + 10 + Math.max(0, pos - remainingLeaveTicks);
			int y = resolution.getScaledHeight() / 2;

			int start = maxRemainingTicks - remainingLeaveTicks;
			float alpha = remainingTime + partTicks > start ? 1F : (remainingTime + partTicks) / start;

			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
			GlStateManager.disableRescaleNormal();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			GlStateManager.color(1F, 1F, 1F, alpha);
			RenderHelper.enableGUIStandardItemLighting();
			int xp = x + (int) (16F * (1F - alpha));
			GlStateManager.translate(xp, y, 0F);
			GlStateManager.scale(alpha, 1F, 1F);
			mc.getRenderItem().renderItemAndEffectIntoGUI(remainingDisplayStack, 0, 0);
			GlStateManager.scale(1F / alpha, 1F, 1F);
			GlStateManager.translate(-xp, -y, 0F);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.color(1F, 1F, 1F, 1F);
			GlStateManager.enableBlend();

			String text = TextFormatting.GREEN + remainingDisplayStack.getDisplayName();
			if (remainingCount >= 0) {
				int max = remainingDisplayStack.getMaxStackSize();
				int stacks = remainingCount / max;
				int rem = remainingCount % max;

				if (stacks == 0)
					text = "" + remainingCount;
				else
					text = remainingCount + " (" + TextFormatting.AQUA + stacks + TextFormatting.RESET + "*"
							+ TextFormatting.GRAY + max + TextFormatting.RESET + "+" + TextFormatting.YELLOW + rem
							+ TextFormatting.RESET + ")";
			} else if (remainingCount == -1)
				text = "\u221E";

			int color = 0x00FFFFFF | (int) (alpha * 0xFF) << 24;
			mc.fontRenderer.drawStringWithShadow(text, x + 20, y + 6, color);

			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void renderHUDItem(ScaledResolution resolution, float partTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		ItemStack stack = mc.player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() instanceof IHUDItem)
			((IHUDItem) stack.getItem()).drawHUD(resolution, partTicks, stack);

		stack = mc.player.getHeldItemOffhand();
		if (!stack.isEmpty() && stack.getItem() instanceof IHUDItem)
			((IHUDItem) stack.getItem()).drawHUD(resolution, partTicks, stack);
	}

	public static void setRemaining(ItemStack stack, int count) {
		HUDHandler.remainingDisplayStack = stack;
		HUDHandler.remainingCount = count;
		remainingTime = stack.isEmpty() ? 0 : maxRemainingTicks;
	}

	public static void setRemaining(PlayerEntity player, ItemStack displayStack, Pattern pattern) {
		int count = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty() && (pattern == null ? ItemStack.areItemsEqual(displayStack, stack) : pattern.matcher(stack.getTranslationKey()).find()))
				count += stack.getCount();
		}

		setRemaining(displayStack, count);
	}

	@OnlyIn(Dist.CLIENT)
	private static Consumer<Integer> generateCallback(final float percentile, final boolean shatter, final boolean overflowed) {
		Minecraft mc = Minecraft.getMinecraft();
		return (Integer shader) -> {
			int percentileUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "percentile");
			int overflowedUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "overflowed");
			int imageUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "image");
			int maskUniform = ARBShaderObjects.glGetUniformLocationARB(shader, "mask");

			OpenGlHelper.setActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB);
			mc.renderEngine.bindTexture(psiBar);
			ARBShaderObjects.glUniform1iARB(imageUniform, 0);

			OpenGlHelper.setActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB + secondaryTextureUnit);

			GlStateManager.enableTexture2D();

			mc.renderEngine.bindTexture(shatter ? psiBarShatter : psiBarMask);
			ARBShaderObjects.glUniform1iARB(maskUniform, secondaryTextureUnit);

			ARBShaderObjects.glUniform1fARB(percentileUniform, percentile);
			ARBShaderObjects.glUniform1iARB(overflowedUniform, overflowed ? 1 : 0);
		};
	}

}
