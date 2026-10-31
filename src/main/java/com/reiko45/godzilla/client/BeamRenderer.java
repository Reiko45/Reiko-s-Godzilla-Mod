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
            double length = to.distanceTo(from);
            switch (style.shape) {
                case JAGGED -> {
                    RandomSource random = RandomSource.create((long) (time / 2) * 31L + i);
                    int segments = Math.max(4, (int) (length / 3.0));
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
                }
                case CURVED -> {
                    int segments = Math.max(8, (int) (length / 2.0));
                    Vec3 prev = from;
                    for (int s = 1; s <= segments; s++) {
                        Vec3 next = style.pointAt(from, to, (double) s / segments);
                        tube(poseStack, consumer, prev, next, width * 2.4f * pulse, r, g, b, 0.3f);
                        tube(poseStack, consumer, prev, next, width * 0.8f, style.coreR, style.coreG, style.coreB, 0.9f);
                        prev = next;
                    }
                }
                case SPIRAL -> {
                    straight(poseStack, consumer, from, to, width, pulse, r, g, b, style);
                    // Two strands twisting around the beam.
                    Vec3 axis = to.subtract(from).normalize();
                    Vec3 side = Math.abs(axis.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
                    Vec3 u = axis.cross(side).normalize();
                    Vec3 v = axis.cross(u).normalize();
                    int steps = Math.max(12, (int) (length * 2));
                    for (int strand = 0; strand < 2; strand++) {
                        Vec3 prev = null;
                        for (int s = 0; s <= steps; s++) {
                            double t = (double) s / steps;
                            double angle = t * length * 0.9 - time * 0.6 + strand * Math.PI;
                            double rad = width * 2.2;
                            Vec3 p = from.lerp(to, t).add(u.scale(Math.cos(angle) * rad)).add(v.scale(Math.sin(angle) * rad));
                            if (prev != null) tube(poseStack, consumer, prev, p, width * 0.45f, 1.0f, 0.95f, 0.6f, 0.7f);
                            prev = p;
                        }
                    }
                }
                case RINGS -> {
                    straight(poseStack, consumer, from, to, width * 0.7f, pulse, r, g, b, style);
                    Vec3 axis = to.subtract(from).normalize();
                    Vec3 side = Math.abs(axis.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
                    Vec3 u = axis.cross(side).normalize();
                    Vec3 v = axis.cross(u).normalize();
                    double spacing = 2.5;
                    double offset = (time * 0.5) % spacing;
                    for (double d = offset; d < length; d += spacing) {
                        Vec3 c = from.add(axis.scale(d));
                        double ringRadius = width * (2.0 + d / Math.max(1.0, length) * 1.5);
                        Vec3 prev = null;
                        for (int s = 0; s <= 10; s++) {
                            double a = s * Math.PI * 2 / 10;
                            Vec3 p = c.add(u.scale(Math.cos(a) * ringRadius)).add(v.scale(Math.sin(a) * ringRadius));
                            if (prev != null) tube(poseStack, consumer, prev, p, width * 0.25f, r, g, b, 0.6f);
                            prev = p;
                        }
                    }
                }
                default -> straight(poseStack, consumer, from, to, width, pulse, r, g, b, style);
            }
        }
    }

    private static void straight(PoseStack poseStack, VertexConsumer consumer, Vec3 from, Vec3 to, float width, float pulse,
                                 float r, float g, float b, BeamStyle style) {
        tube(poseStack, consumer, from, to, width * 2.4f * pulse, r, g, b, 0.3f);
        tube(poseStack, consumer, from, to, width * 1.4f * pulse, r, g, b, 0.5f);
        tube(poseStack, consumer, from, to, width * 0.7f, style.coreR, style.coreG, style.coreB, 0.95f);
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
