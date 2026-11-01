package com.parallax.delta.ui;

import com.parallax.delta.ParallaxDelta;
import com.parallax.delta.module.Module;
import com.parallax.delta.module.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

public class ClickGuiScreen extends Screen {
    private final Set<Module> expanded = new HashSet<>();
    private Setting.Num dragging = null;
    private Module draggingModule = null;

    private static final int X = 20;
    private static final int Y = 25;
    private static final int WIDTH = 230;
    private static final int ROW = 14;
    private static final int SETTING_ROW = 12;

    public ClickGuiScreen() {
        super(Text.literal("Parallax Delta"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        int y = Y;
        for (Module m : ParallaxDelta.MODULES.getModules()) {
            // заголовок модуля
            int bg = m.isEnabled() ? 0x80207A20 : 0x80202020;
            ctx.fill(X, y, X + WIDTH, y + ROW, bg);
            String label = m.getName()
                    + (expanded.contains(m) ? " ▼" : " ▶")
                    + "   " + (m.isEnabled() ? "§aON" : "§cOFF");
            ctx.drawTextWithShadow(textRenderer, label, X + 5, y + 3, 0xFFFFFFFF);
            y += ROW;

            if (expanded.contains(m)) {
                for (Setting<?> s : m.getSettings()) {
                    if (!s.isVisible()) continue;
                    if (s instanceof Setting.Num n) {
                        drawSlider(ctx, n, y, mouseX, mouseY);
                    } else if (s instanceof Setting.Bool b) {
                        drawBool(ctx, b, y);
                    } else if (s instanceof Setting.Mode mo) {
                        drawMode(ctx, mo, y);
                    }
                    y += SETTING_ROW;
                }
            }
            y += 2;
        }

        ctx.drawTextWithShadow(textRenderer,
                "§7ЛКМ — toggle. ПКМ — раскрыть настройки. Right Shift — закрыть.",
                X, this.height - 20, 0xAAAAAA);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawSlider(DrawContext ctx, Setting.Num n, int y, int mouseX, int mouseY) {
        int barX = X + 10;
        int barY = y + 4;
        int barW = WIDTH - 60;
        int barH = 4;
        ctx.fill(barX, barY, barX + barW, barY + barH, 0xFF333333);

        double range = n.getMax() - n.getMin();
        double pct = (n.get() - n.getMin()) / range;
        int fillW = (int) Math.round(barW * pct);
        ctx.fill(barX, barY, barX + fillW, barY + barH, 0xFF3DAEE9);

        String txt = String.format("%s: %.2f", n.getName(), n.get());
        ctx.drawTextWithShadow(textRenderer, txt, X + 5, y, 0xFFDDDDDD);
        ctx.drawTextWithShadow(textRenderer, "⟵⟶", barX + barW + 4, y, 0xFF888888);
    }

    private void drawBool(DrawContext ctx, Setting.Bool b, int y) {
        String s = (b.get() ? "§a[✔] " : "§c[✘] ") + b.getName();
        ctx.drawTextWithShadow(textRenderer, s, X + 10, y, 0xFFFFFFFF);
    }

    private void drawMode(DrawContext ctx, Setting.Mode mo, int y) {
        String s = mo.getName() + ": §e" + mo.getValue();
        ctx.drawTextWithShadow(textRenderer, s, X + 10, y, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int y = Y;
        for (Module m : ParallaxDelta.MODULES.getModules()) {
            if (inside(mouseX, mouseY, X, y, WIDTH, ROW)) {
                if (button == 0) m.toggle();
                else if (button == 1) {
                    if (!expanded.remove(m)) expanded.add(m);
                }
                return true;
            }
            y += ROW;

            if (expanded.contains(m)) {
                for (Setting<?> s : m.getSettings()) {
                    if (!s.isVisible()) continue;
                    if (inside(mouseX, mouseY, X, y, WIDTH, SETTING_ROW)) {
                        handleSettingClick(s, mouseX);
                        return true;
                    }
                    y += SETTING_ROW;
                }
            }
            y += 2;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleSettingClick(Setting<?> s, double mouseX) {
        if (s instanceof Setting.Num n) {
            int barX = X + 10;
            int barW = WIDTH - 60;
            double pct = Math.max(0.0, Math.min(1.0, (mouseX - barX) / barW));
            n.setValue(n.getMin() + (n.getMax() - n.getMin()) * pct);
        } else if (s instanceof Setting.Bool b) {
            b.setValue(!b.get());
        } else if (s instanceof Setting.Mode mo) {
            mo.cycle();
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dx, double dy) {
        // простой drag для слайдеров через click
        return mouseClicked(mouseX, mouseY, button);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
