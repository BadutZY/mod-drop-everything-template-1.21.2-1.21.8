package com.example.stackable;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class StackableTotem implements ModInitializer {
    public static final String MOD_ID = "stackable-totem";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Map untuk menyimpan item dan max stack count yang diinginkan
    private static final Map<Item, Integer> STACKABLE_ITEMS = new HashMap<>();

    static {
        // RANDOM
        STACKABLE_ITEMS.put(Items.TOTEM_OF_UNDYING, 64);
        STACKABLE_ITEMS.put(Items.ENDER_PEARL, 64);
        STACKABLE_ITEMS.put(Items.EGG, 64);
        STACKABLE_ITEMS.put(Items.SNOWBALL, 64);
        STACKABLE_ITEMS.put(Items.ELYTRA, 16);
        STACKABLE_ITEMS.put(Items.CAKE, 16);

        // MINECART
        STACKABLE_ITEMS.put(Items.MINECART, 16);
        STACKABLE_ITEMS.put(Items.CHEST_MINECART, 16);
        STACKABLE_ITEMS.put(Items.FURNACE_MINECART, 16);
        STACKABLE_ITEMS.put(Items.HOPPER_MINECART, 16);
        STACKABLE_ITEMS.put(Items.TNT_MINECART, 16);
        STACKABLE_ITEMS.put(Items.COMMAND_BLOCK_MINECART, 16);

        // BUCKET
        STACKABLE_ITEMS.put(Items.BUCKET, 64);
        STACKABLE_ITEMS.put(Items.PUFFERFISH_BUCKET, 64);
        STACKABLE_ITEMS.put(Items.AXOLOTL_BUCKET, 64);
        STACKABLE_ITEMS.put(Items.COD_BUCKET, 64);
        STACKABLE_ITEMS.put(Items.SALMON_BUCKET, 64);
        STACKABLE_ITEMS.put(Items.TADPOLE_BUCKET, 64);
        STACKABLE_ITEMS.put(Items.TROPICAL_FISH_BUCKET, 64);
        STACKABLE_ITEMS.put(Items.MILK_BUCKET, 64);
        STACKABLE_ITEMS.put(Items.LAVA_BUCKET, 64);
        STACKABLE_ITEMS.put(Items.WATER_BUCKET, 64);
        STACKABLE_ITEMS.put(Items.POWDER_SNOW_BUCKET, 64);

        //HORSE ARMOR
        STACKABLE_ITEMS.put(Items.SADDLE, 16);
        STACKABLE_ITEMS.put(Items.LEATHER_HORSE_ARMOR, 16);
        STACKABLE_ITEMS.put(Items.IRON_HORSE_ARMOR, 16);
        STACKABLE_ITEMS.put(Items.GOLDEN_HORSE_ARMOR, 16);
        STACKABLE_ITEMS.put(Items.DIAMOND_HORSE_ARMOR, 16);

        //BED
        STACKABLE_ITEMS.put(Items.WHITE_BED, 64);
        STACKABLE_ITEMS.put(Items.GRAY_BED, 64);
        STACKABLE_ITEMS.put(Items.LIGHT_GRAY_BED, 64);
        STACKABLE_ITEMS.put(Items.BLACK_BED, 64);
        STACKABLE_ITEMS.put(Items.BROWN_BED, 64);
        STACKABLE_ITEMS.put(Items.RED_BED, 64);
        STACKABLE_ITEMS.put(Items.ORANGE_BED, 64);
        STACKABLE_ITEMS.put(Items.YELLOW_BED, 64);
        STACKABLE_ITEMS.put(Items.GREEN_BED, 64);
        STACKABLE_ITEMS.put(Items.LIME_BED, 64);
        STACKABLE_ITEMS.put(Items.CYAN_BED, 64);
        STACKABLE_ITEMS.put(Items.BLUE_BED, 64);
        STACKABLE_ITEMS.put(Items.LIGHT_BLUE_BED, 64);
        STACKABLE_ITEMS.put(Items.MAGENTA_BED, 64);
        STACKABLE_ITEMS.put(Items.PINK_BED, 64);
        STACKABLE_ITEMS.put(Items.PURPLE_BED, 64);

        //BANNER
        STACKABLE_ITEMS.put(Items.WHITE_BANNER, 64);
        STACKABLE_ITEMS.put(Items.GRAY_BANNER, 64);
        STACKABLE_ITEMS.put(Items.LIGHT_GRAY_BANNER, 64);
        STACKABLE_ITEMS.put(Items.BLACK_BANNER, 64);
        STACKABLE_ITEMS.put(Items.BROWN_BANNER, 64);
        STACKABLE_ITEMS.put(Items.RED_BANNER, 64);
        STACKABLE_ITEMS.put(Items.ORANGE_BANNER, 64);
        STACKABLE_ITEMS.put(Items.YELLOW_BANNER, 64);
        STACKABLE_ITEMS.put(Items.GREEN_BANNER, 64);
        STACKABLE_ITEMS.put(Items.LIME_BANNER, 64);
        STACKABLE_ITEMS.put(Items.CYAN_BANNER, 64);
        STACKABLE_ITEMS.put(Items.BLUE_BANNER, 64);
        STACKABLE_ITEMS.put(Items.LIGHT_BLUE_BANNER, 64);
        STACKABLE_ITEMS.put(Items.MAGENTA_BANNER, 64);
        STACKABLE_ITEMS.put(Items.PINK_BANNER, 64);
        STACKABLE_ITEMS.put(Items.PURPLE_BANNER, 64);

        //BOAT
        STACKABLE_ITEMS.put(Items.OAK_BOAT, 16);
        STACKABLE_ITEMS.put(Items.SPRUCE_BOAT, 16);
        STACKABLE_ITEMS.put(Items.BIRCH_BOAT, 16);
        STACKABLE_ITEMS.put(Items.JUNGLE_BOAT, 16);
        STACKABLE_ITEMS.put(Items.ACACIA_BOAT, 16);
        STACKABLE_ITEMS.put(Items.DARK_OAK_BOAT, 16);
        STACKABLE_ITEMS.put(Items.MANGROVE_BOAT, 16);
        STACKABLE_ITEMS.put(Items.CHERRY_BOAT, 16);
        STACKABLE_ITEMS.put(Items.PALE_OAK_BOAT, 16);
        STACKABLE_ITEMS.put(Items.BAMBOO_RAFT, 16);

        STACKABLE_ITEMS.put(Items.OAK_CHEST_BOAT, 16);
        STACKABLE_ITEMS.put(Items.SPRUCE_CHEST_BOAT, 16);
        STACKABLE_ITEMS.put(Items.BIRCH_CHEST_BOAT, 16);
        STACKABLE_ITEMS.put(Items.JUNGLE_CHEST_BOAT, 16);
        STACKABLE_ITEMS.put(Items.ACACIA_CHEST_BOAT, 16);
        STACKABLE_ITEMS.put(Items.DARK_OAK_CHEST_BOAT, 16);
        STACKABLE_ITEMS.put(Items.MANGROVE_CHEST_BOAT, 16);
        STACKABLE_ITEMS.put(Items.CHERRY_CHEST_BOAT, 16);
        STACKABLE_ITEMS.put(Items.PALE_OAK_CHEST_BOAT, 16);
        STACKABLE_ITEMS.put(Items.BAMBOO_CHEST_RAFT, 16);

        //POTION
        STACKABLE_ITEMS.put(Items.POTION, 64);
        STACKABLE_ITEMS.put(Items.LINGERING_POTION, 64);
        STACKABLE_ITEMS.put(Items.SPLASH_POTION, 64);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Stackable Items mod...");

        // Modify items immediately during mod initialization
        modifyItemsMaxCount();

        // Also modify during server start to ensure it works in all cases
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            modifyItemsMaxCount();
        });

        LOGGER.info("Stackable Items mod initialized! Modified {} items.", STACKABLE_ITEMS.size());
    }

    // Method untuk mendapatkan map items yang stackable (untuk digunakan di Mixin)
    public static Map<Item, Integer> getStackableItems() {
        return STACKABLE_ITEMS;
    }

    // Method untuk menambah item secara dinamis (opsional)
    public static void addStackableItem(Item item, int maxCount) {
        STACKABLE_ITEMS.put(item, maxCount);
        modifyItemMaxCount(item, maxCount);
    }

    private void modifyItemsMaxCount() {
        for (Map.Entry<Item, Integer> entry : STACKABLE_ITEMS.entrySet()) {
            modifyItemMaxCount(entry.getKey(), entry.getValue());
        }
    }

    private static void modifyItemMaxCount(Item item, int newMaxCount) {
        try {
            // Method 1: Try to access maxCount field directly
            Field maxCountField = Item.class.getDeclaredField("maxCount");
            maxCountField.setAccessible(true);
            maxCountField.setInt(item, newMaxCount);
            LOGGER.info("Successfully modified {} max count to {} via reflection",
                    item.toString(), newMaxCount);

        } catch (NoSuchFieldException e) {
            try {
                // Method 2: Try alternative field names that might be used in mappings
                Field[] fields = Item.class.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType() == int.class) {
                        field.setAccessible(true);
                        int value = field.getInt(item);
                        if (value == 1 || value == 16 || value == 64) { // Likely the maxCount field
                            field.setInt(item, newMaxCount);
                            LOGGER.info("Successfully modified {} max count to {} via field: {}",
                                    item.toString(), newMaxCount, field.getName());
                            break;
                        }
                    }
                }
            } catch (Exception e2) {
                LOGGER.error("Failed to modify {} max count", item.toString(), e2);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to modify {} max count", item.toString(), e);
        }
    }
}