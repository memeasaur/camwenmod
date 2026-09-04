package com.example.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiFunction;

import static com.example.UntitledClient.nullableImmutableState;

// TODO -> update
//@Mixin(BlockOcclusionCache.class)
//public class SodiumBlockOcclusionCacheMixin {
//    @ModifyReturnValue(method = "shouldDrawSide", at = @At("RETURN"))
//    private boolean shouldDrawSide(boolean original, BlockState state, BlockGetter view, BlockPos pos, Direction facing) {
//        if (nullableImmutableState != null)
//            return ((BiFunction<BlockState, Boolean, Boolean>) nullableImmutableState.get("SHOULD_DRAW_SIDE_MIXIN")).apply(state, original);
//        else
//            return original;
//    }
//}
