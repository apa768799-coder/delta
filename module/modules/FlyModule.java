package com.parallax.delta.module.modules;

import com.parallax.delta.module.Category;
import com.parallax.delta.module.Module;
import com.parallax.delta.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class FlyModule extends Module {
    private final Setting.Num horizontalSpeed = register(
            new Setting.Num("H-Speed", 0.6, 0.1, 3.0));
    private final Setting.Num verticalSpeed = register(
            new Setting.Num("V-Speed", 0.5, 0.1, 2.0));
    private final Setting.Num accel = register(
            new Setting.Num("Accel", 0.15, 0.02, 1.0));
    private final Setting.Bool glide = register(new Setting.Bool("Glide", true));
    private final Setting.Bool antiFall = register(new Setting.Bool("Anti Fall", true));

    private Vec3d targetVel = Vec3d.ZERO;
    private double fallDistance = 0.0;

    public FlyModule() {
        super("Fly", Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        targetVel = Vec3d.ZERO;
        fallDistance = 0.0;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;

        // направление по вводу
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);
        double wishX = -Math.sin(rad) * forward + Math.cos(rad) * strafe;
        double wishZ = Math.cos(rad) * forward + Math.sin(rad) * strafe;
        double wishLen = Math.sqrt(wishX * wishX + wishZ * wishZ);
        if (wishLen > 1e-4) {
            wishX = wishX / wishLen * horizontalSpeed.get();
            wishZ = wishZ / wishLen * horizontalSpeed.get();
        } else {
            wishX = 0;
            wishZ = 0;
        }

        double vy = 0;
        if (mc.options.jumpKey.isPressed()) vy += verticalSpeed.get();
        if (mc.options.sneakKey.isPressed()) vy -= verticalSpeed.get();

        targetVel = new Vec3d(wishX, vy, wishZ);

        Vec3d current = mc.player.getVelocity();
        double t = accel.get();
        double nx = lerp(current.x, targetVel.x, t);
        double ny = glide.get() ? lerp(current.y, targetVel.y, t) : targetVel.y;
        double nz = lerp(current.z, targetVel.z, t);

        mc.player.setVelocity(nx, ny, nz);

        // отслеживаем накопленное падение до включения / во время выключения
        if (mc.player.getVelocity().y < -0.1) {
            fallDistance += -mc.player.getVelocity().y;
        } else {
            fallDistance = 0;
        }
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * Math.min(1.0, Math.max(0.0, t));
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // обнуляем вертикальную скорость, чтобы не провалиться вниз на сервере
        mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);

        if (antiFall.get() && fallDistance > 3.0 && mc.player.networkHandler != null) {
            // серверу сообщаем onGround=true, чтобы не сработал fall damage
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            fallDistance = 0;
        }
    }
}
