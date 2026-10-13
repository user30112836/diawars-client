package de.davidsw.diawarsclient;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 * Chunked image upload to the Diawars Paper plugin.
 *
 * <p>Wire format: {@code ServerboundCustomPayloadPacket} with id
 * {@code diawars:image} and a single {@code STRING_UTF8} JSON document per
 * chunk: {@code {"file": "...", "part": 0, "total": N, "data": "<base64>"}}.
 * The client resizes the image to 128x128 before sending (see
 * {@link ImageUploader}); the server reassembles the parts and answers in
 * chat. Chunk payloads stay far below packet limits so every payload is small.
 */
public record ImagePayload(String json) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ImagePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("diawars", "image"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImagePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ImagePayload::json,
                    ImagePayload::new
            );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
