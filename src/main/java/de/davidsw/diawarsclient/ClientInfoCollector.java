package de.davidsw.diawarsclient;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.ArrayList;
import java.util.List;

public class ClientInfoCollector {
    private static final Gson GSON = new Gson();

    public record ModEntry(String id, String version) {}

    public record ClientInfo(
            String minecraftVersion,
            String loaderType,
            String loaderVersion,
            boolean isLunarClient,
            List<ModEntry> mods,
            List<String> resourcePacks,
            boolean shaderModInstalled,
            boolean shadersEnabled,
            String activeShaderPack
    ) {}

    public static String collectAsJson() {
        List<ModEntry> mods = new ArrayList<>();
        String minecraftVersion = "unknown";
        String loaderVersion = "unknown";

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String id = mod.getMetadata().getId();
            String version = mod.getMetadata().getVersion().getFriendlyString();

            if (id.equals("minecraft")) {
                minecraftVersion = version;
                continue;
            }
            if (id.equals("fabricloader")) {
                loaderVersion = version;
                continue;
            }
            if (id.startsWith("fabric-") || id.equals("fabric")) continue;

            mods.add(new ModEntry(id, version));
        }

        ShaderInfoCollector.ShaderInfo shaderInfo = ShaderInfoCollector.collect();

        ClientInfo info = new ClientInfo(
                minecraftVersion,
                "fabric",
                loaderVersion,
                detectLunarClient(),
                mods,
                ResourcePackCollector.collectEnabledPacks(),
                shaderInfo.installed(),
                shaderInfo.enabled(),
                shaderInfo.activePackName()
        );

        return GSON.toJson(info);
    }

    private static boolean detectLunarClient() {
        try {
            Class.forName("com.moonsworth.lunar.genesis.Genesis");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}