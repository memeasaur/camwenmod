package com.example.mixins;

import com.example.Configs.Config;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static com.example.UntitledClient.config;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
//    @Inject(at = @At(value = "RETURN"), method = "getArmPose(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/util/Arm;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;", cancellable = true)
//    private static void onGetArmPose(AbstractClientPlayerEntity player, Arm arm, CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
//        // TODO
//    }
//    @Inject(at = @At(value = "RETURN"), method = "getArmPose(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;", cancellable = true)
//    private static void onGetArmPose(PlayerEntity player, ItemStack stack, Hand hand, CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
//        // TODO -> disable other player's being left-handed
//    }

    @Inject(at = @At(value = "RETURN"), method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V")
    private void onUpdateRenderState(
            AbstractClientPlayerEntity abstractClientPlayerEntity,
            PlayerEntityRenderState playerEntityRenderState,
            float f,
            CallbackInfo ci) {

        if (playerEntityRenderState.displayName instanceof Text text &&
                config.nameplateUuids.get(abstractClientPlayerEntity.getUuid()) instanceof Config.NameplateTeam team) {
            playerEntityRenderState.displayName = text.copy().setStyle(text.getStyle().withColor(Objects.requireNonNull(team.color.getColorValue())));
        }
    }

//    @Inject(method = "<init>", at = @At("TAIL"))
//    private void replaceArmorFeature(
//            EntityRendererFactory.Context ctx,
//            boolean slim,
//            CallbackInfo ci
//    ) {
//        if (config.nameplateUuids.get(abstractClient)) {
//            return;
//        }
//
//        List<FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel>> features = ((LivingEntityRendererAccessor<PlayerEntityRenderState, PlayerEntityModel>) (Object) this).getFeatures();
//        features.removeIf(feature ->
//                feature instanceof ArmorFeatureRenderer<?, ?, ?>
//        );
//        // TODO ?
//        features.add(
//                new TintedArmorFeatureRenderer(
//                        (FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>) this,
//                        new ArmorEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM_INNER_ARMOR : EntityModelLayers.PLAYER_INNER_ARMOR)),
//                        new ArmorEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR : EntityModelLayers.PLAYER_OUTER_ARMOR)),
//                        ctx.getEquipmentRenderer()
//                )
//        );
//    }
}
