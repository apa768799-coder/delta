package com.parallax.delta.module.modules;

import com.parallax.delta.module.Category;
import com.parallax.delta.module.Module;
import com.parallax.delta.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class KillAuraModule extends Module {
    private final Setting<Double> range = register(new Setting<>("Range", 3.5));
    private final Setting<Integer> cps = register(new Setting<>("CPS", 8));

    private int cooldown = 0;

    public KillAuraModule() {
        super("KillAura", Category.COMBAT);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        if (cooldown > 0) { cooldown--; return; }

        Entity target = findTarget(client);
        if (target == null) return;

        // Поворот к цели (упрощённо, без плавности)
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2.0, 0);
        Vec3d playerPos = client.player.getEyePos();
        Vec3d diff = targetPos.subtract(playerPos);

        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));

        client.player.setYaw(yaw);
        client.player.setPitch(pitch);

        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);

        cooldown = Math.max(1, 20 / cps.getValue());
    }

    private Entity findTarget(MinecraftClient client) {
        Entity closest = null;
        double best = range.getValue();

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity == client.player || !entity.isAlive()) continue;

            double dist = client.player.distanceTo(entity);
            if (dist < best) {
                best = dist;
                closest = entity;
            }
        }
        return closest;
    }
}
