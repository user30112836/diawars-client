package de.davidsw.diawarsclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class DiawarsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PayloadTypeRegistry.serverboundPlay().register(ClientInfoPayload.TYPE, ClientInfoPayload.CODEC);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			String json = ClientInfoCollector.collectAsJson();
			sender.sendPacket(new ClientInfoPayload(json));
		});
	}
}