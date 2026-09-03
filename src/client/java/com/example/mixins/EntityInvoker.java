package com.example.mixins;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityInvoker {
    @Invoker("setRot")
    void invokeSetRotation(float yaw, float pitch);
}
