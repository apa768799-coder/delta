package com.parallax.delta.module.modules;

import com.parallax.delta.module.Category;
import com.parallax.delta.module.Module;
import com.parallax.delta.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class SpeedModule extends Module {
    private final Setting.Mode mode = register(
            new Setting.Mode("Mode", "Strafe", "Vanilla", "Strafe"));
    private final Setting.Num multiplier = register(
            new Setting.Num("Multiplier", 1.35, 1.05, 3.0));
    private final Setting.Bool onGround = register(new Setting.Bool("On Ground Only", true));

    public SpeedModule() {
        super("Speed", Category.MOVEMENT);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        if (onGround.get() && !mc.player.isOnGround()) return;

        Vec3d v = mc.player.getVelocity();
        if (mode.is("Vanilla")) {
            if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;
            if (Math.sqrt(v.x * v.x + v.z * v.z) < 0.01) return;
            mc.player.setVelocity(v.x * multiplier.get(), v.y, v.z * multiplier.get());
        } else {
            applyStrafe(mc);
        }
    }

    private void applyStrafe(MinecraftClient mc) {
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        if (forward == 0 && strafe == 0) return;

        float yawRad = (float) Math.toRadians(mc.player.getYaw());
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);

        double wx = (-sin * forward + cos * strafe);
        double wz = (cos * forward + sin * strafe);

        double len = Math.sqrt(wx * wx + wz * wz);
        if (len < 1e-5) return;

        double base = Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x
                + mc.player.getVelocity().z * mc.player.getVelocity().z);
        if (base < 0.05) base = 0.15;

        double target = base * multiplier.get();

        double nx = wx / len * target;
        double nz = wz / len * target;

        mc.player.setVelocity(nx, mc.player.getVelocity().y, nz);
    }
}
