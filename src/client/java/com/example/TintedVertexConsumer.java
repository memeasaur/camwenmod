package com.example;

import net.minecraft.client.render.VertexConsumer;

public class TintedVertexConsumer implements VertexConsumer {

    private final VertexConsumer parent;

    public TintedVertexConsumer(VertexConsumer parent) {
        this.parent = parent;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        parent.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        parent.color(r * 80 / 255, g, b * 80 / 255, a);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        parent.texture(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        parent.overlay(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        parent.light(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        parent.normal(x, y, z);
        return this;
    }
}