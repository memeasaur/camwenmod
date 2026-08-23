package com.example;

import com.example.Configs.CheatConfig;
import com.example.Configs.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
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
    static public Config config = getDeserializedJsonBlocking("config", Config.class) instanceof Config foo ? foo : new Config();
    static public CheatConfig cheatConfig = getDeserializedJsonBlocking("cheat-config", CheatConfig.class) instanceof CheatConfig foo ? foo : new CheatConfig();
    @Nullable
    static private SupabaseManager supabaseManager = new SupabaseManager("foo", "bar");
    //    public static final ModMetadata METADATA = FabricLoader.getInstance().getModContainer("untitled").get().getMetadata();
//    private static JsonArray newUpdates;
//    static {
//        new Thread(() -> {
//            try {
//                String response = HTTP_CLIENT.send(HttpRequest.newBuilder()
//                        .uri(URI.create("https://xapkbnegosbyhmondqti.supabase.co/rest/v1/fabricpvputils_updates?version=gt." + METADATA.getVersion() + "&order=version.desc"))
//                        .header("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhhcGtibmVnb3NieWhtb25kcXRpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg3OTgxNTMsImV4cCI6MjA2NDM3NDE1M30.qevIYqIPh3BhiGHj_gppbggv-42RQedaF8Zd-aI5fZA")
//                        .build(), HttpResponse.BodyHandlers.ofString()).body();
//                newUpdates = JsonParser.parseString(response).getAsJsonArray();
//                for (JsonElement element : newUpdates)
//                    if (element.getAsJsonObject().get("is_critical").getAsBoolean())
//                        MinecraftClient.getInstance().execute(() -> {
//                            MINECRAFT_CLIENT_INSTANCE.scheduleStop();
//                            throw new RuntimeException("pvputils missing critical update. force stopping");
//                        });
//            } catch (IOException | InterruptedException e) {
//                if (MinecraftClient.getInstance().player instanceof ClientPlayerEntity player)
//                    MinecraftClient.getInstance().execute(() -> player.sendMessage(Text.literal("updates request failed"), false));
//            }
//        }).start();
//    }
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
            KEYBIND_CONFIG = getAbstractPvpUtilsKeybind("Config");

    private static KeyBinding getAbstractPvpUtilsKeybind(String name) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                name,
                GLFW.GLFW_KEY_UNKNOWN,
                "PvpUtils"
        ));
    }

//    static {
//        try {
//            if (getDeserializedJsonBlocking("nameplates", ) instanceof Map map)
//                nameplateUuids = ((Map<String, String>) map).entrySet().stream()
//                        .collect(Collectors.toMap(entry -> UUID.fromString(entry.getKey()), Map.Entry::getValue));
//            else
//                nameplateUuids = Map.of();
//        } catch (Exception e) {
//            var client = MinecraftClient.getInstance();
//            if (client.player instanceof ClientPlayerEntity player)
//                client.execute(() -> player.sendMessage(Text.literal(e.getMessage()), false));
//            // TODO -> console this
//        }
//    }

//    public static Map<Integer, KeyBinding> duplicateKeybinds; // TODO move to config

//    static {
//        new Thread(() -> {
//            try {
//                Thread.sleep(1000);
//                if (getDeserializedJsonBlocking("duplicateKeybinds", ) instanceof Map map)
//                    duplicateKeybinds = null; //((Map<Integer, Integer>)map);
//                else
//                    duplicateKeybinds = Map.of(); // GLFW.GLFW_KEY_G, USE_VANILLA
//            } catch (Exception e) {
//                var client = MinecraftClient.getInstance();
//                if (client.player instanceof ClientPlayerEntity player)
//                    client.execute(() -> player.sendMessage(Text.literal(e.getMessage()), false));
//                // TODO -> console this
//            }
//        }).start();
//    }

    //    public static final KeyBinding KEYBIND_CONSOLE = getAbstractPvpUtilsKeybind("Console"); TODO
    public static boolean isDebugModeEnabled = false;
    public static boolean isSprintReset = true;
    public static boolean
            isJumpEnabled,
            isForwardEnabled,
            isLeftEnabled,
            isRightEnabled,
            isBackwardEnabled = false;
    public static String lastHitDistance = "";
    public static Text lastHitStrength = EMPTY_TEXT;
    public static int lastHitDisplayTimer = 0;
    public static boolean isYLower;

    // Cheats start
//    static {
//        new Thread(() -> {
//            try {
//                if (getDeserializedJsonBlocking("cheat-config") instanceof Map config) { // TODO -> this shit was a bad idea
//                    isGuiCheatsPvpDisabling = (boolean) config.get("isGuiCheatsPvpDisabling");
//                    if (config.get("immutableRecordedAutoclickerClicks") instanceof List foo && config.get("immutableRecordedAutoclickerMovements") instanceof List bar && foo.size() == bar.size()) {
//                        {
//                            var lists1 = (List<List<Number>>) foo;
//                            int[][] tempClicks = new int[lists1.size()][];
//                            for (int i = 0; i < lists1.size(); i++) {
//                                List<Number> inner = lists1.get(i);
//                                tempClicks[i] = new int[inner.size()]; // TODO ? chatgpt did this
//                                for (int j = 0; j < inner.size(); j++) {
//                                    tempClicks[i][j] = inner.get(j).intValue();
//                                }
//                            }
//                            immutableRecordedAutoclickerClicks = tempClicks;
//                        }
//                        {
//                            var lists2 = (List<List<Map<String, Number>>>) bar;
//                            MouseMovement[][] movements = new MouseMovement[lists2.size()][];
//                            for (int i = 0; i < lists2.size(); i++) {
//                                List<Map<String, Number>> inner = lists2.get(i);
//                                movements[i] = new MouseMovement[inner.size()];
//                                for (int j = 0; j < inner.size(); j++) {
//                                    Map<String, Number> map = inner.get(j);
//                                    movements[i][j] = new MouseMovement(map.get("delayNanos").intValue(), map.get("deltaX").intValue(), map.get("deltaY").intValue());
//                                }
//                            }
//                            immutableRecordedAutoclickerMovements = movements;
//                        }
//                    }
//                    autoclickerStartingMultiplier = ((Number) config.get("autoclickerStartingMultiplier")).floatValue();
//                    autoclickerEndingMultiplier = ((Number) config.get("autoclickerEndingMultiplier")).floatValue();
//                    glfwToggleAutoclickerKeybind = ((Number) config.get("glfwToggleAutoclickerKeybind")).intValue();
//                    glfwEnableAutoclickerKeybind = ((Number) config.get("glfwEnableAutoclickerKeybind")).intValue();
//                    glfwDisableAutoclickerKeybind = ((Number) config.get("glfwDisableAutoclickerKeybind")).intValue();
//                    glfwToggleBlockXrayKeybind = ((Number) config.get("glfwToggleBlockXrayKeybind")).intValue();
//                    glfwTogglePlayerXrayKeybind = ((Number) config.get("glfwTogglePlayerXrayKeybind")).intValue();
//                }
//            } catch (Exception e) {
//                MinecraftClient.getInstance().execute(() -> MINECRAFT_CLIENT_INSTANCE.player.sendMessage(Text.literal(e.getMessage()), false));
//            }
//        }).start();
//    }

    public static boolean isAutoclickerEnabled = false;
    public static boolean isHeldAutoclickerPressed;

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

    public static boolean isRandomDoubleClickEnabled = false;

    @Nullable
    public static PlayerEntity nullableMirrorMovementPlayer = null;
    // Cheats end

    public static boolean isAttackCooldown = false;
    public static float lastAttackCooldownProgress = 0;
    private static double lastEndTickHeight;
//    private static boolean isUpdateNotified = false;

    @Override
    public void onInitializeClient() {
//        ClientPlayConnectionEvents.JOIN.register((clientPlayNetworkHandler, packetSender, v) -> {
//            if (newUpdates != null && !isUpdateNotified && MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player) {
//                for (JsonElement jsonElement : newUpdates)
//                    player.sendMessage(Text.literal("pvputils missed update: " + jsonElement.getAsJsonObject().get("summary").getAsString()), false);
//                isUpdateNotified = true;
//            }
//        });
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            lastHitDisplayTimer++;
        });
//        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
//            // TODO ?
//        });

        ClientTickEvents.START_CLIENT_TICK.register((client) -> {
//            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player) {
//                if (MINECRAFT_CLIENT_INSTANCE.crosshairTarget instanceof BlockHitResult blockHitResult) {
//                    if (MINECRAFT_CLIENT_INSTANCE.world.getBlockState(blockHitResult.getBlockPos()).onUseWithItem())
//                }
//                player.sendMessage(Text.of(String.valueOf(player.isUsingItem())), false);
//            } TODO finish (?)
            if (client.player instanceof ClientPlayerEntity player) {
                if (config.isAttackCooldownNotificationEnabled) {
                    float f = player.getAttackCooldownProgress(.5f);
                    if (f >= 1.0f && isAttackCooldown && lastAttackCooldownProgress != 1.0f) {
                        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), 1, 1);
                        isAttackCooldown = false;
                    } else if (f >= KNOCKBACK_ATTACK_STRENGTH && isAttackCooldown && lastAttackCooldownProgress < KNOCKBACK_ATTACK_STRENGTH)
                        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), .5f, .5f);
                    lastAttackCooldownProgress = f;
                }
                if (!player.isSprinting())
                    isSprintReset = true;

                double currentHeight = player.getY();
                isYLower = currentHeight < lastEndTickHeight;
                lastEndTickHeight = currentHeight;
            }
        });

        // Cheats start TODO ?
        if (isAutoclickerEnabled)
            ATTACK_VANILLA.setPressed(isHeldAutoclickerPressed);
        // Cheats end

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

                                if (config.isAttackIndicatorDataEnabled &&
                                        (!lastHitDistance.isEmpty() || lastHitStrength != EMPTY_TEXT)) {
                                    Text text = Text.literal(lastHitDistance + " ").append(lastHitStrength);
                                    context.drawTextWithShadow(TEXT_RENDERER,
                                            text,
                                            (width - TEXT_RENDERER.getWidth(text)) / 2,
                                            (window.getScaledHeight() - TEXT_RENDERER.fontHeight) / 2 + 20,
                                            0xffffff);
                                    if (lastHitDisplayTimer > 40) {
                                        lastHitDistance = "";
                                        lastHitStrength = EMPTY_TEXT;
                                    }
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
                            if (!cheatConfig.isPlayerWaypointsEnabled) {
                                return;
                            }

                            Camera camera = MINECRAFT_CLIENT_INSTANCE.gameRenderer.getCamera();
                            Vec3d cameraPos = camera.getPos();

                            assert MINECRAFT_CLIENT_INSTANCE.world != null;
                            for (PlayerEntity player : MINECRAFT_CLIENT_INSTANCE.world.getPlayers()) {
//                                Vec3d pos = player.getLerpedPos(tickDelta).add(0, player.getHeight() + 0.5, 0);
                                drawPlayerWaypoint(
                                        player.getLerpedPos(renderTickCounter.getTickDelta(false)),
                                        camera,
                                        projection[0],
                                        context);
                            }
                            if (supabaseManager != null) {
//                                    for (SupabaseManager.VisiblePlayer supabasePlayer : supabaseManager.getPlayers()) {
//                                        drawPlayerWaypoint(supabasePlayer.getTableEntry().getLocationX());
//                                    }
                            }
                        });
            });
        }

//        TODO; // task for sending the http payloads of all the shared info
        //        TODO; // register task for drawing waypoints of far away players
//        RenderLayer WAYPOINT_LAYER = RenderLayer.of(
//                "waypoint",
//                VertexFormats.POSITION_COLOR,
//                VertexFormat.DrawMode.TRIANGLES,
//                256,
//                false, // TODO ?
//                false, // TODO ?
//                RenderLayer.MultiPhaseParameters.builder()
//                        .program(RenderPhase.POSITION_COLOR_PROGRAM)
//                        .depthTest(RenderLayer.ALWAYS_DEPTH_TEST)
//                        .cull(RenderPhase.DISABLE_CULLING)
//                        .writeMaskState(RenderPhase.COLOR_MASK)
//                        .build(false)); // TODO ?
    }

    private void drawPlayerWaypoint(
            Vec3d pos, Camera camera, Matrix4f projection, DrawContext drawContext) {
        Vec3d relative = pos.subtract(camera.getPos());

        Vector4f clipPos = new Vector4f(
                (float) relative.x,
                (float) relative.y,
                (float) relative.z,
                1.0f // ?
        );
        camera.getRotation().transform(clipPos);
        projection.transform(clipPos);

        float x = clipPos.x() / clipPos.w();
        float y = clipPos.y() / clipPos.w();
//        Vec2f screenPos = new Vec2f(x, y);

//        matrices.push();
//        matrices.translate(
//                pos.x - cameraPos.x,
//                pos.y - cameraPos.y,
//                pos.z - cameraPos.z);
//        matrices.multiply(camera.getRotation());
        // diamond TODO -> player heads
        {
//            matrices.push();
//            float size = 0.25f;
//            matrices.scale(size, size, size);
//            assert context.consumers() != null;
//            VertexConsumer Foo = context.consumers().getBuffer(WAYPOINT_LAYER);
//            Vector3f Top = new Vector3f(0, 1, 0);
//            Vector3f Bottom = new Vector3f(0, -1, 0);
//            Vector3f Left = new Vector3f(-1, 0, 0);
//            Vector3f Right = new Vector3f(1, 0, 0);
//            MatrixStack.Entry entry = matrices.peek();
//            Foo.vertex(entry, Top).color(255, 0, 0, 175);
//            Foo.vertex(entry, Left).color(255, 0, 0, 175);
//            Foo.vertex(entry, Bottom).color(255, 0, 0, 175);
//            Foo.vertex(entry, Top).color(255, 0, 0, 175);
//            Foo.vertex(entry, Right).color(255, 0, 0, 175);
//            Foo.vertex(entry, Bottom).color(255, 0, 0, 175);

            int size = 6;
            int intX = (int) x;
            int intY = (int) y;
            drawContext.fill(
                    intX - 1,
                    intY - size,
                    intX + 2,
                    intY + size + 1,
                    0xAFFF0000
            );

            drawContext.fill(
                    intX - size,
                    intY - 1,
                    intX + size + 1,
                    intY + 2,
                    0xAFFF0000
            );

            {
                Vector3f forward = new Vector3f(0, 0, -1);
                camera.getRotation().transform(forward);
                Vec3d look = new Vec3d(forward.x, forward.y, forward.z).normalize();
                Vec3d toMarker = pos.subtract(camera.getPos()).normalize();
                if (look.dotProduct(toMarker) > 0.995) {
//                        TODO; // if targawetted? names etc. should be drawn, distances should be drawn
                }
            }
//            matrices.pop();
        }
        // distance
        {
//            var textRenderer = MINECRAFT_CLIENT_INSTANCE.textRenderer;
//            matrices.push();
//            matrices.scale(0.025f, -0.025f, 0.025f);
//            String text = String.format("%.1fm", cameraPos.distanceTo(pos));
//            float x = -textRenderer.getWidth(text) / 2.0f;
//            textRenderer.draw(
//                    Text.literal(text),
//                    x,
//                    0,
//                    0xFFFFFFFF,
//                    false,
//                    matrices.peek().getPositionMatrix(),
//                    context.consumers(),
//                    TextRenderer.TextLayerType.SEE_THROUGH,
//                    0,
//                    0xF000F0
//            );
//            matrices.pop();
        }
        // TODO name
        {
            // TODO -> this could use the supabase username for mod users? + accounts could have nicknames set
//                    TODO; -> should only appear when looked at
            // TODO -> extra info should also appear when moused over
        }
//        matrices.pop();
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
}