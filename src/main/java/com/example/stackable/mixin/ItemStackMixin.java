package com.example.stackable.mixin;

import com.example.stackable.StackableTotem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void makeItemsStackableInStack(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack)(Object)this;

        // Check if this item is in our stackable items map
        if (StackableTotem.getStackableItems().containsKey(stack.getItem())) {
            int newMaxCount = StackableTotem.getStackableItems().get(stack.getItem());
            cir.setReturnValue(newMaxCount);
        }
    }

    @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
    private void makeItemsStackableCheck(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack)(Object)this;

        // Check if this item is in our stackable items map
        if (StackableTotem.getStackableItems().containsKey(stack.getItem())) {
            cir.setReturnValue(true);
        }
    }
}