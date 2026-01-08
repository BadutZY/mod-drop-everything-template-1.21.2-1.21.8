package com.example.evokerdrop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class untuk mengelola konfigurasi mod
 */
public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("EvokerDrops-ConfigManager");

    /**
     * Menambahkan mob baru ke konfigurasi (TANPA default item)
     */
    public static boolean addMob(ModConfig config, String mobId) {
        if (config.mobConfigs.size() >= 5) {
            LOGGER.warn("Cannot add more mobs, maximum is 5");
            return false;
        }

        // Check if mob already exists
        for (ModConfig.MobDropConfig existing : config.mobConfigs) {
            if (existing.mobId.equals(mobId)) {
                LOGGER.warn("Mob {} already exists in configuration", mobId);
                return false;
            }
        }

        ModConfig.MobDropConfig newMob = new ModConfig.MobDropConfig();
        newMob.mobId = mobId;
        newMob.enabled = true;
        // UPDATED: Tidak ada default item - itemDrops list kosong
        config.mobConfigs.add(newMob);

        LOGGER.info("Added new mob to configuration: {} (no default items)", mobId);
        return true;
    }

    /**
     * Menghapus mob dari konfigurasi
     */
    public static boolean removeMob(ModConfig config, int index) {
        if (index < 0 || index >= config.mobConfigs.size()) {
            LOGGER.warn("Invalid mob index: {}", index);
            return false;
        }

        String mobId = config.mobConfigs.get(index).mobId;
        config.mobConfigs.remove(index);
        LOGGER.info("Removed mob from configuration: {}", mobId);
        return true;
    }

    /**
     * Menambahkan item drop ke mob tertentu
     */
    public static boolean addItemDrop(ModConfig config, int mobIndex, String itemId) {
        if (mobIndex < 0 || mobIndex >= config.mobConfigs.size()) {
            LOGGER.warn("Invalid mob index: {}", mobIndex);
            return false;
        }

        ModConfig.MobDropConfig mobConfig = config.mobConfigs.get(mobIndex);

        if (mobConfig.itemDrops.size() >= 7) {
            LOGGER.warn("Cannot add more items to {}, maximum is 7", mobConfig.mobId);
            return false;
        }

        ModConfig.ItemDropConfig newItem = new ModConfig.ItemDropConfig();
        newItem.itemId = itemId;
        newItem.enabled = true;
        newItem.minCount = 1;
        newItem.maxCount = 1;
        newItem.dropChance = 1.0f;
        newItem.applyLooting = true;

        mobConfig.itemDrops.add(newItem);
        LOGGER.info("Added item {} to mob {}", itemId, mobConfig.mobId);
        return true;
    }

    /**
     * Menghapus item drop dari mob
     */
    public static boolean removeItemDrop(ModConfig config, int mobIndex, int itemIndex) {
        if (mobIndex < 0 || mobIndex >= config.mobConfigs.size()) {
            LOGGER.warn("Invalid mob index: {}", mobIndex);
            return false;
        }

        ModConfig.MobDropConfig mobConfig = config.mobConfigs.get(mobIndex);

        if (itemIndex < 0 || itemIndex >= mobConfig.itemDrops.size()) {
            LOGGER.warn("Invalid item index: {}", itemIndex);
            return false;
        }

        String itemId = mobConfig.itemDrops.get(itemIndex).itemId;
        mobConfig.itemDrops.remove(itemIndex);
        LOGGER.info("Removed item {} from mob {}", itemId, mobConfig.mobId);
        return true;
    }

    /**
     * Mendapatkan daftar mob yang tersedia di Minecraft
     */
    public static List<String> getCommonMobs() {
        List<String> mobs = new ArrayList<>();
        mobs.add("minecraft:zombie");
        mobs.add("minecraft:skeleton");
        mobs.add("minecraft:creeper");
        mobs.add("minecraft:spider");
        mobs.add("minecraft:enderman");
        mobs.add("minecraft:evoker");
        mobs.add("minecraft:vindicator");
        mobs.add("minecraft:pillager");
        mobs.add("minecraft:witch");
        mobs.add("minecraft:ravager");
        mobs.add("minecraft:blaze");
        mobs.add("minecraft:wither_skeleton");
        mobs.add("minecraft:piglin");
        mobs.add("minecraft:piglin_brute");
        mobs.add("minecraft:hoglin");
        mobs.add("minecraft:zoglin");
        mobs.add("minecraft:ghast");
        mobs.add("minecraft:magma_cube");
        mobs.add("minecraft:slime");
        mobs.add("minecraft:cave_spider");
        mobs.add("minecraft:silverfish");
        mobs.add("minecraft:guardian");
        mobs.add("minecraft:elder_guardian");
        mobs.add("minecraft:shulker");
        mobs.add("minecraft:phantom");
        mobs.add("minecraft:drowned");
        mobs.add("minecraft:husk");
        mobs.add("minecraft:stray");
        return mobs;
    }

    /**
     * Mendapatkan daftar item yang sering digunakan
     */
    public static List<String> getCommonItems() {
        List<String> items = new ArrayList<>();
        items.add("minecraft:diamond");
        items.add("minecraft:emerald");
        items.add("minecraft:netherite_ingot");
        items.add("minecraft:netherite_scrap");
        items.add("minecraft:ancient_debris");
        items.add("minecraft:iron_ingot");
        items.add("minecraft:gold_ingot");
        items.add("minecraft:ender_pearl");
        items.add("minecraft:blaze_rod");
        items.add("minecraft:nether_star");
        items.add("minecraft:elytra");
        items.add("minecraft:totem_of_undying");
        items.add("minecraft:enchanted_golden_apple");
        items.add("minecraft:golden_apple");
        items.add("minecraft:heart_of_the_sea");
        items.add("minecraft:trident");
        items.add("minecraft:shulker_shell");
        items.add("minecraft:dragon_breath");
        items.add("minecraft:phantom_membrane");
        items.add("minecraft:prismarine_shard");
        items.add("minecraft:prismarine_crystals");
        return items;
    }

    /**
     * Reset konfigurasi ke default (UPDATED: Kosong tanpa mob atau item)
     */
    public static void resetToDefault(ModConfig config) {
        config.mobConfigs.clear();
        LOGGER.info("Configuration reset to empty (no default mobs or items)");
    }
}