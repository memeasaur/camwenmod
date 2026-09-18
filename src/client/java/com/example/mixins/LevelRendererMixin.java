package com.example.mixins;

import com.example.UntitledClient;
// codex start
// codex (old code) import com.mojang.blaze3d.buffers.GpuBufferSlice;
// codex end
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
// codex start
// codex (old code) import net.minecraft.client.renderer.state.level.CameraRenderState;
// codex (old code) import org.joml.Matrix4fc;
// codex (old code) import org.joml.Vector4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
// codex end
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    // codex start
    // codex (old code) @Inject(method = "render", at = @At("HEAD"))
    @Inject(method = "renderLevel", at = @At("HEAD"))
    // codex end
    private void onRender(
            GraphicsResourceAllocator resourceAllocator,
            DeltaTracker deltaTracker,
            boolean renderOutline,
            // codex start
            // codex (old code) CameraRenderState cameraState,
            // codex (old code) Matrix4fc modelViewMatrix,
            // codex (old code) GpuBufferSlice terrainFog,
            // codex (old code) Vector4f fogColor,
            // codex (old code) boolean shouldRenderSky,
            Camera camera,
            GameRenderer gameRenderer,
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            // codex end
            CallbackInfo ci
    ) {
        // codex start
        // codex (old code) //        UntitledClient.projectionMatrix = new Matrix4f(projectionMatrix);
        // codex (old code) UntitledClient.cameraRenderState = cameraState;
        UntitledClient.projectionMatrix = new Matrix4f(projectionMatrix);
        UntitledClient.camera = camera;
        // codex end
    }
}
