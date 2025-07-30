package com.example.stackable;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;

public class StackablePowderSnowBucket implements ModInitializer {
    public static final String MOD_ID = "stackable-powder-snow-bucket";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Stackable Powder Snow Bucket mod...");

        // Hanya fokus pada POWDER_SNOW_BUCKET
        modifyPowderSnowBucketMaxCount();

        LOGGER.info("Stackable Powder Snow Bucket mod initialized!");
    }

    private void modifyPowderSnowBucketMaxCount() {
        try {
            // Akses field maxCount dengan nama yang benar untuk Minecraft 1.21.4
            Field maxCountField = Items.POWDER_SNOW_BUCKET.getClass().getSuperclass().getDeclaredField("maxCount");
            maxCountField.setAccessible(true);
            maxCountField.setInt(Items.POWDER_SNOW_BUCKET, 64);

            LOGGER.info("Successfully modified POWDER_SNOW_BUCKET max count to 64");

        } catch (NoSuchFieldException e) {
            try {
                // Fallback: cari field dengan tipe int yang nilainya 1 (default bucket stack size)
                Field[] fields = Items.POWDER_SNOW_BUCKET.getClass().getSuperclass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType() == int.class) {
                        field.setAccessible(true);
                        int currentValue = field.getInt(Items.POWDER_SNOW_BUCKET);
                        if (currentValue == 1) { // Default stack size untuk bucket
                            field.setInt(Items.POWDER_SNOW_BUCKET, 64);
                            LOGGER.info("Successfully modified POWDER_SNOW_BUCKET max count to 64 via field: {}",
                                    field.getName());
                            break;
                        }
                    }
                }
            } catch (Exception e2) {
                LOGGER.error("Failed to modify POWDER_SNOW_BUCKET max count", e2);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to modify POWDER_SNOW_BUCKET max count", e);
        }
    }
}