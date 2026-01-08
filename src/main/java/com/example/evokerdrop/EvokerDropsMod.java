package com.example.evokerdrop;

import com.example.evokerdrop.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.KilledByPlayerLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class EvokerDropsMod implements ModInitializer {
    public static final String MOD_ID = "evokerdrops";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ModConfig config;
    private static MinecraftServer currentServer = null;
    private static long lastConfigUpdate = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Evoker Drops Mod");

        config = ModConfig.load();
        lastConfigUpdate = System.currentTimeMillis();
        LOGGER.info("Configuration loaded with {} mob(s) configured", config.mobConfigs.size());

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            currentServer = server;
            LOGGER.info("Server started, tracking for config reloads");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            currentServer = null;
        });

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            ModConfig currentConfig = getConfig();

            for (ModConfig.MobDropConfig mobConfig : currentConfig.mobConfigs) {
                if (!mobConfig.enabled) {
                    continue;
                }

                Identifier mobLootTableId;
                try {
                    String[] parts = mobConfig.mobId.split(":");
                    if (parts.length == 2) {
                        mobLootTableId = Identifier.of(parts[0], "entities/" + parts[1]);
                    } else {
                        mobLootTableId = Identifier.of("minecraft", "entities/" + mobConfig.mobId);
                    }
                } catch (Exception e) {
                    LOGGER.error("Invalid mob ID: {}", mobConfig.mobId, e);
                    continue;
                }

                if (mobLootTableId.equals(key.getValue())) {
                    LOGGER.debug("Modifying loot table for: {}", mobConfig.mobId);

                    // FIXED: Get looting enchantment untuk MC 1.21.2 Fabric
                    RegistryEntry<Enchantment> lootingEnchantment = null;
                    try {
                        // Gunakan registries.getOrThrow untuk mendapatkan wrapper
                        var enchantmentWrapper = registries.getOrThrow(RegistryKeys.ENCHANTMENT);

                        RegistryKey<Enchantment> lootingKey = RegistryKey.of(
                                RegistryKeys.ENCHANTMENT,
                                Identifier.of("minecraft", "looting")
                        );

                        // Gunakan getOptional untuk get enchantment dari wrapper
                        Optional<RegistryEntry.Reference<Enchantment>> lootingOpt =
                                enchantmentWrapper.getOptional(lootingKey);

                        if (lootingOpt.isPresent()) {
                            lootingEnchantment = lootingOpt.get();
                            LOGGER.debug("Successfully retrieved Looting enchantment for mob: {}", mobConfig.mobId);
                        } else {
                            LOGGER.warn("Looting enchantment not found in registry for mob: {}", mobConfig.mobId);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Could not get Looting enchantment from registry: {}", e.getMessage());
                        LOGGER.warn("Looting enchantment will not be applied to drops for mob: {}", mobConfig.mobId);
                    }

                    for (ModConfig.ItemDropConfig itemDrop : mobConfig.itemDrops) {
                        if (!itemDrop.enabled) {
                            continue;
                        }

                        Item item;
                        try {
                            Identifier itemId = Identifier.tryParse(itemDrop.itemId);
                            if (itemId == null) {
                                LOGGER.error("Invalid item ID: {}", itemDrop.itemId);
                                continue;
                            }

                            item = Registries.ITEM.get(itemId);
                            if (item == null || item == Items.AIR) {
                                LOGGER.error("Item not found: {}", itemDrop.itemId);
                                continue;
                            }
                        } catch (Exception e) {
                            LOGGER.error("Error getting item: {}", itemDrop.itemId, e);
                            continue;
                        }

                        LootPool.Builder poolBuilder = LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(1));

                        ItemEntry.Builder<?> itemEntryBuilder = ItemEntry.builder(item);

                        if (itemDrop.minCount != itemDrop.maxCount) {
                            itemEntryBuilder.apply(SetCountLootFunction.builder(
                                    UniformLootNumberProvider.create(
                                            (float) itemDrop.minCount,
                                            (float) itemDrop.maxCount
                                    )
                            ));
                        } else if (itemDrop.minCount > 1) {
                            itemEntryBuilder.apply(SetCountLootFunction.builder(
                                    ConstantLootNumberProvider.create((float) itemDrop.minCount)
                            ));
                        }

                        if (itemDrop.applyLooting && lootingEnchantment != null) {
                            try {
                                itemEntryBuilder.apply(
                                        ApplyBonusLootFunction.uniformBonusCount(lootingEnchantment, 1)
                                );
                                LOGGER.debug("Applied looting enchantment to item: {}", itemDrop.itemId);
                            } catch (Exception e) {
                                LOGGER.warn("Could not apply looting enchantment to {}: {}",
                                        itemDrop.itemId, e.getMessage());
                            }
                        }

                        poolBuilder.with(itemEntryBuilder);
                        poolBuilder.conditionally(KilledByPlayerLootCondition.builder());

                        if (itemDrop.dropChance < 1.0f) {
                            poolBuilder.conditionally(
                                    RandomChanceLootCondition.builder(itemDrop.dropChance)
                            );
                        }

                        tableBuilder.pool(poolBuilder);
                        LOGGER.debug("  Added drop: {} x{}-{} ({}% chance) with Looting: {}",
                                itemDrop.itemId,
                                itemDrop.minCount,
                                itemDrop.maxCount,
                                Math.round(itemDrop.dropChance * 100),
                                itemDrop.applyLooting);
                    }
                }
            }
        });

        LOGGER.info("Evoker Drops Mod initialized successfully");
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static void reloadConfig() {
        LOGGER.info("Reloading configuration...");
        ModConfig oldConfig = config;
        config = ModConfig.load();
        lastConfigUpdate = System.currentTimeMillis();

        if (oldConfig != null) {
            LOGGER.info("Config updated: {} mob(s) -> {} mob(s)",
                    oldConfig.mobConfigs.size(), config.mobConfigs.size());

            for (ModConfig.MobDropConfig mobConfig : config.mobConfigs) {
                LOGGER.debug("Mob: {} with {} item drops", mobConfig.mobId, mobConfig.itemDrops.size());
            }
        }

        if (currentServer != null) {
            try {
                var resourcePackManager = currentServer.getDataPackManager();
                var enabledPacks = resourcePackManager.getEnabledIds();

                currentServer.execute(() -> {
                    try {
                        LOGGER.info("Starting server resource reload...");
                        currentServer.reloadResources(enabledPacks)
                                .thenRun(() -> {
                                    LOGGER.info("§a[Evoker Drops] Server resources reloaded successfully!");
                                    LOGGER.info("§a[Evoker Drops] New drops are now active!");
                                })
                                .exceptionally(throwable -> {
                                    LOGGER.error("Failed to reload resources after config change", throwable);
                                    LOGGER.warn("§e[Evoker Drops] Auto-reload failed, use /reload for immediate effect");
                                    return null;
                                });
                    } catch (Exception e) {
                        LOGGER.error("Error during server reload execution", e);
                    }
                });
            } catch (Exception e) {
                LOGGER.warn("Could not auto-reload server resources: {}", e.getMessage());
                LOGGER.info("§e[Evoker Drops] Config saved! Changes will apply to newly spawned mobs.");
            }
        } else {
            LOGGER.info("§a[Evoker Drops] Config reloaded with {} mob(s)", config.mobConfigs.size());
            LOGGER.info("§7Server not available - changes will apply when server starts");
        }
    }

    public static long getLastConfigUpdate() {
        return lastConfigUpdate;
    }
}