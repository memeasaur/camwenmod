package com.example.mixins;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;

import static com.example.UntitledClient.config;

@Mixin(PotionItem.class)
public class PotionItemMixin extends Item {
    public PotionItemMixin(Settings settings) {
        super(settings);
    }
    @Override
    public boolean hasGlint(ItemStack stack) {
        if (config.currentPotionEnchantmentGlintType.equals("1.8")) {
            PotionContentsComponent potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
            return super.hasGlint(stack) || (potionContents != null && potionContents.hasEffects());
        } else {
            return false;
        }
    }
}
