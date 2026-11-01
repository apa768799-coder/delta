package com.parallax.delta.render;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class RenderUtils {
    private RenderUtils() {}

    public static void drawBox(MatrixStack ms, Box box, float r, float g, float b, float a) {
        Matrix4f mat = ms.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        line(buf, mat, x1, y1, z1, x2, y1, z1, r, g, b, a);
        line(buf, mat, x2, y1, z1, x2, y1, z2, r, g, b, a);
        line(buf, mat, x2, y1, z2, x1, y1, z2, r, g, b, a);
        line(buf, mat, x1, y1, z2, x1, y1, z1, r, g, b, a);

        line(buf, mat, x1, y2, z1, x2, y2, z1, r, g, b, a);
        line(buf, mat, x2, y2, z1, x2, y2, z2, r, g, b, a);
        line(buf, mat, x2, y2, z2, x1, y2, z2, r, g, b, a);
        line(buf, mat, x1, y2, z2, x1, y2, z1, r, g, b, a);

        line(buf, mat, x1, y1, z1, x1, y2, z1, r, g, b, a);
        line(buf, mat, x2, y1, z1, x2, y2, z1, r, g, b, a);
        line(buf, mat, x2, y1, z2, x2, y2, z2, r, g, b, a);
        line(buf, mat, x1, y1, z2, x1, y2, z2, r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    public static void drawLine(MatrixStack ms, Vec3d from, Vec3d to,
                                float r, float g, float b, float a) {
        Matrix4f mat = ms.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        line(buf, mat,
                (float) from.x, (float) from.y, (float) from.z,
                (float) to.x, (float) to.y, (float) to.z,
                r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    private static void line(BufferBuilder buf, Matrix4f mat,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
    }
}
