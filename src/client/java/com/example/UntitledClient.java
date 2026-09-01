package com.example;

import com.example.Configs.CheatConfig;
import com.example.Configs.Config;
import com.google.common.reflect.TypeToken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.BiFunction;

import static com.example.Constants.*;
import static com.example.DelayedClientState.*;
import static com.example.DelayedPlayerState.BASE_FLY_SPEED;
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
    public static final KeyBinding // TODO -> idk why it crashes when I move these
            SNEAK_TOGGLE = getAbstractPvpUtilsKeybind("Sneak (Toggle)"),
            SNEAK_ENABLE = getAbstractPvpUtilsKeybind("Sneak (Enable)"),
            SNEAK_DISABLE = getAbstractPvpUtilsKeybind("Sneak (Disable)");
    public static final KeyBinding
            SPRINT_TOGGLE = getAbstractPvpUtilsKeybind("Sprint (Toggle)"),
            SPRINT_ENABLE = getAbstractPvpUtilsKeybind("Sprint (Enable)"),
            SPRINT_DISABLE = getAbstractPvpUtilsKeybind("Sprint (Disable)");
    public static final KeyBinding
            MOVEMENT_TOGGLE = getAbstractPvpUtilsKeybind("Movement (Toggle)"),
            MOVEMENT_ENABLE = getAbstractPvpUtilsKeybind("Movement (Enable)"),
            MOVEMENT_DISABLE = getAbstractPvpUtilsKeybind("Movement (Disable)");
    public static final KeyBinding
            FULLBRIGHT_TOGGLE = getAbstractPvpUtilsKeybind("Fullbright (Toggle)"),
            FULLBRIGHT_ENABLE = getAbstractPvpUtilsKeybind("Fullbright (Enable)"),
            FULLBRIGHT_DISABLE = getAbstractPvpUtilsKeybind("Fullbright (Disable)");
    public static final KeyBinding
            FULLBRIGHT_HOLD = getAbstractPvpUtilsKeybind("Fullbright (Hold)");
    public static final KeyBinding
            ALLY_TOGGLE = getAbstractPvpUtilsKeybind("Ally (Toggle)"),
            ENEMY_TOGGLE = getAbstractPvpUtilsKeybind("Enemy (Toggle)"),
            FOCUS_TOGGLE = getAbstractPvpUtilsKeybind("Focus (Toggle)"),
            NAMEPLATE_CYCLE = getAbstractPvpUtilsKeybind("Cycle nameplate type");
    public static final KeyBinding
            HEAD_RUN_CAMERA_OFFSET_TOGGLE = getAbstractPvpUtilsKeybind("Head-run camera offset (Toggle)"),
            HEAD_RUN_CAMERA_OFFSET_HOLD = getAbstractPvpUtilsKeybind("Head-run camera offset (Hold)");
    public static final KeyBinding
            PLAYER_WAYPOINTS_TOGGLE = getAbstractPvpUtilsKeybind("Player waypoints (Toggle)"),
            PLAYER_WAYPOINTS_HOLD = getAbstractPvpUtilsKeybind("Player waypoints (Hold)");
    public static final KeyBinding
            PLAYER_XRAY_TOGGLE = getAbstractPvpUtilsKeybind("Player xray (Toggle)"),
            BLOCK_XRAY_TOGGLE = getAbstractPvpUtilsKeybind("Block xray (Toggle)");
    public static final KeyBinding
            KEYBIND_CONFIG = getAbstractPvpUtilsKeybind("Config");

    private static KeyBinding getAbstractPvpUtilsKeybind(String name) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                name,
                GLFW.GLFW_KEY_UNKNOWN,
                "PvpUtils"
        ));
    }

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

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register((client) -> {
            if (client.player instanceof ClientPlayerEntity player) {
                if (!player.isSprinting())
                    isSprintReset = true;
            }
        });

        {
            final Identifier EXAMPLE_LAYER = Identifier.of("pvputils1", "hud-example-layer");
            HudLayerRegistrationCallback.EVENT.register((wrapper) ->
                    wrapper.attachLayerBefore(
                            IdentifiedLayer.CHAT,
                            EXAMPLE_LAYER,
                            (context, v) -> {
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
                                    if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player) {
                                        boolean isFlying = player.getAbilities().flying;
                                        boolean isSneaking = player.isSneaking();
                                        boolean isSprintingElseDone = player.isSprinting() && !isFlying; // TODO probably can't do both of these anyway
                                        boolean isSneakingElseDone = isSneaking && !isFlying;
                                        if (isSprintingElseDone &&
                                                (config.isSprintEnabled || OPTIONS.getSprintToggled().getValue())) {
                                            if (flag)
                                                stringBuilder.append(", ");
                                            stringBuilder.append("Sprinting");
                                            flag = true;
                                            isSprintingElseDone = false;
                                        }
                                        if (isSneakingElseDone &&
                                                (config.isSneakEnabled || OPTIONS.getSneakToggled().getValue())) {
                                            if (flag)
                                                stringBuilder.append(", ");
                                            stringBuilder.append("Sneaking");
                                            flag = true;
                                            isSneakingElseDone = false;
                                        }
                                        if (flag)
                                            stringBuilder.append(" (Toggled)");
                                        boolean keyHeldFlag = false;
                                        if (isSprintingElseDone && SPRINT_VANILLA.isPressed()) {
                                            if (flag)
                                                stringBuilder.append(", ");
                                            stringBuilder.append("Sprinting");
                                            flag = true;
                                            keyHeldFlag = true;

                                            isSprintingElseDone = false;
                                        }
                                        if (isSneakingElseDone && SNEAK_VANILLA.isPressed()) {
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
                                            if (JUMP_VANILLA.isPressed()) {
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
                                            if (player.getAbilities().getFlySpeed() != BASE_FLY_SPEED)
                                                flyingBuilder.append(" (")
                                                        .append(player.getAbilities().getFlySpeed() / BASE_FLY_SPEED)
                                                        .append("x boost)");
                                            stringBuilder.append(flyingBuilder);

                                            flag = true;
                                        }
                                        stringBuilder.append("]  "); // double space from original mod
                                    }
                                }
                                Window window = MINECRAFT_CLIENT_INSTANCE.getWindow();
                                int width = window.getScaledWidth();
                                if (flag) {
                                    String finalText = stringBuilder.toString();
                                    context.drawTextWithShadow(TEXT_RENDERER,
                                            Text.literal(finalText),
                                            width - TEXT_RENDERER.getWidth(finalText) - 1,
                                            1,
                                            0xffffff);
                                }
                            }));
        }

        {
            final Identifier EXAMPLE_LAYER = Identifier.of("pvputils2", "hud-example-layer");
            final Matrix4f[] projection = new Matrix4f[1];
            WorldRenderEvents.AFTER_ENTITIES.register(context -> {
                projection[0] = new Matrix4f(context.projectionMatrix());
            });
            HudLayerRegistrationCallback.EVENT.register((wrapper) -> {
                wrapper.attachLayerBefore(
                        IdentifiedLayer.CHAT,
                        EXAMPLE_LAYER,
                        (context, renderTickCounter) -> {
                            if (!config.isPlayerWaypointsEnabled && !PLAYER_WAYPOINTS_HOLD.isPressed()) {
                                return;
                            }

                            Camera camera = MINECRAFT_CLIENT_INSTANCE.gameRenderer.getCamera();
                            assert MINECRAFT_CLIENT_INSTANCE.world != null;
                            for (PlayerEntity player : MINECRAFT_CLIENT_INSTANCE.world.getPlayers()) {
                                // TODO -> I think I'd have to raycast each of these if I wanted the visible players to not have them
                                if (!(player instanceof AbstractClientPlayerEntity clientPlayerEntity) || player == MINECRAFT_CLIENT_INSTANCE.player) {
                                    continue;
                                }
                                drawPlayerWaypoint(
                                        player.getLerpedPos(renderTickCounter.getTickDelta(false))
                                                .add(0, player.getHeight() / 2, 0),
                                        camera,
                                        projection[0],
                                        context,
                                        clientPlayerEntity);
                            }
                        });
            });
        }
    }

    private void drawPlayerWaypoint(
            Vec3d worldPos,
            Camera camera,
            Matrix4f projection,
            DrawContext drawContext,
            AbstractClientPlayerEntity player) {
        Vec3d cameraRelativePos = worldPos.subtract(camera.getPos());

        Vector4f clipPos = new Vector4f(
                (float) cameraRelativePos.x,
                (float) cameraRelativePos.y,
                (float) cameraRelativePos.z,
                1.0f // ?
        );
        Quaternionf cameraRotation = new Quaternionf(camera.getRotation());
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
            int screenX = (int) ((ndcX + 1) / 2 * window.getScaledWidth());
            int screenY = (int) ((1 - ndcY) / 2 * window.getScaledHeight());
            int backgroundSize = size + 4;
            drawContext.fill(
                    screenX - backgroundSize / 2,
                    screenY - backgroundSize / 2,
                    screenX + (backgroundSize + 1) / 2,
                    screenY + (backgroundSize + 1) / 2,
                    Objects.equals(config.nameplateUuids.get(player.getUuid()), "ally") ? 0xAF00FF00 : 0xAFFF0000
            );
//            TODO; // config option for only doing teammates
            PlayerSkinDrawer.draw(
                    drawContext,
                    player.getSkinTextures(),
                    screenX - size / 2,
                    screenY - size / 2,
                    size);

            // distance
            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity clientPlayerEntity) {
                double distance = clientPlayerEntity.getPos().distanceTo(worldPos);
                String distanceText = String.format("%.1fm", distance);

                drawText(screenX, distanceText, screenY, size, drawContext);
            }
            // hovered
            {
                Vector3f forward = new Vector3f(0, 0, -1);
                camera.getRotation().transform(forward);
                Vec3d look = new Vec3d(forward.x, forward.y, forward.z).normalize();
                Vec3d toMarker = worldPos.subtract(camera.getPos()).normalize();
                if (look.dotProduct(toMarker) > 0.995) {
                    // TODO -> this could use the supabase username for mod users? + accounts could have nicknames set
                    // TODO -> extra info should also appear when MOUSED over
                    // name
                    {
                        String name = player.getNameForScoreboard();
                        drawText(
                                screenX,
                                name,
                                screenY - size / 2 - TEXT_RENDERER.fontHeight - 2,
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
                                screenY - size / 2 - TEXT_RENDERER.fontHeight * 2 - 4,
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
            int screenX, String text, int screenY, int size, DrawContext drawContext) {
        int textX = screenX - TEXT_RENDERER.getWidth(text) / 2;
        int textY = screenY + size / 2 + 2;
        drawContext.drawTextWithShadow(
                TEXT_RENDERER,
                text,
                textX,
                textY,
                0xFFFFFF
        );
    }
}