/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.client.core.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import vazkii.psi.api.ClientPsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.cad.ICADColorizer;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.client.core.handler.ClientTickHandler;
import vazkii.psi.client.core.handler.ColorHandler;
import vazkii.psi.client.core.handler.ContributorSpellCircleHandler;
import vazkii.psi.client.core.handler.KeybindHandler;
import vazkii.psi.client.core.handler.ShaderHandler;
import vazkii.psi.client.fx.SparkleParticleData;
import vazkii.psi.client.fx.WispParticleData;
import vazkii.psi.client.gui.GuiProgrammer;
import vazkii.psi.client.model.ModelCAD;
import vazkii.psi.client.render.entity.RenderSpellCircle;
import vazkii.psi.client.render.entity.RenderSpellProjectile;
import vazkii.psi.client.render.tile.RenderTileProgrammer;
import vazkii.psi.common.block.base.ModBlocks;
import vazkii.psi.common.block.tile.TileProgrammer;
import vazkii.psi.common.core.proxy.IProxy;
import vazkii.psi.common.entity.EntitySpellCharge;
import vazkii.psi.common.entity.EntitySpellCircle;
import vazkii.psi.common.entity.EntitySpellGrenade;
import vazkii.psi.common.entity.EntitySpellMine;
import vazkii.psi.common.entity.EntitySpellProjectile;
import vazkii.psi.common.item.base.ModItems;
import vazkii.psi.common.lib.LibItemNames;
import vazkii.psi.common.lib.LibMisc;
import vazkii.psi.common.spell.other.PieceConnector;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientProxy implements IProxy {

	@Override
	public void registerHandlers() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::modelBake);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addCADModels);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
	}

	private void clientSetup(FMLClientSetupEvent event) {

		KeybindHandler.init();

		ClientRegistry.bindTileEntityRenderer(TileProgrammer.TYPE, RenderTileProgrammer::new);

		RenderingRegistry.registerEntityRenderingHandler(EntitySpellCircle.TYPE, RenderSpellCircle::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellCharge.TYPE, RenderSpellProjectile::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellGrenade.TYPE, RenderSpellProjectile::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellProjectile.TYPE, RenderSpellProjectile::new);
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellMine.TYPE, RenderSpellProjectile::new);
		RenderTypeLookup.setRenderLayer(ModBlocks.conjured, RenderType.getTranslucent());
		ContributorSpellCircleHandler.firstStart();
		ModelBakery.LOCATIONS_BUILTIN_TEXTURES.addAll(ClientPsiAPI.getAllSpellPieceMaterial());
		ModelBakery.LOCATIONS_BUILTIN_TEXTURES.add(new RenderMaterial(ClientPsiAPI.PSI_PIECE_TEXTURE_ATLAS, PieceConnector.LINES_TEXTURE));
	}

	private void loadComplete(FMLLoadCompleteEvent event) {
		DeferredWorkQueue.runLater(() -> {
			Map<RenderType, BufferBuilder> map = ObfuscationReflectionHelper.getPrivateValue(IRenderTypeBuffer.Impl.class, Minecraft.getInstance().getBufferBuilders().getEntityVertexConsumers(), "field_228458_b_");
			RenderType layer = SpellPiece.getLayer();
			map.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
			map.put(GuiProgrammer.LAYER, new BufferBuilder(GuiProgrammer.LAYER.getExpectedBufferSize()));
			ColorHandler.init();
			ShaderHandler.init();
		});
	}

	private void modelBake(ModelBakeEvent event) {
		ModelResourceLocation key = new ModelResourceLocation(ModItems.cad.getRegistryName(), "inventory");
		event.getModelRegistry().put(key, new ModelCAD());

	}

	private void addCADModels(ModelRegistryEvent event) {
		ModelLoader.addSpecialModel(new ResourceLocation(LibMisc.MOD_ID, "item/" + LibItemNames.CAD_IRON));
		ModelLoader.addSpecialModel(new ResourceLocation(LibMisc.MOD_ID, "item/" + LibItemNames.CAD_GOLD));
		ModelLoader.addSpecialModel(new ResourceLocation(LibMisc.MOD_ID, "item/" + LibItemNames.CAD_PSIMETAL));
		ModelLoader.addSpecialModel(new ResourceLocation(LibMisc.MOD_ID, "item/" + LibItemNames.CAD_EBONY_PSIMETAL));
		ModelLoader.addSpecialModel(new ResourceLocation(LibMisc.MOD_ID, "item/" + LibItemNames.CAD_IVORY_PSIMETAL));
		ModelLoader.addSpecialModel(new ResourceLocation(LibMisc.MOD_ID, "item/" + LibItemNames.CAD_CREATIVE));

	}

	@Override
	public boolean hasAdvancement(ResourceLocation advancement, PlayerEntity playerEntity) {
		if (playerEntity instanceof ClientPlayerEntity) {
			ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity) playerEntity;
			return clientPlayerEntity.connection.getAdvancementManager().getAdvancementList().getAdvancement(advancement) != null;
		} else if (playerEntity instanceof ServerPlayerEntity) {
			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerEntity;
			return serverPlayerEntity.getServer().getAdvancementManager().getAdvancement(advancement) != null && serverPlayerEntity.getAdvancements().getProgress(serverPlayerEntity.getServer().getAdvancementManager().getAdvancement(advancement)).isDone();
		}
		return false;
	}

	@Override
	public void addParticleForce(World world, IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		world.addParticle(particleData, true, x, y, z, xSpeed, ySpeed, zSpeed);
	}

	@Override
	public PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getInstance().world;
	}

	@Override
	public long getWorldElapsedTicks() {
		return ClientTickHandler.ticksInGame;
	}

	@Override
	public int getClientRenderDistance() {
		return Minecraft.getInstance().gameSettings.renderDistanceChunks;
	}

	@Override
	public int getColorForCAD(ItemStack cadStack) {
		ICAD icad = (ICAD) cadStack.getItem();
		return icad.getSpellColor(cadStack);
	}

	@Override
	public int getColorForColorizer(ItemStack colorizer) {
		if (colorizer.isEmpty() || !(colorizer.getItem() instanceof ICADColorizer)) {
			return ICADColorizer.DEFAULT_SPELL_COLOR;
		}
		ICADColorizer icc = (ICADColorizer) colorizer.getItem();
		return icc.getColor(colorizer);
	}

	@Override
	public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float motionx, float motiony, float motionz, float size, int m) {
		if (m == 0) {
			return;
		}
		SparkleParticleData data = new SparkleParticleData(size, r, g, b, m, motionx, motiony, motionz);
		addParticleForce(world, data, x, y, z, motionx, motiony, motionz);

	}

	@Override
	public void sparkleFX(double x, double y, double z, float r, float g, float b, float motionx, float motiony, float motionz, float size, int m) {
		sparkleFX(Minecraft.getInstance().world, x, y, z, r, g, b, motionx, motiony, motionz, size, m);
	}

	@Override
	public void wispFX(World world, double x, double y, double z, float r, float g, float b, float size, float motionx, float motiony, float motionz, float maxAgeMul) {
		if (maxAgeMul == 0) {
			return;
		}
		WispParticleData data = new WispParticleData(size, r, g, b, maxAgeMul);
		addParticleForce(world, data, x, y, z, motionx, motiony, motionz);
	}

	@Override
	public void wispFX(double x, double y, double z, float r, float g, float b, float size, float motionx, float motiony, float motionz, float maxAgeMul) {
		wispFX(Minecraft.getInstance().world, x, y, z, r, g, b, size, motionx, motiony, motionz, maxAgeMul);
	}

	@Override
	public void openProgrammerGUI(TileProgrammer programmer) {
		Minecraft.getInstance().displayGuiScreen(new GuiProgrammer(programmer));
	}

}
