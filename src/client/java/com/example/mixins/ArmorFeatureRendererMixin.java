package com.example.mixins;

import com.example.TintedArmorFeatureRenderer;
import com.example.TintedVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {
    @ModifyVariable(
            method = "renderArmor",
            at = @At("HEAD"),
            argsOnly = true
    )
    private VertexConsumerProvider onTintArmor(VertexConsumerProvider vertexConsumers) {
        ArmorFeatureRenderer<?, ?, ?> armor = (ArmorFeatureRenderer<?, ?, ?>) (Object) this;
        if (armor instanceof TintedArmorFeatureRenderer) {
            return new TintedVertexConsumerProvider(vertexConsumers);
        }
        return vertexConsumers;
    }
}