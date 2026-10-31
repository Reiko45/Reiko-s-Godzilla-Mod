package com.reiko45.godzilla.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.reiko45.godzilla.entity.BeamEmitter;
import com.reiko45.godzilla.entity.BeamStyle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.Color;

/** Draws glowing beams (additive "lightning" render type): a bright core inside a wider coloured glow. */
public final class BeamRenderer {
    private static final int SIDES = 8;

    private BeamRenderer() {}

    public static <T extends Entity & BeamEmitter> void renderBeams(T entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        int count = entity.getActiveBeamCount();
        if (count <= 0) return;
        Vec3 base = entity.getPosition(partialTick);
        BeamStyle style = entity.getBeamStyle();
        float width = entity.getBeamWidth();
        float time = entity.tickCount + partialTick;
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());

        float r = style.r, g = style.g, b = style.b;
        if (style.rainbow) {
            Color c = Color.getHSBColor((time * 0.03f) % 1.0f, 0.8f, 1.0f);
            r = c.getRed() / 255f;
            g = c.getGreen() / 255f;
            b = c.getBlue() / 255f;
        }
        float pulse = 0.85f + 0.15f * Mth.sin(time * 1.3f);

        for (int i = 0; i < count; i++) {
            Vec3 from = entity.getBeamOrigin(i, partialTick).subtract(base);
            Vec3 to = entity.getBeamEnd(i).subtract(base);
            if (style.jagged) {
                RandomSource random = RandomSource.create((long) (time / 2) * 31L + i);
                int segments = Math.max(4, (int) (to.distanceTo(from) / 3.0));
                Vec3 prev = from;
                for (int s = 1; s <= segments; s++) {
                    Vec3 next = from.lerp(to, (double) s / segments);
                    if (s < segments) {
                        double jitter = width * 2.5;
                        next = next.add((random.nextDouble() - 0.5) * jitter, (random.nextDouble() - 0.5) * jitter, (random.nextDouble() - 0.5) * jitter);
                    }
                    tube(poseStack, consumer, prev, next, width * 2.2f * pulse, r, g, b, 0.35f);
                    tube(poseStack, consumer, prev, next, width * 0.8f, style.coreR, style.coreG, style.coreB, 0.9f);
                    prev = next;
                }
            } else {
                tube(poseStack, consumer, from, to, width * 2.4f * pulse, r, g, b, 0.3f);
                tube(poseStack, consumer, from, to, width * 1.4f * pulse, r, g, b, 0.5f);
                tube(poseStack, consumer, from, to, width * 0.7f, style.coreR, style.coreG, style.coreB, 0.95f);
            }
        }
    }

    /** An open prism from {@code from} to {@code to}, in coordinates relative to the current pose. */
    private static void tube(PoseStack poseStack, VertexConsumer consumer, Vec3 from, Vec3 to, float radius,
                             float r, float g, float b, float a) {
        Vec3 d = to.subtract(from);
        float length = (float) d.length();
        if (length < 1.0E-3) return;
        poseStack.pushPose();
        poseStack.translate(from.x, from.y, from.z);
        float horizontal = (float) Math.sqrt(d.x * d.x + d.z * d.z);
        poseStack.mulPose(Axis.YP.rotation((float) Math.atan2(d.x, d.z)));
        poseStack.mulPose(Axis.XP.rotation((float) -Math.atan2(d.y, horizontal)));
        Matrix4f m = poseStack.last().pose();
        for (int s = 0; s < SIDES; s++) {
            double a0 = s * Math.PI * 2 / SIDES;
            double a1 = (s + 1) * Math.PI * 2 / SIDES;
            float x0 = (float) Math.cos(a0) * radius, y0 = (float) Math.sin(a0) * radius;
            float x1 = (float) Math.cos(a1) * radius, y1 = (float) Math.sin(a1) * radius;
            consumer.addVertex(m, x0, y0, 0).setColor(r, g, b, a);
            consumer.addVertex(m, x1, y1, 0).setColor(r, g, b, a);
            consumer.addVertex(m, x1, y1, length).setColor(r, g, b, a);
            consumer.addVertex(m, x0, y0, length).setColor(r, g, b, a);
        }
        poseStack.popPose();
    }
}
