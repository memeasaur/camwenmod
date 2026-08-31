//package com.example.mixins;
//
//import net.minecraft.client.data.Model;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.entity.equipment.EquipmentModel;
//import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.equipment.EquipmentAsset;
//import net.minecraft.registry.RegistryKey;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(EquipmentRenderer.class)
//public class EquipmentRendererMixin {
//    @Inject(
//            method = "render*",
//            at = @At("HEAD")
//    )
//    private void renderArmor(
//            EquipmentModel.LayerType layerType,
//            RegistryKey<EquipmentAsset> asset,
//            Model model,
//            ItemStack stack,
//            MatrixStack matrices,
//            VertexConsumerProvider vertexConsumers,
//            int light,
//            CallbackInfo ci
//    ) {
//        // stack is available here
//    }
//}