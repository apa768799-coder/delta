package com.parallax.delta.ui;

import com.parallax.delta.ParallaxDelta;
import com.parallax.delta.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class ClickGuiScreen extends Screen {
    public ClickGuiScreen() {
        super(Text.literal("Parallax Delta"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        int y = 30;
        for (Module m : ParallaxDelta.MODULES.getModules()) {
            String label = m.getName() + ": " + (m.isEnabled() ? "§aON" : "§cOFF");
            ctx.drawTextWithShadow(this.textRenderer, label, 30, y, 0xFFFFFF);
            y += 14;
        }

        ctx.drawTextWithShadow(this.textRenderer,
                "§7Right Shift — закрыть. Клик по модулю — переключить.",
                30, this.height - 30, 0xAAAAAA);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int y = 30;
        for (Module m : ParallaxDelta.MODULES.getModules()) {
            if (mouseX >= 25 && mouseX <= 220 && mouseY >= y - 2 && mouseY <= y + 12) {
                m.toggle();
                return true;
            }
            y += 14;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
