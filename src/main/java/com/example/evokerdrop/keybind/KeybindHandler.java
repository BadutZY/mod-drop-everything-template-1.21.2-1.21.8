package com.example.evokerdrop.keybind;

import com.example.evokerdrop.EvokerDropsMod;
import com.example.evokerdrop.config.ClothConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Handler untuk keybind mod
 */
public class KeybindHandler {

    // Keybind untuk membuka config
    private static KeyBinding openConfigKey;

    /**
     * Register keybind dan event handler
     */
    public static void register() {
        // Buat keybind baru
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.evokerdrop.openconfig", // Translation key
                InputUtil.Type.KEYSYM, // Input type (keyboard)
                GLFW.GLFW_KEY_M, // Default key: M
                "category.evokerdrop.keybinds" // Category translation key
        ));

        // Register client tick event untuk detect key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check jika key ditekan
            while (openConfigKey.wasPressed()) {
                // Pastikan client dan screen tersedia
                if (client.player != null) {
                    // Buka config screen
                    client.execute(() -> {
                        try {
                            client.setScreen(ClothConfigScreen.createConfigScreen(
                                    client.currentScreen,
                                    EvokerDropsMod.getConfig()
                            ));
                            EvokerDropsMod.LOGGER.info("Config screen opened via keybind");
                        } catch (Exception e) {
                            EvokerDropsMod.LOGGER.error("Failed to open config screen via keybind", e);
                        }
                    });
                }
            }
        });

        EvokerDropsMod.LOGGER.info("Keybinds registered successfully");
    }

    /**
     * Get keybind untuk config
     */
    public static KeyBinding getOpenConfigKey() {
        return openConfigKey;
    }
}