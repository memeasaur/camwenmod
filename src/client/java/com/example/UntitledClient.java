package com.example;

import com.example.Configs.CheatConfig;
import com.example.Configs.Config;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;
import java.util.function.BiFunction;

import static com.example.Constants.*;
import static com.example.DelayedClientState.*;
import static com.example.DelayedPlayerState.BASE_FLY_SPEED;
import static com.example.Utils.getAbstractPvpUtilsKeybind;
import static com.example.Utils.getDeserializedJsonBlocking;

public class UntitledClient implements ClientModInitializer {
    static public Config config = getDeserializedJsonBlocking("config", Config.class) instanceof Config foo
            ? foo
            : new Config();
    public static HashMap<String, CheatConfig> cheatConfigs = getDeserializedJsonBlocking(
            "cheat-configs",
            new TypeToken<HashMap<String, CheatConfig>>() {
            }.getType()) instanceof HashMap<?, ?> map
            ? (HashMap<String, CheatConfig>) map // TODO -> ?
            : new HashMap<>();
    //    TODO; // gl. also, I have to just do this without supabase-kt because fabric(?) is retarded
    // java has a websocket I can use for this apparently
    //        TODO; // task for sending the http payloads of all the shared info
    //        TODO; // register task for drawing waypoints of far away players
    public static final KeyMapping // TODO -> idk why it crashes when I move these
            SNEAK_TOGGLE = getAbstractPvpUtilsKeybind("Sneak (Toggle)"),
            SNEAK_ENABLE = getAbstractPvpUtilsKeybind("Sneak (Enable)"),
            SNEAK_DISABLE = getAbstractPvpUtilsKeybind("Sneak (Disable)");
    public static final KeyMapping
            SPRINT_TOGGLE = getAbstractPvpUtilsKeybind("Sprint (Toggle)"),
            SPRINT_ENABLE = getAbstractPvpUtilsKeybind("Sprint (Enable)"),
            SPRINT_DISABLE = getAbstractPvpUtilsKeybind("Sprint (Disable)");
    public static final KeyMapping
            MOVEMENT_TOGGLE = getAbstractPvpUtilsKeybind("Movement (Toggle)"),
            MOVEMENT_ENABLE = getAbstractPvpUtilsKeybind("Movement (Enable)"),
            MOVEMENT_DISABLE = getAbstractPvpUtilsKeybind("Movement (Disable)");
    public static final KeyMapping
            FULLBRIGHT_TOGGLE = getAbstractPvpUtilsKeybind("Fullbright (Toggle)"),
            FULLBRIGHT_ENABLE = getAbstractPvpUtilsKeybind("Fullbright (Enable)"),
            FULLBRIGHT_DISABLE = getAbstractPvpUtilsKeybind("Fullbright (Disable)");
    public static final KeyMapping
            FULLBRIGHT_HOLD = getAbstractPvpUtilsKeybind("Fullbright (Hold)");
    public static final KeyMapping
            ALLY_TOGGLE = getAbstractPvpUtilsKeybind("Ally (Toggle)"),
            FRIENDLY_TOGGLE = getAbstractPvpUtilsKeybind("Friendly (Toggle)");
    //            ENEMY_TOGGLE = getAbstractPvpUtilsKeybind("Enemy (Toggle)"),
//            FOCUS_TOGGLE = getAbstractPvpUtilsKeybind("Focus (Toggle)"),
    public static final KeyMapping
            HEAD_RUN_CAMERA_OFFSET_TOGGLE = getAbstractPvpUtilsKeybind("Head-run camera offset (Toggle)"),
            HEAD_RUN_CAMERA_OFFSET_HOLD = getAbstractPvpUtilsKeybind("Head-run camera offset (Hold)");
    public static final KeyMapping
            PLAYER_WAYPOINTS_TOGGLE = getAbstractPvpUtilsKeybind("Player waypoints (Toggle)"),
            PLAYER_WAYPOINTS_HOLD = getAbstractPvpUtilsKeybind("Player waypoints (Hold)");
    public static final KeyMapping
            PLAYER_XRAY_TOGGLE = getAbstractPvpUtilsKeybind("Player xray (Toggle)"),
            BLOCK_XRAY_TOGGLE = getAbstractPvpUtilsKeybind("Block xray (Toggle)");
    public static final KeyMapping
            KEYBIND_CONFIG = getAbstractPvpUtilsKeybind("Config");

    public static boolean isDebugModeEnabled = false;
    public static boolean isSprintReset = true;
    public static boolean
            isJumpEnabled,
            isForwardEnabled,
            isLeftEnabled,
            isRightEnabled,
            isBackwardEnabled;

    public static String currentXrayType = "";
    public static Set<Block> immutableXrayBlocks = Set.of(Blocks.STONE, Blocks.DEEPSLATE, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.RED_SAND, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.GRANITE, Blocks.GRAVEL); // TODO -> move all destruct-able state to map // Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.FURNACE, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.ANCIENT_DEBRIS, Blocks.NETHER_GOLD_ORE, Blocks.GOLD_BLOCK, Blocks.RAW_GOLD_BLOCK, Blocks.RAW_IRON_BLOCK, Blocks.RAW_COPPER_BLOCK, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.BOOKSHELF, Blocks.COBWEB)); // TODO -> move all destruct-able state to map
    // TODO -> make immutableXrayBlocks serializable
    @Nullable // TODO -> don't use string literals for this
    public static Map<String, Object> nullableImmutableState = Map.of(
            "SHOULD_DRAW_SIDE_MIXIN", (BiFunction<BlockState, Boolean, Boolean>) (state, original) -> {
                switch (currentXrayType) {
                    case "block" -> {
                        if (immutableXrayBlocks.contains(state.getBlock()))
                            return false;
                    }
                    case "player" -> {
                        return false;
                    }
                }
                return original;
                // TODO -> put outline around the block edges (?)
            });

    public static Matrix4f projectionMatrix;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register((client) -> {
            if (client.player instanceof LocalPlayer player) {
                if (!player.isSprinting())
                    isSprintReset = true;
            }
        });

        {
            final Identifier EXAMPLE_LAYER = Identifier.fromNamespaceAndPath("pvputils1", "hud-example-layer");
            HudElementRegistry.attachElementBefore(
                    VanillaHudElements.CHAT,
                    EXAMPLE_LAYER,
                    (context, _) -> {
                        StringBuilder stringBuilder = new StringBuilder("[");
                        boolean flag = false;
                        if (config.isToggleSneakGuiEnabled) {
                            if (isForwardEnabled) {
                                stringBuilder.append("Forward");
                                flag = true;
                            }
                            flag =
                                    handleGetIsEnabled(isJumpEnabled,
                                            handleGetIsEnabled(isBackwardEnabled,
                                                    handleGetIsEnabled(isRightEnabled,
                                                            handleGetIsEnabled(isLeftEnabled, flag, stringBuilder, "Left"), stringBuilder, "Right"), stringBuilder, "Backwards"), stringBuilder, "Jump");
                            if (MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player) {
                                boolean isFlying = player.getAbilities().flying;
                                boolean isSneaking = player.isShiftKeyDown();
                                boolean isSprintingElseDone = player.isSprinting() && !isFlying; // TODO probably can't do both of these anyway
                                boolean isSneakingElseDone = isSneaking && !isFlying;
                                if (isSprintingElseDone &&
                                        (config.isSprintEnabled || OPTIONS.toggleSprint().get())) {
                                    if (flag)
                                        stringBuilder.append(", ");
                                    stringBuilder.append("Sprinting");
                                    flag = true;
                                    isSprintingElseDone = false;
                                }
                                if (isSneakingElseDone &&
                                        (config.isSneakEnabled || OPTIONS.toggleCrouch().get())) {
                                    if (flag)
                                        stringBuilder.append(", ");
                                    stringBuilder.append("Sneaking");
                                    flag = true;
                                    isSneakingElseDone = false;
                                }
                                if (flag)
                                    stringBuilder.append(" (Toggled)");
                                boolean keyHeldFlag = false;
                                if (isSprintingElseDone && SPRINT_VANILLA.isDown()) {
                                    if (flag)
                                        stringBuilder.append(", ");
                                    stringBuilder.append("Sprinting");
                                    flag = true;
                                    keyHeldFlag = true;

                                    isSprintingElseDone = false;
                                }
                                if (isSneakingElseDone && SNEAK_VANILLA.isDown()) {
                                    if (flag)
                                        stringBuilder.append(", ");
                                    stringBuilder.append("Sneaking");
                                    flag = true;
                                    keyHeldFlag = true;

                                    // TODO -> Sneaking (Vanilla) or (Crouching) from height
                                }
                                if (keyHeldFlag)
                                    stringBuilder.append(" (Key Held)");

                                flag = handleGetIsEnabled(isSprintingElseDone, flag, stringBuilder, "Sprinting (Vanilla)");

                                if (isFlying) {
                                    StringBuilder flyingBuilder = new StringBuilder();
                                    boolean flyingFlag = false;
                                    if (JUMP_VANILLA.isDown()) {
                                        flyingBuilder.append("Ascending");
                                        flyingFlag = true;
                                    }
                                    if (isSneaking) {
                                        if (flyingFlag)
                                            flyingBuilder.append(", ");
                                        flyingBuilder.append("Descending");
                                        flyingFlag = true;
                                    }
                                    if (!flyingFlag)
                                        flyingBuilder.append("Flying");
                                    if (player.getAbilities().getFlyingSpeed() != BASE_FLY_SPEED)
                                        flyingBuilder.append(" (")
                                                .append(player.getAbilities().getFlyingSpeed() / BASE_FLY_SPEED)
                                                .append("x boost)");
                                    stringBuilder.append(flyingBuilder);

                                    flag = true;
                                }
                                stringBuilder.append("]  "); // double space from original mod
                            }
                        }
                        Window window = MINECRAFT_CLIENT_INSTANCE.getWindow();
                        int width = window.getGuiScaledWidth();
                        if (flag) {
                            String finalText = stringBuilder.toString();
                            context.text(TEXT_RENDERER,
                                    finalText,
                                    width - TEXT_RENDERER.width(finalText) - 1,
                                    1,
                                    0xffffff);
                        }
                    });
        }

        {
            final Identifier EXAMPLE_LAYER = Identifier.fromNamespaceAndPath("pvputils2", "hud-example-layer");
//            final Matrix4f[] projection = new Matrix4f[1];
//            LevelRenderEvents.AFTER_SOLID_FEATURES.register(context -> {
//                projection[0] = new Matrix4f(context.projectionMatrix());
//            });
            HudElementRegistry.attachElementBefore(
                    VanillaHudElements.CHAT,
                    EXAMPLE_LAYER,
                    (context, _) -> {
                        if (!config.isPlayerWaypointsEnabled && !PLAYER_WAYPOINTS_HOLD.isDown()) {
                            return;
                        }

                        Camera camera = MINECRAFT_CLIENT_INSTANCE.gameRenderer.mainCamera();
                        assert MINECRAFT_CLIENT_INSTANCE.level != null;
                        for (AbstractClientPlayer player : MINECRAFT_CLIENT_INSTANCE.level.players()) {
                            // TODO -> I think I'd have to raycast each of these if I wanted the visible players to not have them
                            if (player == MINECRAFT_CLIENT_INSTANCE.player) { // !(player instanceof AbstractClientPlayer clientPlayerEntity) ||
                                continue;
                            }
                            drawPlayerWaypoint(
                                    player.position().add(0, player.getBbHeight() / 2, 0),
                                    camera,
                                    projectionMatrix,
                                    context,
                                    player);
                        }
                    });
        }
    }

    private void drawPlayerWaypoint(
            Vec3 worldPos,
            Camera camera,
            Matrix4f projection,
            GuiGraphicsExtractor drawContext,
            AbstractClientPlayer player) {
        Vec3 cameraRelativePos = worldPos.subtract(camera.position());

        Vector4f clipPos = new Vector4f(
                (float) cameraRelativePos.x,
                (float) cameraRelativePos.y,
                (float) cameraRelativePos.z,
                1.0f // ?
        );
        Quaternionf cameraRotation = new Quaternionf(camera.rotation());
        cameraRotation.conjugate().transform(clipPos);
        projection.transform(clipPos);

        float ndcX = clipPos.x() / clipPos.w();
        float ndcY = clipPos.y() / clipPos.w();

        if (clipPos.w() < 0) {
            ndcX = -ndcX;
            ndcY = -ndcY;
            float max = Math.max(Math.abs(ndcX), Math.abs(ndcY));
            if (max > 0.0f) {
                ndcX /= max;
                ndcY /= max;
            }
        }
        ndcX = Math.clamp(ndcX, -1.0f, 1.0f);
        ndcY = Math.clamp(ndcY, -1.0f, 1.0f);

        // player head
        {

            int size = 12;
            Window window = MINECRAFT_CLIENT_INSTANCE.getWindow();
            int screenX = (int) ((ndcX + 1) / 2 * window.getGuiScaledWidth());
            int screenY = (int) ((1 - ndcY) / 2 * window.getGuiScaledHeight());
            int backgroundSize = size + 4;
            drawContext.fill(
                    screenX - backgroundSize / 2,
                    screenY - backgroundSize / 2,
                    screenX + (backgroundSize + 1) / 2,
                    screenY + (backgroundSize + 1) / 2,
                    config.nameplateUuids.get(player.getUUID()) instanceof Config.NameplateTeam team
                            ? 0xFF000000 | team.color.getValue()
                            : 0xAFFF0000
            );
//            TODO; // config option for only doing teammates
            PlayerFaceExtractor.extractRenderState(
                    drawContext,
                    player.getSkin(),
                    screenX - size / 2,
                    screenY - size / 2,
                    size);

            // distance
            if (MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer clientPlayerEntity) {
                double distance = clientPlayerEntity.position().distanceTo(worldPos);
                String distanceText = String.format("%.1fm", distance);

                drawText(screenX, distanceText, screenY, size, drawContext);
            }
            // hovered
            {
                Vector3f forward = new Vector3f(0, 0, -1);
                camera.rotation().transform(forward);
                Vec3 look = new Vec3(forward.x, forward.y, forward.z).normalize();
                Vec3 toMarker = worldPos.subtract(camera.position()).normalize();
                if (look.dot(toMarker) > 0.995) {
                    // TODO -> this could use the supabase username for mod users? + accounts could have nicknames set
                    // TODO -> extra info should also appear when MOUSED over
                    // name
                    {
                        String name = player.getScoreboardName();
                        drawText(
                                screenX,
                                name,
                                screenY - size / 2 - TEXT_RENDERER.lineHeight - 2,
                                size,
                                drawContext);
                    }
                    // coords
                    {
                        String coordinates = String.format(
                                "%.0f, %.0f, %.0f",
                                worldPos.x,
                                worldPos.y,
                                worldPos.z
                        );
                        drawText(
                                screenX,
                                coordinates,
                                screenY - size / 2 - TEXT_RENDERER.lineHeight * 2 - 4,
                                size,
                                drawContext
                        );
                    }
                }
            }
        }
    }

    private static boolean handleGetIsEnabled(
            boolean isEnabled, boolean flag, StringBuilder stringBuilder, String string) {
        if (isEnabled) {
            if (flag)
                stringBuilder.append(", ");
            stringBuilder.append(string);
            return true;
        }
        return flag;
    }

    private static void drawText(
            int screenX, String text, int screenY, int size, GuiGraphicsExtractor drawContext) {
        int textX = screenX - TEXT_RENDERER.width(text) / 2;
        int textY = screenY + size / 2 + 2;
        drawContext.text(
                TEXT_RENDERER,
                text,
                textX,
                textY,
                0xFFFFFF
        );
    }
}