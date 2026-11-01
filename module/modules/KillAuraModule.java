package com.parallax.delta.module.modules;

import com.parallax.delta.module.Category;
import com.parallax.delta.module.Module;
import com.parallax.delta.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAuraModule extends Module {
    private final Setting.Num range = register(new Setting.Num("Range", 3.4, 2.0, 6.0));
    private final Setting.Num cpsMin = register(new Setting.Num("CPS Min", 8, 1, 20));
    private final Setting.Num cpsMax = register(new Setting.Num("CPS Max", 12, 1, 20));
    private final Setting.Num fov = register(new Setting.Num("FOV", 180, 30, 360));
    private final Setting.Num rotSpeed = register(new Setting.Num("Rot Speed", 30, 5, 180));
    private final Setting.Num minHp = register(new Setting.Num("Min HP", 0, 0, 20));
    private final Setting.Mode targetMode = register(
            new Setting.Mode("Target", "Players", "Players", "Mobs", "Animals", "All"));
    private final Setting.Mode rotMode = register(
            new Setting.Mode("Rotation", "Silent", "Silent", "Legit", "None"));
    private final Setting.Bool onlyVisible = register(new Setting.Bool("Only Visible", true));
    private final Setting.Bool swing = register(new Setting.Bool("Swing", true));
    private final Setting.Bool multi = register(new Setting.Bool("Multi", false));
    private final Setting.Bool strictAim = register(new Setting.Bool("Strict Aim", true));
    private final Setting.Num maxTargets = register(
            new Setting.Num("Max Targets", 3, 1, 8).visibleWhen(multi::get));

    private int cooldown = 0;

    public KillAuraModule() {
        super("KillAura", Category.COMBAT);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (cooldown > 0) { cooldown--; return; }

        List<LivingEntity> targets = findTargets(mc);
        if (targets.isEmpty()) return;

        LivingEntity primary = targets.get(0);
        if (rotMode.is("Silent") || rotMode.is("Legit")) {
            applyRotation(mc, primary, rotMode.is("Silent"));
        }

        if (strictAim.get() && !aligned(mc, primary)) return;

        int attacked = 0;
        int limit = multi.get() ? maxTargets.getInt() : 1;
        for (LivingEntity target : targets) {
            if (attacked >= limit) break;
            mc.interactionManager.attackEntity(mc.player, target);
            if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
            attacked++;
        }

        if (attacked > 0) cooldown = nextDelay();
    }

    private int nextDelay() {
        double lo = Math.min(cpsMin.get(), cpsMax.get());
        double hi = Math.max(cpsMin.get(), cpsMax.get());
        double cps = lo + Math.random() * (hi - lo);
        return (int) Math.max(1, Math.round(20.0 / cps));
    }

    private List<LivingEntity> findTargets(MinecraftClient mc) {
        List<LivingEntity> candidates = new ArrayList<>();
        Vec3d eye = mc.player.getEyePos();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (!isValidTarget(mc, le)) continue;

            double dist = mc.player.distanceTo(le);
            if (dist > range.get()) continue;

            double angle = angleTo(eye, le);
            if (angle > fov.get() / 2.0) continue;

            if (le.getHealth() < minHp.get()) continue;
            if (onlyVisible.get() && !isVisible(mc, le)) continue;

            candidates.add(le);
        }

        candidates.sort(Comparator.comparingDouble(le -> score(mc, le)));
        return candidates;
    }

    private double score(MinecraftClient mc, LivingEntity le) {
        double dist = mc.player.distanceTo(le);
        double angle = angleTo(mc.player.getEyePos(), le);
        double hpBonus = (20.0 - Math.min(le.getHealth(), 20.0)) * 0.05;
        return dist * 0.7 + angle * 0.3 - hpBonus;
    }

    private boolean isValidTarget(MinecraftClient mc, LivingEntity le) {
        if (le == mc.player || !le.isAlive() || le.isRemoved() || le.isSpectator()) return false;

        if (le instanceof PlayerEntity) {
            return targetMode.is("Players") || targetMode.is("All");
        }
        if (le instanceof Monster) {
            return targetMode.is("Mobs") || targetMode.is("All");
        }
        if (le instanceof AnimalEntity) {
            return targetMode.is("Animals") || targetMode.is("All");
        }
        return targetMode.is("All");
    }

    private double angleTo(Vec3d eye, LivingEntity le) {
        Vec3d tp = le.getPos().add(0, le.getHeight() * 0.5, 0);
        Vec3d d = tp.subtract(eye);
        double horiz = Math.sqrt(d.x * d.x + d.z * d.z);
        double yaw = Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0;
        double pitch = -Math.toDegrees(Math.atan2(d.y, horiz));
        // расстояние угловое по сфере, грубая аппроксимация
        return Math.sqrt(yaw * yaw + pitch * pitch) * 0.5;
    }

    private boolean isVisible(MinecraftClient mc, LivingEntity target) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d p = target.getPos().add(0, target.getHeight() * 0.6, 0);
        BlockHitResult hit = mc.world.raycast(new RaycastContext(
                eye, p, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        return hit.getType() == HitResult.Type.MISS;
    }

    private void applyRotation(MinecraftClient mc, LivingEntity target, boolean silent) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d tp = target.getPos().add(0, target.getHeight() * 0.6, 0);
        Vec3d d = tp.subtract(eye);

        float targetYaw = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
        float targetPitch = (float) -Math.toDegrees(
                Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));

        float speed = rotSpeed.getFloat();
        float newYaw = smooth(mc.player.getYaw(), targetYaw, speed);
        float newPitch = smooth(mc.player.getPitch(), targetPitch, speed);

        // "Silent" в этой реализации всё равно меняет клиентский yaw/pitch:
        // полноценный silent требует перехвата PlayerMoveC2SPacket.LookAndOnGround через миксин.
        // Здесь мы понижаем визуальную скорость, чтобы не дёргалось на экране.
        if (silent) {
            mc.player.setYaw(newYaw);
            mc.player.setPitch(newPitch);
        } else {
            mc.player.setYaw(smooth(mc.player.getYaw(), targetYaw, speed * 0.5f));
            mc.player.setPitch(smooth(mc.player.getPitch(), targetPitch, speed * 0.5f));
        }
    }

    private float smooth(float current, float target, float maxDelta) {
        float delta = MathHelper.wrapDegrees(target - current);
        if (Math.abs(delta) <= maxDelta) return target;
        return current + Math.signum(delta) * maxDelta;
    }

    private boolean aligned(MinecraftClient mc, LivingEntity target) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d tp = target.getPos().add(0, target.getHeight() * 0.5, 0);
        Vec3d look = mc.player.getRotationVec(1.0f);
        Vec3d dir = tp.subtract(eye).normalize();
        // cos(25°) ≈ 0.906
        return look.dotProduct(dir) > 0.9;
    }

    @Override
    protected void onDisable() {
        cooldown = 0;
    }
}
