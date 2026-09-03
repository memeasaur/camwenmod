package com.example.mixins;

import org.spongepowered.asm.mixin.Mixin;

import static com.example.UntitledClient.config;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;

@Mixin(PotionItem.class)
public class PotionItemMixin extends Item {
    public PotionItemMixin(Properties settings) {
        super(settings);
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        if (config.currentPotionEnchantmentGlintType.equals("1.8")) {
            PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
            return super.isFoil(stack) || (potionContents != null && potionContents.hasEffects());
        } else {
            return false;
        }
    }
}
