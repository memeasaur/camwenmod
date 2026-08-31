package com.example.mixins;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static com.example.UntitledClient.config;

@Mixin(BipedEntityRenderer.class)
public class BipedEntityRendererMixin {
    @Inject(at = @At(value = "RETURN"), method = "updateBipedRenderState")
    private static void onUpdateBipedRenderState(
            LivingEntity entity,
            BipedEntityRenderState state,
            float tickDelta,
            ItemModelManager itemModelResolver,
            CallbackInfo ci) {
        if (Objects.equals(config.nameplateUuids.get(entity.getUuid()), "ally")); {
            // TODO -> maintain the enchantment
            // TODO -> only do this if it's a default iron piece
            if (state.equippedHeadStack.isOf(Items.IRON_HELMET)) {
                state.equippedHeadStack = Items.DIAMOND_HELMET.getDefaultStack();
            }
            if (state.equippedHeadStack.isOf(Items.IRON_CHESTPLATE)) {
                state.equippedChestStack = Items.DIAMOND_CHESTPLATE.getDefaultStack();
            }
            if (state.equippedLegsStack.isOf(Items.IRON_LEGGINGS)) {
                state.equippedLegsStack = Items.DIAMOND_LEGGINGS.getDefaultStack();
            }
            if (state.equippedFeetStack.isOf(Items.IRON_BOOTS)) {
                state.equippedFeetStack = Items.DIAMOND_BOOTS.getDefaultStack();
            }
        }
    }
}
