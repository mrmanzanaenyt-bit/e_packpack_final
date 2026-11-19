package com.enhancedbackpack;

import com.enhancedbackpack.inventory.BackpackHelper;
import com.enhancedbackpack.menu.ModMenus;
import com.enhancedbackpack.network.OpenBackpackPayload;
import com.enhancedbackpack.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancedBackpack implements ModInitializer {
	public static final String MOD_ID = "enhancedbackpack";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModComponents.init();
		ModItems.init();
		ModMenus.init();

		// Pestana de creativo: Herramientas y utilidades
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
			output.accept(ModItems.BACKPACK);
			output.accept(ModItems.HORN_PLUGIN);
			output.accept(ModItems.CRAFTING_PLUGIN);
		});

		// Cliente -> servidor: "abrir mochila"
		PayloadTypeRegistry.serverboundPlay().register(OpenBackpackPayload.TYPE, OpenBackpackPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenBackpackPayload.TYPE,
				(payload, context) -> {
					LOGGER.info("[EnhancedBackpack] Paquete 'abrir mochila' recibido de {}", context.player().getName().getString());
					BackpackHelper.openFirst(context.player());
				});

		// El horno del plugin sigue fundiendo aunque la mochila este cerrada
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				BackpackHelper.tickFurnaces(player, server.getTickCount());
			}
		});
	}
}
