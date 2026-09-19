// codex start
package com.example;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import static com.example.UntitledClient.cameraRenderState;
import static com.example.UntitledClient.config;

/** In-game aim marker for the most recently attacked player; never changes aim or attacks. */
public final class LastHitTarget {
    private static Player target;
    private static ClientLevel targetLevel;

    private LastHitTarget() {}

    public static void onAttack(Player attacker, Entity attacked) {
        Minecraft client = Minecraft.getInstance();
        if (config.isLastHitTargetEnabled && attacker == client.player && !attacker.isSpectator()
                && attacked instanceof Player hitPlayer && hitPlayer != attacker
                && hitPlayer.isAlive() && !hitPlayer.isSpectator()) {
            target = hitPlayer;
            targetLevel = client.level;
        }
    }

    public static void clear() {
        target = null;
        targetLevel = null;
    }

    public static void tick(Minecraft client) {
        if (!config.isLastHitTargetEnabled || client.player == null || !client.player.isAlive()
                || client.player.isSpectator() || client.level != targetLevel
                || (target != null && (target.isRemoved() || !target.isAlive()
                || target.isSpectator()))) {
            clear();
        }
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft client = Minecraft.getInstance();
        tick(client);
        if (target == null || client.level == null || client.player == null
                || cameraRenderState == null || client.gui.hud.isHidden() || client.gui.screen() != null || client.gui.overlay() != null) {
            return;
        }
        // Align the hitbox with the interpolated player model, measuring reach from the local eyes.
        Vec3 offset = target.getPosition(partialTick).subtract(target.position());
        AABB box = target.getBoundingBox().move(offset);
        Vec3 point = closestSurfacePoint(box, client.player.getEyePosition(partialTick));
        Vec3 relative = point.subtract(cameraRenderState.pos);
        Vector4f clip = new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1);
        new Quaternionf(cameraRenderState.orientation).conjugate().transform(clip);
        cameraRenderState.projectionMatrix.transform(clip);
        // Offscreen/behind-camera points must not become misleading edge markers.
        if (!Float.isFinite(clip.w) || clip.w <= 0.00001f) return;
        float x = clip.x / clip.w;
        float y = clip.y / clip.w;
        if (!Float.isFinite(x) || !Float.isFinite(y) || Math.abs(x) > 1 || Math.abs(y) > 1) return;
        int screenX = Math.round((x + 1) * 0.5f * client.getWindow().getGuiScaledWidth());
        int screenY = Math.round((1 - y) * 0.5f * client.getWindow().getGuiScaledHeight());
        // Black outline keeps the red target visible against both bright and dark backgrounds.
        graphics.fill(screenX - 5, screenY - 2, screenX + 6, screenY + 3, 0xFF000000);
        graphics.fill(screenX - 2, screenY - 5, screenX + 3, screenY + 6, 0xFF000000);
        graphics.fill(screenX - 4, screenY - 1, screenX + 5, screenY + 2, 0xFFFF4040);
        graphics.fill(screenX - 1, screenY - 4, screenX + 2, screenY + 5, 0xFFFF4040);
        graphics.fill(screenX, screenY, screenX + 1, screenY + 1, 0xFFFFFFFF);
    }

    static Vec3 closestSurfacePoint(AABB box, Vec3 eye) {
        double x = Math.clamp(eye.x, box.minX, box.maxX);
        double y = Math.clamp(eye.y, box.minY, box.maxY);
        double z = Math.clamp(eye.z, box.minZ, box.maxZ);
        // When overlapping, clamping alone returns an interior point; select the nearest face.
        if (eye.x > box.minX && eye.x < box.maxX && eye.y > box.minY && eye.y < box.maxY
                && eye.z > box.minZ && eye.z < box.maxZ) {
            double[] distances = {x - box.minX, box.maxX - x, y - box.minY,
                    box.maxY - y, z - box.minZ, box.maxZ - z};
            int face = 0;
            for (int i = 1; i < distances.length; i++) {
                if (distances[i] < distances[face]) face = i;
            }
            switch (face) {
                case 0 -> x = box.minX;
                case 1 -> x = box.maxX;
                case 2 -> y = box.minY;
                case 3 -> y = box.maxY;
                case 4 -> z = box.minZ;
                case 5 -> z = box.maxZ;
            }
        }
        return new Vec3(x, y, z);
    }
}
// codex end
