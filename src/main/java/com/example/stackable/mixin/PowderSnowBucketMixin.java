package com.example.stackable.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PowderSnowBucketItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBucketItem.class)
public class PowderSnowBucketMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void fixStackedPowderSnowBucketUsage(net.minecraft.item.ItemUsageContext context,
                                                 CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (player == null) {
            return;
        }

        // Hanya proses jika item ini adalah POWDER_SNOW_BUCKET dan stacknya lebih dari 1
        if (stack.getItem() == Items.POWDER_SNOW_BUCKET && stack.getCount() > 1) {
            BlockPos targetPos = pos.offset(context.getSide());

            // Cek apakah posisi valid untuk menempatkan powder snow
            if (world.getBlockState(targetPos).isAir() || world.getBlockState(targetPos).isReplaceable()) {
                if (!world.isClient) {
                    // Tempatkan powder snow block
                    world.setBlockState(targetPos, Blocks.POWDER_SNOW.getDefaultState());

                    // Play sound
                    world.playSound(null, targetPos, SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                // Hanya kurangi stack dan berikan bucket kosong jika player TIDAK dalam mode creative
                if (!player.getAbilities().creativeMode) {
                    // Kurangi stack count sebanyak 1 saja
                    stack.decrement(1);

                    // Berikan empty bucket ke player (akan stack otomatis)
                    ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                    if (!player.getInventory().insertStack(emptyBucket)) {
                        player.dropItem(emptyBucket, false);
                    }
                }

                cir.setReturnValue(ActionResult.SUCCESS);
            } else {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}