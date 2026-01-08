package com.example.evokerdrop.config;

import com.example.evokerdrop.EvokerDropsMod;
import com.example.evokerdrop.util.MinecraftRegistryHelper;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BaseListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClothConfigScreen {

    private static List<ModConfig.MobDropConfig> workingMobConfigs;

    public static Screen createConfigScreen(Screen parent, ModConfig config) {
        // Create a working copy of the config
        workingMobConfigs = new ArrayList<>();
        for (ModConfig.MobDropConfig mob : config.mobConfigs) {
            workingMobConfigs.add(mob.copy());
        }

        return buildConfigScreen(parent, config, false);
    }

    private static Screen buildConfigScreen(Screen parent, ModConfig config, boolean autoSave) {
        // Auto-save jika dipanggil dari instant add/remove
        if (autoSave) {
            config.mobConfigs.clear();
            for (ModConfig.MobDropConfig mob : workingMobConfigs) {
                config.mobConfigs.add(mob.copy());
            }
            config.save();
            EvokerDropsMod.reloadConfig();
            EvokerDropsMod.LOGGER.info("Auto-saved configuration with {} mob(s)", config.mobConfigs.size());
        }

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Mob Drop Everything Configuration"))
                .setSavingRunnable(() -> {
                    // Update the actual config with working copy
                    config.mobConfigs.clear();
                    for (ModConfig.MobDropConfig mob : workingMobConfigs) {
                        config.mobConfigs.add(mob.copy());
                    }
                    config.save();

                    // Trigger immediate reload di client dan server
                    EvokerDropsMod.reloadConfig();

                    // Silent reload tanpa command error message
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.world != null) {
                        EvokerDropsMod.LOGGER.info("Config saved in world, changes will apply immediately");
                    }

                    EvokerDropsMod.LOGGER.info("Configuration saved and reloaded with {} mob(s)", config.mobConfigs.size());
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();



        // ========== MOB MANAGEMENT CATEGORY ==========
        ConfigCategory mobManagementCategory = builder.getOrCreateCategory(Text.literal("📋 Mob Management"));

        mobManagementCategory.addEntry(entryBuilder.startTextDescription(
                Text.literal("§eManage which mobs will drop custom items. Maximum 5 mobs.")
        ).build());

        mobManagementCategory.addEntry(entryBuilder.startTextDescription(
                Text.literal("§7Current mobs: §a" + workingMobConfigs.size() + "§7/§a5")
        ).build());

        // Add Mob Section dengan TOMBOL CUSTOM
        if (workingMobConfigs.size() < 5) {
            mobManagementCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§a━━━━━━━ Add New Mob ━━━━━━━")
            ).build());

            // Dropdown untuk select mob
            List<String> availableMobs = MinecraftRegistryHelper.getAllMobIds();
            List<String> mobDisplayNames = new ArrayList<>();
            Map<String, String> displayNameToId = new HashMap<>();

            for (String mobId : availableMobs) {
                String displayName = formatMobName(mobId);
                mobDisplayNames.add(displayName);
                displayNameToId.put(displayName, mobId);
            }

            // Buat string field reference yang bisa di-track
            final AbstractConfigListEntry<String> dropdownEntry = entryBuilder.startStringDropdownMenu(
                            Text.literal("🔍 Select Mob (Type to search)"),
                            ""
                    )
                    .setDefaultValue("")
                    .setSelections(mobDisplayNames)
                    .setSuggestionMode(true)
                    .setTooltip(
                            Text.literal("§eType to search for mobs"),
                            Text.literal("§7Select a mob, then click 'ADD MOB' button below")
                    )
                    .build();

            mobManagementCategory.addEntry(dropdownEntry);

            // String field untuk custom mob ID
            final AbstractConfigListEntry<String> customFieldEntry = entryBuilder.startStrField(
                            Text.literal("Custom Mob ID (Optional)"),
                            ""
                    )
                    .setDefaultValue("")
                    .setTooltip(
                            Text.literal("§eEnter full ID: modname:mob_name"),
                            Text.literal("§7Example: minecraft:zombie")
                    )
                    .build();

            mobManagementCategory.addEntry(customFieldEntry);

            // CUSTOM BUTTON ENTRY untuk ADD MOB
            mobManagementCategory.addEntry(new ButtonEntry(
                    Text.literal("§a✚ ADD MOB"),
                    button -> {
                        // Coba ambil dari custom field dulu
                        String mobId = customFieldEntry.getValue();

                        // Kalau custom field kosong, coba dari dropdown
                        if (mobId == null || mobId.isEmpty()) {
                            String displayName = dropdownEntry.getValue();
                            if (displayName != null && !displayName.isEmpty()) {
                                mobId = displayNameToId.get(displayName);
                            }
                        }

                        EvokerDropsMod.LOGGER.info("ADD MOB button clicked, mob ID: {}", mobId);

                        if (mobId == null || mobId.isEmpty()) {
                            EvokerDropsMod.LOGGER.warn("No mob ID selected or entered!");
                            return;
                        }

                        if (workingMobConfigs.size() >= 5) {
                            EvokerDropsMod.LOGGER.warn("Maximum mob limit reached!");
                            return;
                        }

                        // Check if mob already exists
                        boolean exists = false;
                        for (ModConfig.MobDropConfig existing : workingMobConfigs) {
                            if (existing.mobId.equals(mobId)) {
                                exists = true;
                                break;
                            }
                        }

                        if (exists) {
                            EvokerDropsMod.LOGGER.warn("Mob {} already exists!", mobId);
                            return;
                        }

                        // Add new mob
                        addNewMob(mobId);
                        EvokerDropsMod.LOGGER.info("Successfully added mob: {}, total mobs: {}", mobId, workingMobConfigs.size());

                        // INSTANT RELOAD dengan AUTO-SAVE
                        MinecraftClient client = MinecraftClient.getInstance();
                        client.execute(() -> {
                            EvokerDropsMod.LOGGER.info("Reloading config screen with {} mobs...", workingMobConfigs.size());
                            Screen newScreen = buildConfigScreen(parent, config, true); // Auto-save = true
                            client.setScreen(newScreen);
                        });
                    },
                    Text.literal("§eClick to add the selected mob immediately!"),
                    Text.literal("§7Changes are saved automatically")
            ));

        } else {
            mobManagementCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§c⚠ Maximum mob limit reached (5/5)")
            ).build());
        }

        // Display existing mobs
        for (int i = 0; i < workingMobConfigs.size(); i++) {
            final int mobIndex = i;
            ModConfig.MobDropConfig mobConfig = workingMobConfigs.get(mobIndex);

            mobManagementCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            ).build());

            mobManagementCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§e🗡 Mob #" + (mobIndex + 1) + ": §f" + getMobName(mobConfig.mobId))
            ).build());

            mobManagementCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§8ID: " + mobConfig.mobId)
            ).build());

            // Enable/Disable toggle
            mobManagementCategory.addEntry(entryBuilder.startBooleanToggle(
                            Text.literal("  ✓ Enable Custom Drops"),
                            mobConfig.enabled
                    )
                    .setDefaultValue(true)
                    .setTooltip(Text.literal("Enable or disable custom drops for this mob"))
                    .setSaveConsumer(newValue -> {
                        if (mobIndex < workingMobConfigs.size()) {
                            workingMobConfigs.get(mobIndex).enabled = newValue;
                        }
                    })
                    .build());

            // TOMBOL REMOVE MOB (Merah)
            mobManagementCategory.addEntry(new ButtonEntry(
                    Text.literal("§c✖ REMOVE MOB"),
                    button -> {
                        EvokerDropsMod.LOGGER.info("REMOVE MOB button clicked for: {}", mobConfig.mobId);

                        if (mobIndex < workingMobConfigs.size()) {
                            String removedMobId = workingMobConfigs.get(mobIndex).mobId;
                            workingMobConfigs.remove(mobIndex);
                            EvokerDropsMod.LOGGER.info("Removed mob: {}, remaining mobs: {}", removedMobId, workingMobConfigs.size());

                            // INSTANT RELOAD dengan AUTO-SAVE
                            MinecraftClient client = MinecraftClient.getInstance();
                            client.execute(() -> {
                                EvokerDropsMod.LOGGER.info("Reloading config screen after mob removal...");
                                Screen newScreen = buildConfigScreen(parent, config, true); // Auto-save = true
                                client.setScreen(newScreen);
                            });
                        }
                    },
                    Text.literal("§cClick to remove this mob permanently!"),
                    Text.literal("§7Changes are saved automatically")
            ));
        }

        // ========== MOB CONFIGURATION CATEGORIES ==========
        for (int mobIdx = 0; mobIdx < workingMobConfigs.size(); mobIdx++) {
            final int mobIndex = mobIdx;
            ModConfig.MobDropConfig mobConfig = workingMobConfigs.get(mobIndex);

            ConfigCategory mobCategory = builder.getOrCreateCategory(
                    Text.literal("🗡 " + getMobName(mobConfig.mobId))
            );

            mobCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§eMob Configuration")
            ).build());

            // Dropdown untuk update Mob ID
            List<String> availableMobsForChange = MinecraftRegistryHelper.getAllMobIds();
            List<String> mobDisplayNamesForChange = new ArrayList<>();
            Map<String, String> mobDisplayNameToId = new HashMap<>();

            for (String mobId : availableMobsForChange) {
                String displayName = formatMobName(mobId);
                mobDisplayNamesForChange.add(displayName);
                mobDisplayNameToId.put(displayName, mobId);
            }

            String currentMobDisplayName = formatMobName(mobConfig.mobId);

            mobCategory.addEntry(
                    entryBuilder.startStringDropdownMenu(
                                    Text.literal("Mob ID (🔍 Type to search)"),
                                    currentMobDisplayName
                            )
                            .setDefaultValue(currentMobDisplayName)
                            .setSelections(mobDisplayNamesForChange)
                            .setSuggestionMode(true)
                            .setTooltip(
                                    Text.literal("§eSelect mob from dropdown or type to search"),
                                    Text.literal("§7Current: " + mobConfig.mobId),
                                    Text.literal("§7For custom mods: type full ID manually")
                            )
                            .setSaveConsumer(displayName -> {
                                if (mobIndex < workingMobConfigs.size()) {
                                    String newMobId = mobDisplayNameToId.get(displayName);
                                    if (newMobId != null) {
                                        workingMobConfigs.get(mobIndex).mobId = newMobId;
                                        EvokerDropsMod.LOGGER.info("Changed mob to: {} ({})", displayName, newMobId);
                                    } else if (!displayName.isEmpty()) {
                                        workingMobConfigs.get(mobIndex).mobId = displayName;
                                        EvokerDropsMod.LOGGER.info("Set custom mob ID: {}", displayName);
                                    }
                                }
                            })
                            .build()
            );

            mobCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            ).build());

            mobCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§eItem Drops Configuration")
            ).build());

            mobCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("§7Items: §a" + mobConfig.itemDrops.size() + "§7/§a7")
            ).build());

            // Add Item Section dengan TOMBOL CUSTOM
            if (mobConfig.itemDrops.size() < 7) {
                mobCategory.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§a━━━━━━━ Add New Item ━━━━━━━")
                ).build());

                // Dropdown untuk select item
                List<String> availableItems = MinecraftRegistryHelper.getAllItemIds();
                List<String> itemDisplayNames = new ArrayList<>();
                Map<String, String> itemDisplayNameToId = new HashMap<>();

                for (String itemId : availableItems) {
                    String displayName = formatItemName(itemId);
                    itemDisplayNames.add(displayName);
                    itemDisplayNameToId.put(displayName, itemId);
                }

                // Buat dropdown entry reference
                final AbstractConfigListEntry<String> itemDropdownEntry = entryBuilder.startStringDropdownMenu(
                                Text.literal("🔍 Select Item (Type to search)"),
                                ""
                        )
                        .setDefaultValue("")
                        .setSelections(itemDisplayNames)
                        .setSuggestionMode(true)
                        .setTooltip(
                                Text.literal("§eType to search for items"),
                                Text.literal("§7Select an item, then click 'ADD ITEM' button below")
                        )
                        .build();

                mobCategory.addEntry(itemDropdownEntry);

                // String field untuk custom item ID
                final AbstractConfigListEntry<String> customItemFieldEntry = entryBuilder.startStrField(
                                Text.literal("Custom Item ID (Optional)"),
                                ""
                        )
                        .setDefaultValue("")
                        .setTooltip(
                                Text.literal("§eEnter full ID: modname:item_name"),
                                Text.literal("§7Example: minecraft:diamond")
                        )
                        .build();

                mobCategory.addEntry(customItemFieldEntry);

                // CUSTOM BUTTON ENTRY untuk ADD ITEM
                mobCategory.addEntry(new ButtonEntry(
                        Text.literal("§a✚ ADD ITEM"),
                        button -> {
                            // Coba ambil dari custom field dulu
                            String itemId = customItemFieldEntry.getValue();

                            // Kalau custom field kosong, coba dari dropdown
                            if (itemId == null || itemId.isEmpty()) {
                                String displayName = itemDropdownEntry.getValue();
                                if (displayName != null && !displayName.isEmpty()) {
                                    itemId = itemDisplayNameToId.get(displayName);
                                }
                            }

                            EvokerDropsMod.LOGGER.info("ADD ITEM button clicked for mob {}, item ID: {}",
                                    mobConfig.mobId, itemId);

                            if (itemId == null || itemId.isEmpty()) {
                                EvokerDropsMod.LOGGER.warn("No item ID selected or entered!");
                                return;
                            }

                            if (mobConfig.itemDrops.size() >= 7) {
                                EvokerDropsMod.LOGGER.warn("Maximum item limit reached for mob {}!", mobConfig.mobId);
                                return;
                            }

                            // Add new item
                            addNewItem(mobConfig, itemId);
                            EvokerDropsMod.LOGGER.info("Successfully added item: {} to mob {}, total items: {}",
                                    itemId, mobConfig.mobId, mobConfig.itemDrops.size());

                            // INSTANT RELOAD dengan AUTO-SAVE
                            MinecraftClient client = MinecraftClient.getInstance();
                            client.execute(() -> {
                                EvokerDropsMod.LOGGER.info("Reloading config screen with updated items...");
                                Screen newScreen = buildConfigScreen(parent, config, true); // Auto-save = true
                                client.setScreen(newScreen);
                            });
                        },
                        Text.literal("§eClick to add the selected item immediately!"),
                        Text.literal("§7Changes are saved automatically")
                ));
            }

            // Display items
            for (int itemIdx = 0; itemIdx < mobConfig.itemDrops.size(); itemIdx++) {
                final int itemIndex = itemIdx;
                ModConfig.ItemDropConfig itemDrop = mobConfig.itemDrops.get(itemIndex);

                mobCategory.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                ).build());

                mobCategory.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§6💎 Item Drop #" + (itemIndex + 1) + ": §f" + getItemName(itemDrop.itemId))
                ).build());

                mobCategory.addEntry(entryBuilder.startTextDescription(
                        Text.literal("§8ID: " + itemDrop.itemId)
                ).build());

                // Dropdown untuk update Item ID
                List<String> availableItemsForChange = MinecraftRegistryHelper.getAllItemIds();
                List<String> itemDisplayNamesForChange = new ArrayList<>();
                Map<String, String> itemDisplayNameToIdForChange = new HashMap<>();

                for (String itemId : availableItemsForChange) {
                    String displayName = formatItemName(itemId);
                    itemDisplayNamesForChange.add(displayName);
                    itemDisplayNameToIdForChange.put(displayName, itemId);
                }

                String currentItemDisplayName = formatItemName(itemDrop.itemId);

                mobCategory.addEntry(
                        entryBuilder.startStringDropdownMenu(
                                        Text.literal("  Item ID (🔍 Type to search)"),
                                        currentItemDisplayName
                                )
                                .setDefaultValue(currentItemDisplayName)
                                .setSelections(itemDisplayNamesForChange)
                                .setSuggestionMode(true)
                                .setTooltip(
                                        Text.literal("§eSelect item from dropdown or type to search"),
                                        Text.literal("§7Current: " + itemDrop.itemId),
                                        Text.literal("§7For custom mods: type full ID manually")
                                )
                                .setSaveConsumer(displayName -> {
                                    if (mobIndex < workingMobConfigs.size() &&
                                            itemIndex < workingMobConfigs.get(mobIndex).itemDrops.size()) {
                                        String newItemId = itemDisplayNameToIdForChange.get(displayName);
                                        if (newItemId != null) {
                                            workingMobConfigs.get(mobIndex).itemDrops.get(itemIndex).itemId = newItemId;
                                            EvokerDropsMod.LOGGER.info("Changed item to: {} ({}) for mob {}",
                                                    displayName, newItemId, mobConfig.mobId);
                                        } else if (!displayName.isEmpty()) {
                                            workingMobConfigs.get(mobIndex).itemDrops.get(itemIndex).itemId = displayName;
                                            EvokerDropsMod.LOGGER.info("Set custom item ID: {} for mob {}",
                                                    displayName, mobConfig.mobId);
                                        }
                                    }
                                })
                                .build()
                );

                // Enabled
                mobCategory.addEntry(entryBuilder.startBooleanToggle(
                                Text.literal("  ✓ Enable This Drop"),
                                itemDrop.enabled
                        )
                        .setDefaultValue(true)
                        .setSaveConsumer(newValue -> {
                            if (mobIndex < workingMobConfigs.size() &&
                                    itemIndex < workingMobConfigs.get(mobIndex).itemDrops.size()) {
                                workingMobConfigs.get(mobIndex).itemDrops.get(itemIndex).enabled = newValue;
                            }
                        })
                        .build());

                // Min Count
                mobCategory.addEntry(entryBuilder.startIntSlider(
                                Text.literal("  Minimum Count"),
                                itemDrop.minCount,
                                1,
                                64
                        )
                        .setDefaultValue(1)
                        .setTooltip(Text.literal("Minimum number of items dropped"))
                        .setSaveConsumer(newValue -> {
                            if (mobIndex < workingMobConfigs.size() &&
                                    itemIndex < workingMobConfigs.get(mobIndex).itemDrops.size()) {
                                workingMobConfigs.get(mobIndex).itemDrops.get(itemIndex).minCount = newValue;
                            }
                        })
                        .build());

                // Max Count
                mobCategory.addEntry(entryBuilder.startIntSlider(
                                Text.literal("  Maximum Count"),
                                itemDrop.maxCount,
                                1,
                                64
                        )
                        .setDefaultValue(1)
                        .setTooltip(Text.literal("Maximum number of items dropped"))
                        .setSaveConsumer(newValue -> {
                            if (mobIndex < workingMobConfigs.size() &&
                                    itemIndex < workingMobConfigs.get(mobIndex).itemDrops.size()) {
                                workingMobConfigs.get(mobIndex).itemDrops.get(itemIndex).maxCount = newValue;
                            }
                        })
                        .build());

                // Drop Chance
                mobCategory.addEntry(entryBuilder.startIntSlider(
                                Text.literal("  Drop Chance (%)"),
                                Math.round(itemDrop.dropChance * 100),
                                0,
                                100
                        )
                        .setDefaultValue(100)
                        .setTooltip(Text.literal("Percentage chance for this item to drop"))
                        .setSaveConsumer(newValue -> {
                            if (mobIndex < workingMobConfigs.size() &&
                                    itemIndex < workingMobConfigs.get(mobIndex).itemDrops.size()) {
                                workingMobConfigs.get(mobIndex).itemDrops.get(itemIndex).dropChance = newValue / 100.0f;
                            }
                        })
                        .build());

                // Apply Looting
                mobCategory.addEntry(entryBuilder.startBooleanToggle(
                                Text.literal("  ⚔ Apply Looting Enchantment"),
                                itemDrop.applyLooting
                        )
                        .setDefaultValue(true)
                        .setTooltip(Text.literal("Whether Looting enchantment affects this drop"))
                        .setSaveConsumer(newValue -> {
                            if (mobIndex < workingMobConfigs.size() &&
                                    itemIndex < workingMobConfigs.get(mobIndex).itemDrops.size()) {
                                workingMobConfigs.get(mobIndex).itemDrops.get(itemIndex).applyLooting = newValue;
                            }
                        })
                        .build());

                // TOMBOL REMOVE ITEM (Merah)
                mobCategory.addEntry(new ButtonEntry(
                        Text.literal("§c✖ REMOVE ITEM"),
                        button -> {
                            EvokerDropsMod.LOGGER.info("REMOVE ITEM button clicked for item: {} from mob: {}",
                                    itemDrop.itemId, mobConfig.mobId);

                            if (mobIndex < workingMobConfigs.size() &&
                                    itemIndex < workingMobConfigs.get(mobIndex).itemDrops.size()) {
                                String removedItemId = workingMobConfigs.get(mobIndex).itemDrops.get(itemIndex).itemId;
                                workingMobConfigs.get(mobIndex).itemDrops.remove(itemIndex);
                                EvokerDropsMod.LOGGER.info("Removed item: {} from mob: {}, remaining items: {}",
                                        removedItemId, mobConfig.mobId,
                                        workingMobConfigs.get(mobIndex).itemDrops.size());

                                // INSTANT RELOAD dengan AUTO-SAVE
                                MinecraftClient client = MinecraftClient.getInstance();
                                client.execute(() -> {
                                    EvokerDropsMod.LOGGER.info("Reloading config screen after item removal...");
                                    Screen newScreen = buildConfigScreen(parent, config, true); // Auto-save = true
                                    client.setScreen(newScreen);
                                });
                            }
                        },
                        Text.literal("§cClick to remove this item permanently!"),
                        Text.literal("§7Changes are saved automatically")
                ));
            }
        }

        return builder.build();
    }

    private static void addNewMob(String mobId) {
        if (mobId == null || mobId.isEmpty()) return;

        // Check if mob already exists
        for (ModConfig.MobDropConfig existing : workingMobConfigs) {
            if (existing.mobId.equals(mobId)) {
                EvokerDropsMod.LOGGER.warn("Mob {} already exists", mobId);
                return;
            }
        }

        ModConfig.MobDropConfig newMob = new ModConfig.MobDropConfig();
        newMob.mobId = mobId;
        newMob.enabled = true;

        workingMobConfigs.add(newMob);
        EvokerDropsMod.LOGGER.info("Added new mob: {} (no default items)", mobId);
    }

    private static void addNewItem(ModConfig.MobDropConfig mobConfig, String itemId) {
        if (itemId == null || itemId.isEmpty()) return;
        if (mobConfig.itemDrops.size() >= 7) return;

        ModConfig.ItemDropConfig newItem = new ModConfig.ItemDropConfig();
        newItem.itemId = itemId;
        newItem.enabled = true;
        newItem.minCount = 1;
        newItem.maxCount = 1;
        newItem.dropChance = 1.0f;
        newItem.applyLooting = true;
        mobConfig.itemDrops.add(newItem);
    }

    private static String formatMobName(String mobId) {
        if (mobId == null || mobId.isEmpty()) return "Unknown";
        String[] parts = mobId.split(":");
        if (parts.length == 2) {
            return capitalizeWords(parts[1].replace("_", " "));
        }
        return capitalizeWords(mobId.replace("_", " "));
    }

    private static String formatItemName(String itemId) {
        if (itemId == null || itemId.isEmpty()) return "Unknown";
        String[] parts = itemId.split(":");
        if (parts.length == 2) {
            return capitalizeWords(parts[1].replace("_", " "));
        }
        return capitalizeWords(itemId.replace("_", " "));
    }

    private static String getMobName(String mobId) {
        if (mobId == null || mobId.isEmpty()) return "Unknown";
        String[] parts = mobId.split(":");
        if (parts.length == 2) {
            return capitalizeWords(parts[1].replace("_", " "));
        }
        return capitalizeWords(mobId.replace("_", " "));
    }

    private static String getItemName(String itemId) {
        if (itemId == null || itemId.isEmpty()) return "Unknown";
        String[] parts = itemId.split(":");
        if (parts.length == 2) {
            return capitalizeWords(parts[1].replace("_", " "));
        }
        return capitalizeWords(itemId.replace("_", " "));
    }

    private static String capitalizeWords(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    // ========== CUSTOM BUTTON ENTRY CLASS ==========
    /**
     * Custom entry yang menampilkan tombol yang bisa diklik langsung
     * Tidak perlu Save & Quit, langsung eksekusi action
     */
    private static class ButtonEntry extends AbstractConfigListEntry<Object> {
        private final ButtonWidget button;
        private final List<Text> tooltips;
        private final Text fieldName;

        public ButtonEntry(Text buttonText, ButtonWidget.PressAction onPress, Text... tooltips) {
            super(buttonText, false);
            this.fieldName = buttonText;
            this.tooltips = new ArrayList<>();
            for (Text tooltip : tooltips) {
                this.tooltips.add(tooltip);
            }

            this.button = ButtonWidget.builder(buttonText, onPress)
                    .dimensions(0, 0, 200, 20)
                    .build();
        }

        @Override
        public Text getFieldName() {
            return fieldName;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean isHovered, float delta) {
            // Position button di tengah
            button.setX(x + entryWidth / 2 - 100);
            button.setY(y);
            button.setWidth(200);
            button.setHeight(20);
            button.render(context, mouseX, mouseY, delta);

            // Show tooltip jika hover
            if (isHovered && !tooltips.isEmpty() &&
                    mouseX >= button.getX() && mouseX <= button.getX() + button.getWidth() &&
                    mouseY >= button.getY() && mouseY <= button.getY() + button.getHeight()) {

                MinecraftClient client = MinecraftClient.getInstance();
                if (client.currentScreen != null) {
                    context.drawTooltip(client.textRenderer, tooltips, mouseX, mouseY);
                }
            }
        }

        @Override
        public List<? extends Element> children() {
            return List.of(button);
        }

        @Override
        public List<? extends Selectable> narratables() {
            return List.of(button);
        }

        @Override
        public Optional<Object> getDefaultValue() {
            return Optional.empty();
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public void save() {
            // No need to save, button action is immediate
        }
    }
}