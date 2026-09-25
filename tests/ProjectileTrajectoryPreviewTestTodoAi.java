package com.example.overlayTodoAi;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Standalone regression checks using Minecraft's real hitbox intersection code. */
public final class ProjectileTrajectoryPreviewTestTodoAi {
    private static void check(Vec3 expected, Vec3 actual, String scenario) {
        if (expected == null ? actual != null : actual == null || expected.distanceToSqr(actual) > 1e-12) {
            throw new AssertionError(scenario + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        Vec3 start = new Vec3(0, 0, 0);
        Vec3 end = new Vec3(10, 0, 0);
        AABB near = new AABB(2, -1, -1, 3, 1, 1);
        AABB far = new AABB(7, -1, -1, 8, 1, 1);
        check(null, ProjectileTrajectoryPreview.nearestEntityImpact(start, end, List.of()), "empty path");
        check(new Vec3(2, 0, 0), ProjectileTrajectoryPreview.nearestEntityImpact(start, end, List.of(far, near)), "nearest hit regardless of entity order");
        check(null, ProjectileTrajectoryPreview.nearestEntityImpact(start, new Vec3(1, 0, 0), List.of(near)), "entity behind block-clipped endpoint");
        check(new Vec3(2, 0, 0), ProjectileTrajectoryPreview.nearestEntityImpact(start, new Vec3(5, 0, 0), List.of(far, near)), "entity before block");
        check(null, ProjectileTrajectoryPreview.nearestEntityImpact(start, end, List.of(new AABB(2, 2, -1, 3, 3, 1))), "off-path entity");
        check(start, ProjectileTrajectoryPreview.nearestEntityImpact(start, end, List.of(new AABB(-1, -1, -1, 1, 1, 1))), "launch inside hitbox");
        check(new Vec3(3, 0, 0), ProjectileTrajectoryPreview.nearestEntityImpact(end, start, List.of(near)), "reverse direction");
        check(new Vec3(2, 2, 0), ProjectileTrajectoryPreview.nearestEntityImpact(start, new Vec3(5, 5, 0), List.of(new AABB(2, 2, -1, 3, 3, 1))), "diagonal segment");
        if (!ProjectileTrajectoryPreview.isImpactVisible(BlockHitResult.miss(end, Direction.WEST, BlockPos.containing(end)), end))
            throw new AssertionError("Unobstructed impact must be visible");
        if (!ProjectileTrajectoryPreview.isImpactVisible(new BlockHitResult(end, Direction.WEST, BlockPos.containing(end), false), end))
            throw new AssertionError("Impact surface must not hide its own dot");
        if (ProjectileTrajectoryPreview.isImpactVisible(new BlockHitResult(new Vec3(5, 0, 0), Direction.WEST, new BlockPos(5, 0, 0), false), end))
            throw new AssertionError("Terrain before impact must hide the dot");
        if (!ProjectileTrajectoryPreview.isImpactVisible(new BlockHitResult(end.subtract(0.0001, 0, 0), Direction.WEST, BlockPos.containing(end), false), end))
            throw new AssertionError("Surface rounding must not hide the dot");
        System.out.println("PASS: 8 trajectory collision and 4 impact visibility checks");
    }
}