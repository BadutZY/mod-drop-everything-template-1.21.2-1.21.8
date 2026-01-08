package com.example.evokerdrop;

import com.example.evokerdrop.command.ReloadCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ServerInitializer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        // Register reload command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReloadCommand.register(dispatcher);
        });
    }
}