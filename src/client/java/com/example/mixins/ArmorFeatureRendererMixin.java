//package com.example.mixins;
//
//import com.example.TintedVertexConsumerProvider;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.ModifyVariable;
//
//@Mixin(ArmorFeatureRenderer.class)
//public class ArmorFeatureRendererMixin {
////    @ModifyArg(method = "renderArmor", at = @At(
////            value = "INVOKE",
////            target = "Lnet/minecraft/client/render/entity/equipment/EquipmentRenderer;render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"),
////            index = 5)
////    private VertexConsumerProvider onTintArmor(VertexConsumerProvider original) {
////        return new TintedVertexConsumerProvider(original);
////    }
//    @ModifyVariable(
//            method = "renderArmor",
//            at = @At("HEAD"),
//            argsOnly = true
//    )
//    private VertexConsumerProvider onTintArmor(VertexConsumerProvider vertexConsumers) {
//        return new TintedVertexConsumerProvider(vertexConsumers);
//    }
//}