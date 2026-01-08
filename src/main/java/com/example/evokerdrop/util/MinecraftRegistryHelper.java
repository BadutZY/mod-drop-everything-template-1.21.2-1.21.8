package com.example.evokerdrop.util;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MinecraftRegistryHelper {

    /**
     * Get all available mob IDs from the entity registry
     */
    public static List<String> getAllMobIds() {
        List<String> mobIds = new ArrayList<>();

        // Add common hostile mobs first
        mobIds.add("minecraft:zombie");
        mobIds.add("minecraft:skeleton");
        mobIds.add("minecraft:creeper");
        mobIds.add("minecraft:spider");
        mobIds.add("minecraft:cave_spider");
        mobIds.add("minecraft:enderman");
        mobIds.add("minecraft:evoker");
        mobIds.add("minecraft:vindicator");
        mobIds.add("minecraft:pillager");
        mobIds.add("minecraft:witch");
        mobIds.add("minecraft:ravager");
        mobIds.add("minecraft:vex");
        mobIds.add("minecraft:blaze");
        mobIds.add("minecraft:ghast");
        mobIds.add("minecraft:magma_cube");
        mobIds.add("minecraft:slime");
        mobIds.add("minecraft:wither_skeleton");
        mobIds.add("minecraft:piglin");
        mobIds.add("minecraft:piglin_brute");
        mobIds.add("minecraft:hoglin");
        mobIds.add("minecraft:zoglin");
        mobIds.add("minecraft:zombified_piglin");
        mobIds.add("minecraft:guardian");
        mobIds.add("minecraft:elder_guardian");
        mobIds.add("minecraft:shulker");
        mobIds.add("minecraft:phantom");
        mobIds.add("minecraft:drowned");
        mobIds.add("minecraft:husk");
        mobIds.add("minecraft:stray");
        mobIds.add("minecraft:silverfish");
        mobIds.add("minecraft:endermite");

        // Boss mobs
        mobIds.add("minecraft:wither");
        mobIds.add("minecraft:ender_dragon");

        // Boss mobs
        mobIds.add("minecraft:iron_golem");
        mobIds.add("minecraft:snow_golem");

        // Get all registered entities that are monsters or creatures
        try {
            for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
                Identifier id = Registries.ENTITY_TYPE.getId(entityType);
                if (id != null) {
                    String entityId = id.toString();

                    // Check if entity is a living mob (has spawn group)
                    SpawnGroup spawnGroup = entityType.getSpawnGroup();
                    if (spawnGroup == SpawnGroup.MONSTER ||
                            spawnGroup == SpawnGroup.CREATURE ||
                            spawnGroup == SpawnGroup.AMBIENT ||
                            spawnGroup == SpawnGroup.WATER_CREATURE ||
                            spawnGroup == SpawnGroup.WATER_AMBIENT ||
                            spawnGroup == SpawnGroup.UNDERGROUND_WATER_CREATURE ||
                            spawnGroup == SpawnGroup.AXOLOTLS) {

                        if (!mobIds.contains(entityId)) {
                            mobIds.add(entityId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to predefined list if registry access fails
        }

        Collections.sort(mobIds);
        return mobIds;
    }

    /**
     * Get all available item IDs from the item registry
     */
    public static List<String> getAllItemIds() {
        List<String> itemIds = new ArrayList<>();

        // Add common valuable items first
        itemIds.add("minecraft:diamond");
        itemIds.add("minecraft:emerald");
        itemIds.add("minecraft:netherite_ingot");
        itemIds.add("minecraft:netherite_scrap");
        itemIds.add("minecraft:ancient_debris");
        itemIds.add("minecraft:iron_ingot");
        itemIds.add("minecraft:gold_ingot");
        itemIds.add("minecraft:ender_pearl");
        itemIds.add("minecraft:blaze_rod");
        itemIds.add("minecraft:nether_star");
        itemIds.add("minecraft:elytra");
        itemIds.add("minecraft:totem_of_undying");
        itemIds.add("minecraft:enchanted_golden_apple");
        itemIds.add("minecraft:golden_apple");
        itemIds.add("minecraft:heart_of_the_sea");
        itemIds.add("minecraft:trident");
        itemIds.add("minecraft:shulker_shell");
        itemIds.add("minecraft:dragon_breath");
        itemIds.add("minecraft:phantom_membrane");
        itemIds.add("minecraft:prismarine_shard");
        itemIds.add("minecraft:prismarine_crystals");
        itemIds.add("minecraft:echo_shard");
        itemIds.add("minecraft:netherite_upgrade_smithing_template");
        itemIds.add("minecraft:experience_bottle");

        // Get all registered items
        try {
            for (Item item : Registries.ITEM) {
                if (item != Items.AIR) {
                    Identifier id = Registries.ITEM.getId(item);
                    if (id != null) {
                        String itemId = id.toString();
                        if (!itemIds.contains(itemId)) {
                            itemIds.add(itemId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to predefined list if registry access fails
        }

        Collections.sort(itemIds);
        return itemIds;
    }

    /**
     * Get filtered mob IDs based on search term
     */
    public static List<String> searchMobs(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return getAllMobIds();
        }

        String search = searchTerm.toLowerCase();
        return getAllMobIds().stream()
                .filter(mobId -> mobId.toLowerCase().contains(search))
                .collect(Collectors.toList());
    }

    /**
     * Get filtered item IDs based on search term
     */
    public static List<String> searchItems(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return getAllItemIds();
        }

        String search = searchTerm.toLowerCase();
        return getAllItemIds().stream()
                .filter(itemId -> itemId.toLowerCase().contains(search))
                .collect(Collectors.toList());
    }

    /**
     * Check if a mob ID is valid
     */
    public static boolean isValidMobId(String mobId) {
        if (mobId == null || mobId.isEmpty()) {
            return false;
        }

        try {
            Identifier id = Identifier.tryParse(mobId);
            if (id == null) {
                return false;
            }

            EntityType<?> entityType = Registries.ENTITY_TYPE.get(id);
            return entityType != null && entityType != EntityType.PIG; // PIG is default for invalid
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if an item ID is valid
     */
    public static boolean isValidItemId(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }

        try {
            Identifier id = Identifier.tryParse(itemId);
            if (id == null) {
                return false;
            }

            Item item = Registries.ITEM.get(id);
            return item != null && item != Items.AIR;
        } catch (Exception e) {
            return false;
        }
    }
}