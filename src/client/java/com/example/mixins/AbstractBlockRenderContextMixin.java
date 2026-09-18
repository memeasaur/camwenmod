package com.example.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiFunction;

import static com.example.UntitledClient.nullableImmutableState;

@Mixin(AbstractBlockRenderContext.class)
public class AbstractBlockRenderContextMixin {
    @Shadow
    protected BlockState state;

    // Grass quads have no cull face, so Sodium skips shouldDrawSide for them.
    @ModifyReturnValue(method = "isFaceCulled", at = @At("RETURN"))
    private boolean hideGrass(boolean original) {
        if (nullableImmutableState != null && state != null
                && (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS))) {
            return !((BiFunction<BlockState, Boolean, Boolean>) nullableImmutableState.get("SHOULD_DRAW_SIDE_MIXIN"))
                    .apply(state, !original);
        }
        return original;
    }

    @ModifyReturnValue(method = "shouldDrawSide", at = @At("RETURN"))
    private boolean shouldDrawSide(boolean original) {
        if (nullableImmutableState != null)
            return ((BiFunction<BlockState, Boolean, Boolean>) nullableImmutableState.get("SHOULD_DRAW_SIDE_MIXIN")).apply(state, original);
        else
            return original;
    }
}
