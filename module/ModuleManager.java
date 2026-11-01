package com.parallax.delta.module;

import com.parallax.delta.module.modules.*;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        modules.add(new KillAuraModule());
        modules.add(new PlayerESPModule());
        modules.add(new FlyModule());
        modules.add(new SpeedModule());
        modules.add(new AutoTotemModule());
    }

    public List<Module> getModules() { return modules; }

    public void tick(MinecraftClient client) {
        for (Module m : modules) {
            if (m.isEnabled()) m.onTick(client);
        }
    }
}
