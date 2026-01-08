package com.example.evokerdrop;

import com.example.evokerdrop.keybind.KeybindHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class ClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register keybinds
        KeybindHandler.register();
        EvokerDropsMod.LOGGER.info("Keybind handler initialized");

        // Track client lifecycle untuk reload support
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            EvokerDropsMod.LOGGER.info("Client started, config reload support enabled");
        });
    }
}