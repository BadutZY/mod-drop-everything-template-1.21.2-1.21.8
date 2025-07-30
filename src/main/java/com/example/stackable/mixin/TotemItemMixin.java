package com.example.stackable.mixin;

import com.example.stackable.StackableTotem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class TotemItemMixin {

    @Shadow
    public abstract int getMaxCount();

    @Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
    private void makeItemsStackable(CallbackInfoReturnable<Integer> cir) {
        Item thisItem = (Item)(Object)this;

        // Check if this item is in our stackable items map
        if (StackableTotem.getStackableItems().containsKey(thisItem)) {
            int newMaxCount = StackableTotem.getStackableItems().get(thisItem);
            cir.setReturnValue(newMaxCount);
        }
    }
}