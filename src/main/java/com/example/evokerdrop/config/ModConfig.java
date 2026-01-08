package com.example.evokerdrop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("EvokerDrops-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "evokerdrops.json"
    );

    // Default configuration - KOSONG
    public List<MobDropConfig> mobConfigs = new ArrayList<>();

    // Transient fields (not saved to JSON)
    private transient List<Runnable> changeListeners = new ArrayList<>();

    public ModConfig() {
        LOGGER.info("Configuration initialized with empty mob list");
    }

    public static ModConfig load() {
        LOGGER.info("=== LOADING CONFIG FROM FILE ===");
        LOGGER.info("Config file path: {}", CONFIG_FILE.getAbsolutePath());
        LOGGER.info("Config file exists: {}", CONFIG_FILE.exists());

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                // Read file content untuk logging
                String content = new String(Files.readAllBytes(CONFIG_FILE.toPath()));
                LOGGER.info("Config file content length: {} bytes", content.length());
                LOGGER.debug("Config file content: {}", content);

                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config == null) {
                    LOGGER.warn("Config parsed as null, creating new empty config");
                    config = new ModConfig();
                } else {
                    LOGGER.info("Config parsed successfully with {} mob(s)", config.mobConfigs.size());
                }

                // Initialize transient fields
                config.changeListeners = new ArrayList<>();

                // Validate and fix if needed
                config.validate();

                // Log loaded config details
                for (ModConfig.MobDropConfig mob : config.mobConfigs) {
                    LOGGER.info("  - Loaded mob: {} (enabled: {}) with {} items",
                            mob.mobId, mob.enabled, mob.itemDrops.size());
                }

                LOGGER.info("=== CONFIG LOAD COMPLETED ===");
                return config;
            } catch (Exception e) {
                LOGGER.error("Failed to load config, using defaults", e);
                return new ModConfig();
            }
        } else {
            LOGGER.info("No config file found, creating new empty configuration");
            ModConfig config = new ModConfig();
            config.save();
            return config;
        }
    }

    public void save() {
        LOGGER.info("=== SAVING CONFIG TO FILE ===");
        LOGGER.info("Config file path: {}", CONFIG_FILE.getAbsolutePath());
        LOGGER.info("Saving {} mob(s)", mobConfigs.size());

        // Log detail config yang akan disave
        for (MobDropConfig mob : mobConfigs) {
            LOGGER.info("  - Saving mob: {} (enabled: {}) with {} items",
                    mob.mobId, mob.enabled, mob.itemDrops.size());
            for (ItemDropConfig item : mob.itemDrops) {
                LOGGER.info("    * Item: {} (enabled: {}, count: {}-{}, chance: {}%)",
                        item.itemId, item.enabled, item.minCount, item.maxCount,
                        Math.round(item.dropChance * 100));
            }
        }

        try {
            // Ensure config directory exists
            File configDir = CONFIG_FILE.getParentFile();
            if (!configDir.exists()) {
                configDir.mkdirs();
                LOGGER.info("Created config directory: {}", configDir.getAbsolutePath());
            }

            // Write to file
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
                writer.flush(); // Force write to disk
            }

            // Verify file was written
            if (CONFIG_FILE.exists()) {
                long fileSize = CONFIG_FILE.length();
                LOGGER.info("Config file written successfully, size: {} bytes", fileSize);

                // Read back untuk verify
                String savedContent = new String(Files.readAllBytes(CONFIG_FILE.toPath()));
                LOGGER.debug("Saved config content: {}", savedContent);

                // Verify dengan parse ulang
                ModConfig verifyConfig = GSON.fromJson(savedContent, ModConfig.class);
                if (verifyConfig != null) {
                    LOGGER.info("Config file verified: {} mob(s) saved correctly", verifyConfig.mobConfigs.size());
                } else {
                    LOGGER.error("Config file verification failed: parsed as null");
                }
            } else {
                LOGGER.error("Config file does not exist after write!");
            }

            notifyChanges();
            LOGGER.info("=== CONFIG SAVE COMPLETED ===");
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
            LOGGER.error("Stack trace:", e);
        }
    }

    private void validate() {
        // Ensure we don't have more than 5 mob configs
        if (mobConfigs.size() > 5) {
            LOGGER.warn("Too many mobs ({}), limiting to 5", mobConfigs.size());
            mobConfigs = new ArrayList<>(mobConfigs.subList(0, 5));
        }

        // Validate each mob config
        for (MobDropConfig mobConfig : mobConfigs) {
            // Ensure each mob doesn't have more than 7 item drops
            if (mobConfig.itemDrops.size() > 7) {
                LOGGER.warn("Mob {} has too many items ({}), limiting to 7",
                        mobConfig.mobId, mobConfig.itemDrops.size());
                mobConfig.itemDrops = new ArrayList<>(mobConfig.itemDrops.subList(0, 7));
            }

            // Validate each item drop
            for (ItemDropConfig itemDrop : mobConfig.itemDrops) {
                // Ensure drop chance is between 0 and 1
                if (itemDrop.dropChance < 0) {
                    LOGGER.warn("Invalid drop chance {} for item {}, setting to 0",
                            itemDrop.dropChance, itemDrop.itemId);
                    itemDrop.dropChance = 0;
                }
                if (itemDrop.dropChance > 1) {
                    LOGGER.warn("Invalid drop chance {} for item {}, setting to 1",
                            itemDrop.dropChance, itemDrop.itemId);
                    itemDrop.dropChance = 1;
                }

                // Ensure count values are positive
                if (itemDrop.minCount < 1) {
                    LOGGER.warn("Invalid minCount {} for item {}, setting to 1",
                            itemDrop.minCount, itemDrop.itemId);
                    itemDrop.minCount = 1;
                }
                if (itemDrop.maxCount < itemDrop.minCount) {
                    LOGGER.warn("maxCount {} less than minCount {} for item {}, fixing",
                            itemDrop.maxCount, itemDrop.minCount, itemDrop.itemId);
                    itemDrop.maxCount = itemDrop.minCount;
                }
            }
        }
    }

    public void addChangeListener(Runnable listener) {
        if (changeListeners == null) {
            changeListeners = new ArrayList<>();
        }
        changeListeners.add(listener);
    }

    private void notifyChanges() {
        if (changeListeners != null) {
            for (Runnable listener : changeListeners) {
                try {
                    listener.run();
                } catch (Exception e) {
                    LOGGER.error("Error notifying change listener", e);
                }
            }
        }
    }

    public static class MobDropConfig {
        public String mobId = "";
        public boolean enabled = true;
        public List<ItemDropConfig> itemDrops = new ArrayList<>();

        public MobDropConfig() {
            // Tidak ada default - biarkan kosong
        }

        public MobDropConfig copy() {
            MobDropConfig copy = new MobDropConfig();
            copy.mobId = this.mobId;
            copy.enabled = this.enabled;
            copy.itemDrops = new ArrayList<>();
            for (ItemDropConfig item : this.itemDrops) {
                copy.itemDrops.add(item.copy());
            }
            return copy;
        }
    }

    public static class ItemDropConfig {
        public String itemId = "";
        public boolean enabled = true;
        public int minCount = 1;
        public int maxCount = 1;
        public float dropChance = 1.0f;
        public boolean applyLooting = true;

        public ItemDropConfig() {
            // Tidak ada default - biarkan kosong
        }

        public ItemDropConfig copy() {
            ItemDropConfig copy = new ItemDropConfig();
            copy.itemId = this.itemId;
            copy.enabled = this.enabled;
            copy.minCount = this.minCount;
            copy.maxCount = this.maxCount;
            copy.dropChance = this.dropChance;
            copy.applyLooting = this.applyLooting;
            return copy;
        }
    }
}