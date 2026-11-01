package com.parallax.delta.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final Category category;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public List<Setting<?>> getSettings() { return settings; }

    protected <S extends Setting<?>> S register(S s) {
        settings.add(s);
        return s;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void setEnabled(boolean state) {
        if (this.enabled != state) {
            this.enabled = state;
            if (state) onEnable(); else onDisable();
        }
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick(MinecraftClient client) {}
}
