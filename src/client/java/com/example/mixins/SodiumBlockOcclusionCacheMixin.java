package com.example.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiFunction;

import static com.example.UntitledClient.nullableImmutableState;

@Mixin(BlockOcclusionCache.class)
public class SodiumBlockOcclusionCacheMixin {
    @ModifyReturnValue(method = "shouldDrawSide", at = @At("RETURN"))
    private boolean shouldDrawSide(boolean original, BlockState state, BlockView view, BlockPos pos, Direction facing) {
        if (nullableImmutableState != null)
            return ((BiFunction<BlockState, Boolean, Boolean>) nullableImmutableState.get("SHOULD_DRAW_SIDE_MIXIN")).apply(state, original);
        else
            return original;
    }
}
