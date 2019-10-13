/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [17/01/2016, 00:45:57 (GMT)]
 */
package vazkii.psi.client.render.tile;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;
import org.lwjgl.opengl.GL11;
import vazkii.arl.block.BlockFacing;
import vazkii.arl.util.ClientTicker;
import vazkii.arl.util.RenderHelper;
import vazkii.psi.api.internal.TooltipHelper;
import vazkii.psi.client.gui.GuiProgrammer;
import vazkii.psi.common.Psi;
import vazkii.psi.common.block.base.ModBlocks;
import vazkii.psi.common.block.tile.TileProgrammer;

public class RenderTileProgrammer extends TileEntityRenderer<TileProgrammer> {

	@Override
	public void render(TileProgrammer te, double x, double y, double z, float partialTicks, int destroyStage, float something) {
		if (te.isEnabled()) {
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			GlStateManager.disableCull();

			float brightnessX = OpenGlHelper.lastBrightnessX;
			float brightnessY = OpenGlHelper.lastBrightnessY;
			if (!Psi.magical)
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0xf0, 0xf0);

			GlStateManager.translate(x, y + 1.62F, z);
			GlStateManager.rotate(180F, 0F, 0F, 1F);
			GlStateManager.rotate(-90F, 0F, 1F, 0F);

			float rot = 90F;
			BlockState state = te.getWorld().getBlockState(te.getPos());
			if (state.getBlock() != ModBlocks.programmer)
				return;

			BlockState actualState = state.getActualState(te.getWorld(), te.getPos());
			Direction facing = actualState.getValue(BlockFacing.FACING);
			switch (facing) {
				case SOUTH:
					rot = -90F;
					break;
				case EAST:
					rot = 180F;
					break;
				case WEST:
					rot = 0F;
					break;
				default:
					break;
			}

			GlStateManager.translate(0.5F, 0F, 0.5F);
			GlStateManager.rotate(rot, 0F, 1F, 0F);
			GlStateManager.translate(-0.5F, 0F, -0.5F);

			float f = 1F / 300F;
			GlStateManager.scale(f, f, -f);

			if (Psi.magical) {
				GlStateManager.rotate(90F, 1F, 0F, 0F);
				GlStateManager.translate(70F, -220F, -100F + Math.sin(ClientTicker.total / 50) * 10);
				GlStateManager.rotate(-16F + (float) Math.cos(ClientTicker.total / 100) * 10F, 1F, 0F, 0F);
			} else GlStateManager.translate(70F, 0F, -200F);

			te.spell.draw();

			Minecraft mc = Minecraft.getMinecraft();
			mc.renderEngine.bindTexture(GuiProgrammer.texture);

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1F, 1F, 1F, (Psi.magical ? 1F : 0.5F));
			GlStateManager.translate(0F, 0F, -0.01F);

			RenderHelper.drawTexturedModalRect(-7, -7, 0, 0, 0, 174, 184, 1F / 256F, 1F / 256F);

			GlStateManager.translate(0F, 0F, 0.01F);

			int color = Psi.magical ? 0 : 0xFFFFFF;
			mc.fontRenderer.drawString(TooltipHelper.local("psimisc.name"), 0, 164, color);
			mc.fontRenderer.drawString(te.spell.name, 38, 164, color);

			if (!Psi.magical)
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightnessX, brightnessY);
			GlStateManager.enableLighting();
			GlStateManager.enableCull();
			GlStateManager.popMatrix();
		}
	}

}
