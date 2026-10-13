package de.davidsw.diawarsclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;

public class DiawarsClient implements ClientModInitializer {
	public static final String MOD_ID = "diawars-client";

	/**
	 * Set when the server asks us to open the upload UI (via the
	 * {@code diawars:image_open} signal, e.g. from the images menu or
	 * {@code /image upload}). The screen opens on the next client tick
	 * because screen changes must not happen inside network handling.
	 */
	private static volatile boolean pendingOpenUploadScreen;

	@Override
	public void onInitializeClient() {
		PayloadTypeRegistry.serverboundPlay().register(ClientInfoPayload.TYPE, ClientInfoPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ImagePayload.TYPE, ImagePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ImageOpenPayload.TYPE, ImageOpenPayload.CODEC);
		ClientPlayNetworking.registerGlobalReceiver(ImageOpenPayload.TYPE, (payload, context) -> pendingOpenUploadScreen = true);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			String json = ClientInfoCollector.collectAsJson();
			sender.sendPacket(new ClientInfoPayload(json));
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ImageUploader.clearQueue());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (pendingOpenUploadScreen) {
				pendingOpenUploadScreen = false;
				client.setScreenAndShow(new ImageUploadScreen());
			}
			drainUploadQueue(client);
		});
	}

	private static void drainUploadQueue(Minecraft client) {
		if (client.getConnection() == null || !ImageUploader.hasQueued()) {
			return;
		}
		int sent = 0;
		while (sent < 8 && ImageUploader.hasQueued() && ClientPlayNetworking.canSend(ImagePayload.TYPE)) {
			ClientPlayNetworking.send(new ImagePayload(ImageUploader.poll()));
			sent++;
		}
	}
}
