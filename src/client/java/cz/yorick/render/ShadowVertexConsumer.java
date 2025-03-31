package cz.yorick.render;

import net.minecraft.client.render.VertexConsumer;

public record ShadowVertexConsumer(VertexConsumer delegate) implements VertexConsumer {
    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        return this.delegate.vertex(x, y, z);
    }

    //wraps the color to make it transparent
    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this.delegate.color(red, green, blue, alpha/2);
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this.delegate.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this.delegate.overlay(u, v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this.delegate.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this.delegate.normal(x, y, z);
    }
}
