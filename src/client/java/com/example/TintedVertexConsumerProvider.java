package com.example;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

public class TintedVertexConsumerProvider implements VertexConsumerProvider {

    private final VertexConsumerProvider parent;

    public TintedVertexConsumerProvider(VertexConsumerProvider parent) {
        this.parent = parent;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        return new TintedVertexConsumer(parent.getBuffer(layer));
    }
}