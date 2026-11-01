package com.parallax.delta.module.modules;

import com.parallax.delta.module.Category;
import com.parallax.delta.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class FlyModule extends Module {
    public FlyModule() {
        super("Fly", Category.MOVEMENT);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;

        if (client.options.jumpKey.isPressed()) {
            Vec3d v = client.player.getVelocity();
            client.player.setVelocity(v.x, 0.2, v.z);
        } else if (client.player.getVelocity().y > 0) {
            Vec3d v = client.player.getVelocity();
            client.player.setVelocity(v.x, Math.max(0, v.y - 0.05), v.z);
        }
    }
}
