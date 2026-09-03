package de.davidsw.diawarsclient;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;
import java.util.Optional;

public class ShaderInfoCollector {

    public record ShaderInfo(boolean installed, boolean enabled, String activePackName) {}

    public static ShaderInfo collect() {
        boolean installed = FabricLoader.getInstance().isModLoaded("iris");
        if (!installed) return new ShaderInfo(false, false, null);

        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object apiInstance = irisApiClass.getMethod("getInstance").invoke(null);

            Method isInUseMethod = irisApiClass.getMethod("isShaderPackInUse");
            boolean enabled = (boolean) isInUseMethod.invoke(apiInstance);

            String packName = null;
            if (enabled) {
                Method getPackNameMethod = irisApiClass.getMethod("getCurrentPackName");
                Object result = getPackNameMethod.invoke(apiInstance);
                if (result instanceof Optional<?> optional) {
                    packName = optional.map(Object::toString).orElse(null);
                }
            }

            return new ShaderInfo(true, enabled, packName);
        } catch (Exception | LinkageError e) {
            return new ShaderInfo(true, false, null);
        }
    }
}