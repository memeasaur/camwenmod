//package com.example.mixins;
//
//import net.minecraft.client.render.RenderLayer;
//import net.minecraft.client.render.VertexConsumer;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//
//@Mixin(EquipmentRenderer.class)
//public class EquipmentRendererMixin {
//
//    @Redirect(
//            method = "render",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"
//            )
//    )
//    private VertexConsumer pvputils$tintArmor(
//            VertexConsumerProvider provider,
//            RenderLayer layer
//    ) {
//        VertexConsumer original = provider.getBuffer(layer);
//
//        return new VertexConsumer() {
//
//            @Override
//            public VertexConsumer vertex(float x, float y, float z) {
//                original.vertex(x, y, z);
//                return this;
//            }
//
//            @Override
//            public VertexConsumer color(int r, int g, int b, int a) {
//                original.color(
//                        (int) (r * 0.4f),
//                        g,
//                        (int) (b * 0.4f),
//                        a
//                );
//                return this;
//            }
//
//            @Override
//            public VertexConsumer texture(float u, float v) {
//                original.texture(u, v);
//                return this;
//            }
//
//            @Override
//            public VertexConsumer overlay(int u, int v) {
//                original.overlay(u, v);
//                return this;
//            }
//
//            @Override
//            public VertexConsumer light(int u, int v) {
//                original.light(u, v);
//                return this;
//            }
//
//            @Override
//            public VertexConsumer normal(float x, float y, float z) {
//                original.normal(x, y, z);
//                return this;
//            }
//        };
//    }
//}