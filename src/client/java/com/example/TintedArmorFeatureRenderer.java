package com.example;

import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;

public class TintedArmorFeatureRenderer
        extends ArmorFeatureRenderer<
        PlayerEntityRenderState,
        PlayerEntityModel,
        ArmorEntityModel<PlayerEntityRenderState>
        > {

    public TintedArmorFeatureRenderer(
            FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context,
            ArmorEntityModel<PlayerEntityRenderState> innerModel,
            ArmorEntityModel<PlayerEntityRenderState> outerModel,
            EquipmentRenderer equipmentRenderer
    ) {
        super(context, innerModel, outerModel, equipmentRenderer);
    }
}
