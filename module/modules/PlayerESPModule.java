package com.parallax.delta.module.modules;

import com.parallax.delta.module.Category;
import com.parallax.delta.module.Module;
import com.parallax.delta.module.Setting;
import com.parallax.delta.render.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

public class PlayerESPModule extends Module {
    private final Setting.Bool boxes = register(new Setting.Bool("Boxes", true));
    private final Setting.Bool tracers = register(new Setting.Bool("Tracers", false));
    private final Setting.Bool nameTags = register(new Setting.Bool("NameTags", true));
    private final Setting.Bool health = register(new Setting.Bool("Health", true));
    private final Setting.Bool distance = register(new Setting.Bool("Distance", true));
    private final Setting.Num maxDist = register(new Setting.Num("Max Dist", 64, 8, 256));

    private final Setting.Mode entities = register(
            new Setting.Mode("Entities", "All", "Players", "Mobs", "Animals", "All"));

    public PlayerESPModule() {
        super("PlayerESP", Category.RENDER);
        WorldRenderEvents.AFTER_ENTITIES.register(this::onRender);
    }

    private void onRender(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        MatrixStack ms = context.matrixStack();
        Vec3d cam = context.camera().getPos();
        VertexConsumerProvider consumers = context.consumers();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (le == mc.player || !le.isAlive()) continue;
            if (!matchesFilter(le)) continue;
            if (mc.player.distanceTo(le) > maxDist.get()) continue;

            float[] color = colorFor(le);
            Box bb = le.getBoundingBox().offset(-cam.x, -cam.y, -cam.z);

            if (boxes.get()) {
                RenderUtils.drawBox(ms, bb, color[0], color[1], color[2], 1.0f);
            }
            if (tracers.get()) {
                Vec3d center = new Vec3d(
                        (bb.minX + bb.maxX) / 2.0,
                        (bb.minY + bb.maxY) / 2.0,
                        (bb.minZ + bb.maxZ) / 2.0);
                RenderUtils.drawLine(ms, new Vec3d(0, 0, 0), center, color[0], color[1], color[2], 1.0f);
            }
            if (nameTags.get()) {
                drawNameTag(mc, ms, consumers, le, bb, color);
            }
        }
    }

    private boolean matchesFilter(LivingEntity le) {
        String m = entities.getValue();
        if (m.equals("Players")) return le instanceof PlayerEntity;
        if (m.equals("Mobs")) return le instanceof Monster;
        if (m.equals("Animals")) return le instanceof AnimalEntity;
        return true;
    }

    private float[] colorFor(LivingEntity le) {
        if (le instanceof PlayerEntity) return new float[]{0.0f, 1.0f, 0.0f};
        if (le instanceof Monster) return new float[]{1.0f, 0.0f, 0.0f};
        if (le instanceof AnimalEntity) return new float[]{0.5f, 0.8f, 1.0f};
        return new float[]{1.0f, 1.0f, 1.0f};
    }

    private void drawNameTag(MinecraftClient mc, MatrixStack ms,
                             VertexConsumerProvider consumers,
                             LivingEntity le, Box bb, float[] color) {
        if (consumers == null) return;
        TextRenderer tr = mc.textRenderer;

        StringBuilder sb = new StringBuilder(le.getName().getString());
        if (health.get()) {
            sb.append(" §c").append(String.format("%.1f", le.getHealth()));
        }
        if (distance.get()) {
            sb.append(" §7").append(String.format("%.1fm", mc.player.distanceTo(le)));
        }
        Text label = Text.literal(sb.toString());
        int width = tr.getWidth(label);

        double dx = (bb.minX + bb.maxX) / 2.0;
        double dy = bb.maxY + 0.3;
        double dz = (bb.minZ + bb.maxZ) / 2.0;

        ms.push();
        ms.translate(dx, dy, dz);
        ms.multiply(mc.gameRenderer.getCamera().getRotation());
        ms.scale(-0.025f, -0.025f, 0.025f);

        Matrix4f mat = ms.peek().getPositionMatrix();
        tr.draw(label.asOrderedText(), -width / 2.0f, 0, 0xFFFFFFFF,
                false, mat, consumers,
                TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
        ms.pop();
    }

    // Не используется напрямую, оставлен как расширение для будущих фильтров
    @SuppressWarnings("unused")
    private boolean hasLineOfSight(MinecraftClient mc, LivingEntity le) {
        return mc.player.canSee(le);
    }

    @SuppressWarnings("unused")
    private List<LivingEntity> snapshot(MinecraftClient mc) {
        return mc.world.getEntitiesByClass(LivingEntity.class,
                mc.player.getBoundingBox().expand(128), e -> e != mc.player);
    }
}
