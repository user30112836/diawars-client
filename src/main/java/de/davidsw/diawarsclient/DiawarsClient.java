package de.davidsw.diawarsclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class DiawarsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			String json = ClientInfoCollector.collectAsJson();
			sender.sendPacket(new ClientInfoPayload(json));
		});
	}
}