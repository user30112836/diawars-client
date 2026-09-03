package de.davidsw.diawarsclient;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResourcePackCollector {
    private static final Set<String> BUILTIN_IDS = Set.of(
            "vanilla", "fabric", "fabric/internal", "programer_art"
    );

    public static List<String> collectEnabledPacks() {
        List<String> packs = new ArrayList<>();

        var repository = Minecraft.getInstance().getResourcePackRepository();
        for (var pack : repository.getSelectedPacks()) {
            if (BUILTIN_IDS.contains(pack.getId())) continue;
            packs.add(pack.getTitle().getString());
        }

        return packs;
    }
}