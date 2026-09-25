package com.example.overlayTodoAi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import static com.example.UntitledClient.camera;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// codex start
/** Predicts vanilla projectile motion locally and marks its impact in the capture-excluded click-through overlay. */
final class ProjectileTrajectoryPreview {
    private static final int MAX_STEPS = 100;
    private static final Color TRAJECTORY_COLOR = new Color(0xFFFF0000, true);
    // codex start
    private static final Color ENTITY_IMPACT_COLOR = new Color(0xFF00FF00, true);
    //codex end

    private ProjectileTrajectoryPreview() {
    }

    static boolean draw(Graphics2D graphics, Minecraft client, Function<Vec3, Vector2i> project) {
        if (!(client.player instanceof LocalPlayer player) || client.level == null) return false;
        TrajectoryProperties properties = properties(player);
        if (properties == null) return false;

        // codex start
        TrajectoryResult result = simulate(client, player, properties);
        List<Vec3> points = result.points();
        //codex end
        if (points.size() < 2) return false;

        // codex start
        Vec3 impactPosition = points.getLast();
        if (camera == null) return false;
        BlockHitResult visibilityHit = client.level.clip(new ClipContext(
                camera.getPosition(), impactPosition, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
        if (!isImpactVisible(visibilityHit, impactPosition)) return false;
        //codex end
        Vector2i impact = project.apply(impactPosition);
        graphics.setColor(result.hitEntity() ? ENTITY_IMPACT_COLOR : TRAJECTORY_COLOR);
        int size = 2;
        int radius = size / 2;
        graphics.fillOval(impact.x - radius, impact.y - radius, size, size);
        return true;
    }

    // codex start
    static boolean isImpactVisible(BlockHitResult visibilityHit, Vec3 impactPosition) {
        // A hit on the impact surface itself is visible; tolerate sub-millimeter rounding.
        return visibilityHit.getType() == HitResult.Type.MISS
                || visibilityHit.getLocation().distanceToSqr(impactPosition) <= 1.0e-6;
    }
    //codex end
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
//        if (held.is(Items.SPLASH_POTION) || held.is(Items.LINGERING_POTION)
//                || held.is(Items.EXPERIENCE_BOTTLE)) {
//            return new TrajectoryProperties(0.5, 0.05, 0.99);
//        }
        if (held.is(Items.SNOWBALL) || held.is(Items.EGG) || held.is(Items.ENDER_PEARL)) {
            return new TrajectoryProperties(1.5, 0.03, 0.99);
        }
        return null;
    }

    private static TrajectoryResult simulate(Minecraft client, LocalPlayer player, TrajectoryProperties properties) {
        List<Vec3> points = new ArrayList<>();
        Vec3 position = player.getEyePosition();
        Vec3 velocity = player.getLookAngle().normalize().scale(properties.speed());
        points.add(position);
        for (int step = 0; step < MAX_STEPS; step++) {
            Vec3 next = position.add(velocity);
            BlockHitResult hit = client.level.clip(new ClipContext(
                    position, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            // codex start
            Vec3 segmentEnd = hit.getType() == HitResult.Type.MISS ? next : hit.getLocation();
            List<AABB> hitboxes = client.level.getEntities(player,
                            new AABB(position, segmentEnd).inflate(1.0), entity -> !entity.isSpectator() && entity.canBeHitByProjectile())
                    .stream().map(Entity::getBoundingBox).toList();
            Vec3 entityImpact = nearestEntityImpact(position, segmentEnd, hitboxes);
            if (entityImpact != null && (hit.getType() == HitResult.Type.MISS
                    || position.distanceToSqr(entityImpact) < position.distanceToSqr(segmentEnd))) {
                points.add(entityImpact);
                return new TrajectoryResult(points, true);
            }
            //codex end
            if (hit.getType() != HitResult.Type.MISS) {
                points.add(hit.getLocation());
                break;
            }
            points.add(next);
            position = next;
            velocity = velocity.scale(properties.drag()).add(0, -properties.gravity(), 0);
        }
        return new TrajectoryResult(points, false);
    }

    // codex start
    static Vec3 nearestEntityImpact(Vec3 start, Vec3 end, Iterable<AABB> hitboxes) {
        Vec3 nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (AABB hitbox : hitboxes) {
            Vec3 impact = hitbox.contains(start) ? start : hitbox.clip(start, end).orElse(null);
            if (impact != null && start.distanceToSqr(impact) < nearestDistance) {
                nearest = impact;
                nearestDistance = start.distanceToSqr(impact);
            }
        }
        return nearest;
    }

    private record TrajectoryResult(List<Vec3> points, boolean hitEntity) {
    }
    //codex end

    private record TrajectoryProperties(double speed, double gravity, double drag) {
    }
}
// codex end
