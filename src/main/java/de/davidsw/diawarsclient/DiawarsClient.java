package de.davidsw.diawarsclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class DiawarsClient implements ClientModInitializer {
	public static final String MOD_ID = "diawars-client";

	private static KeyMapping openUploadScreenKey;

	/**
	 * Set by the {@code /diawarsimage} command: the screen must NOT open
	 * directly in the command callback (it runs inside ChatScreen input
	 * handling, which closes the current screen right after), so the tick
	 * handler opens it on the next tick instead.
	 */
	private static volatile boolean pendingOpenUploadScreen;

	@Override
	public void onInitializeClient() {
		PayloadTypeRegistry.serverboundPlay().register(ClientInfoPayload.TYPE, ClientInfoPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ImagePayload.TYPE, ImagePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ImageOpenPayload.TYPE, ImageOpenPayload.CODEC);
		ClientPlayNetworking.registerReceiver(ImageOpenPayload.TYPE, (payload, context) -> pendingOpenUploadScreen = true);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			String json = ClientInfoCollector.collectAsJson();
			sender.sendPacket(new ClientInfoPayload(json));
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ImageUploader.clearQueue());

		openUploadScreenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.diawars-client.upload_image",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_U,
				KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean open = openUploadScreenKey.consumeClick() || pendingOpenUploadScreen;
			pendingOpenUploadScreen = false;
			if (open) {
				client.setScreenAndShow(new ImageUploadScreen());
			}
			drainUploadQueue(client);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommands.literal("diawarsimage").executes(context -> {
					pendingOpenUploadScreen = true;
					return 1;
				}))
		);
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
