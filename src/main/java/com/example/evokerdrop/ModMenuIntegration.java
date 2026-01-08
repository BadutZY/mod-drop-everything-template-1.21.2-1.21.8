package com.example.evokerdrop;

import com.example.evokerdrop.config.ClothConfigScreen;
import com.example.evokerdrop.config.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ClothConfigScreen.createConfigScreen(parent, EvokerDropsMod.getConfig());
    }
}