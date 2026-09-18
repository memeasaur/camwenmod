package com.example.mixins;

// codex start
// codex (old code) import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
// codex end
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// codex start
// codex (old code) @Mixin(LocalPlayer.class)
@Mixin(GameRenderer.class)
// codex end
public interface ClientPlayerEntityInvoker {
    @Invoker("pick")
    HitResult invokePick(
            Entity camera,
            double blockInteractionRange,
            double entityInteractionRange,
            float tickDelta);
}
