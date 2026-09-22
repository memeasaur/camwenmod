package com.example.overlayTodoAi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// codex start
/** Predicts vanilla projectile motion locally and draws it in the capture-excluded click-through overlay. */
final class ProjectileTrajectoryPreview {
    private static final int MAX_STEPS = 100;
    private static final Color TRAJECTORY_COLOR = new Color(0xFF55FFFF, true);

    private ProjectileTrajectoryPreview() {
    }

    static boolean draw(Graphics2D graphics, Minecraft client, Function<Vec3, Vector2i> project) {
        if (!(client.player instanceof LocalPlayer player) || client.level == null) return false;
        TrajectoryProperties properties = properties(player);
        if (properties == null) return false;

        List<Vec3> points = simulate(client, player, properties);
        if (points.size() < 2) return false;

        graphics.setColor(TRAJECTORY_COLOR);
        graphics.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Vector2i previous = project.apply(points.getFirst());
        for (int index = 1; index < points.size(); index++) {
            Vector2i current = project.apply(points.get(index));
            graphics.drawLine(previous.x, previous.y, current.x, current.y);
            previous = current;
        }
        graphics.fillOval(previous.x - 2, previous.y - 2, 5, 5);
        return true;
    }

    private static TrajectoryProperties properties(LocalPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.is(Items.BOW)) {
            if (!player.isUsingItem()) return null;
            int usedTicks = Math.max(0, 72_000 - player.getUseItemRemainingTicks());
            float charge = Math.min(1.0f, (usedTicks / 20.0f * (usedTicks / 20.0f) + 2.0f * usedTicks / 20.0f) / 3.0f);
            return charge < 0.1f ? null : new TrajectoryProperties(charge * 3.0, 0.05, 0.99);
        }
        if (held.is(Items.TRIDENT)) {
            return player.isUsingItem() ? new TrajectoryProperties(2.5, 0.05, 0.99) : null;
        }
        if (held.is(Items.CROSSBOW) && CrossbowItem.isCharged(held)) {
            return new TrajectoryProperties(3.15, 0.05, 0.99);
        }
        if (held.is(Items.SPLASH_POTION) || held.is(Items.LINGERING_POTION)
                || held.is(Items.EXPERIENCE_BOTTLE)) {
            return new TrajectoryProperties(0.5, 0.05, 0.99);
        }
        if (held.is(Items.SNOWBALL) || held.is(Items.EGG) || held.is(Items.ENDER_PEARL)) {
            return new TrajectoryProperties(1.5, 0.03, 0.99);
        }
        return null;
    }

    private static List<Vec3> simulate(Minecraft client, LocalPlayer player, TrajectoryProperties properties) {
        List<Vec3> points = new ArrayList<>();
        Vec3 position = player.getEyePosition();
        Vec3 velocity = player.getLookAngle().normalize().scale(properties.speed());
        points.add(position);
        for (int step = 0; step < MAX_STEPS; step++) {
            Vec3 next = position.add(velocity);
            BlockHitResult hit = client.level.clip(new ClipContext(
                    position, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.MISS) {
                points.add(hit.getLocation());
                break;
            }
            points.add(next);
            position = next;
            velocity = velocity.scale(properties.drag()).add(0, -properties.gravity(), 0);
        }
        return points;
    }

    private record TrajectoryProperties(double speed, double gravity, double drag) {
    }
}
// codex end
