package com.parallax.delta.module.modules;

import com.parallax.delta.module.Category;
import com.parallax.delta.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class SpeedModule extends Module {
    public SpeedModule() {
        super("Speed", Category.MOVEMENT);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        if (!client.player.isOnGround() || client.player.input.movementForward == 0) return;

        Vec3d v = client.player.getVelocity();
        client.player.setVelocity(v.x * 1.35, v.y, v.z * 1.35);
    }
}
