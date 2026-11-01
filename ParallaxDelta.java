package com.parallax.delta;

import com.parallax.delta.module.ModuleManager;
import com.parallax.delta.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ParallaxDelta implements ClientModInitializer {
    public static ModuleManager MODULES;
    private static KeyBinding guiKey;

    @Override
    public void onInitializeClient() {
        MODULES = new ModuleManager();
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.parallaxdelta.gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.parallaxdelta"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }
            MODULES.tick(client);
        });
    }
}
