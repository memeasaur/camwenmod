package com.example.mixins;

import com.example.Configs.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

import static com.example.UntitledClient.config;
import static com.example.Utils.buildReplacementTeamLeatherItemStack;

@Mixin(HumanoidMobRenderer.class)
public class BipedEntityRendererMixin {
    @Inject(at = @At(value = "RETURN"), method = "extractHumanoidRenderState")
    private static void onUpdateBipedRenderState(
            LivingEntity entity,
            HumanoidRenderState state,
            float tickDelta,
            ItemModelResolver itemModelResolver,
            CallbackInfo ci) {
        if (!config.isNameplateIronLeatherSwapped) {
            return;
        }

        if (config.nameplateUuids.get(entity.getUuid()) instanceof Config.NameplateTeam team) {
            // TODO -> only do this if it's a default iron piece
            int color = Objects.requireNonNull(team.color.getColor());
            if (state.headEquipment.is(Items.IRON_HELMET)) {
                state.headEquipment = buildReplacementTeamLeatherItemStack(
                        state.headEquipment, Items.LEATHER_HELMET, color);
            }
            if (state.chestEquipment.is(Items.IRON_CHESTPLATE)) {
                state.chestEquipment = buildReplacementTeamLeatherItemStack(
                        state.chestEquipment, Items.LEATHER_CHESTPLATE, color);
            }
            if (state.legsEquipment.is(Items.IRON_LEGGINGS)) {
                state.legsEquipment = buildReplacementTeamLeatherItemStack(
                        state.legsEquipment, Items.LEATHER_LEGGINGS, color);
            }
            if (state.feetEquipment.is(Items.IRON_BOOTS)) {
                state.feetEquipment = buildReplacementTeamLeatherItemStack(
                        state.feetEquipment, Items.LEATHER_BOOTS, color);
            }
        }
    }
}
