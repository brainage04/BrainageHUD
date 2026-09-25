package io.github.brainage04.brainagehud.waypoint;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.brainage04.brainagehud.config.other.WaypointConfig;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

/**
 * Draws the current dimension's visible waypoints in the world: a tinted beacon beam, a spinning
 * gem hovering above the spot with a pulse spreading over the ground beneath it, and a label with
 * the name and distance that stays readable through walls.
 *
 * <p>Everything is submitted in camera-relative coordinates from the loader's level render hook.
 */
public final class WaypointRenderer {
    /**
     * Waypoints further than this share of the render distance are drawn scaled towards the
     * camera. Scaling about the camera leaves a waypoint's size and place on screen unchanged, but
     * keeps it inside the far plane and out of the distance fog.
     */
    private static final double PROJECTION_SHARE = 0.5D;
    /** Past these distances the marker and label grow with distance, so they keep their size on screen. */
    private static final double MARKER_GROWTH_START = 24.0D;
    private static final double LABEL_GROWTH_START = 12.0D;
    /** Within this distance every label shows its name. */
    private static final double NAME_DISTANCE = 16.0D;
    /** How close to the crosshair (in degrees) a waypoint must be to show its name from afar. */
    private static final float FOCUS_ANGLE = 5.0F;
    private static final float TEXT_SCALE = 0.025F;
    private static final int PULSE_SEGMENTS = 48;
    private static final double PULSE_DISTANCE = 48.0D;

    private WaypointRenderer() {}

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Camera camera) {
        WaypointConfig config = getConfig().waypointConfig;
        if (!config.showInWorld) return;

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) return;

        String dimension = level.dimension().identifier().toString();
        Optional<List<Waypoint>> waypoints = WaypointStore.current();
        boolean anyWaypoints = waypoints.isPresent() && !waypoints.get().isEmpty();
        if (!anyWaypoints && !config.showWorldCentre) return;

        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        float time = Math.floorMod(level.getGameTime(), 24_000L) + partialTick;
        double projectionDistance = Math.max(32.0D, minecraft.options.getEffectiveRenderDistance() * 16.0D * PROJECTION_SHARE);
        Frame frame = new Frame(camera, minecraft.font, config, time, projectionDistance, level.getMinY(), level.getMaxY() + 1);

        if (config.showWorldCentre) {
            submitWaypoint(poseStack, collector, frame, WorldCentre.in(dimension));
        }
        if (anyWaypoints) {
            for (Waypoint waypoint : waypoints.get()) {
                if (waypoint.visible && waypoint.dimension.equals(dimension)) {
                    submitWaypoint(poseStack, collector, frame, waypoint);
                }
            }
        }
    }

    /**
     * @param beamBottom the lowest block Y of the dimension
     * @param beamTop one above the highest block Y of the dimension
     */
    private record Frame(Camera camera, Font font, WaypointConfig config, float time, double projectionDistance, int beamBottom, int beamTop) {}

    private static void submitWaypoint(PoseStack poseStack, SubmitNodeCollector collector, Frame frame, Waypoint waypoint) {
        Vec3 base = waypoint.standingPosition();
        Vec3 offset = base.subtract(frame.camera.position());
        double distance = offset.length();
        double horizontalDistance = offset.horizontalDistance();
        int rgb = waypoint.colour & 0xFFFFFF;
        // standing on the waypoint: its gem and label would sit in the player's face
        boolean standingOnIt = horizontalDistance < 1.0D && offset.y > -2.5D && offset.y < 0.5D;

        float markerSize = (float) Math.max(1.0D, distance / MARKER_GROWTH_START);
        float bob = 0.12F * Mth.sin(frame.time * 0.08F);
        float gemHalfHeight = 0.36F * markerSize;
        float gemCentre = 0.1F + (1.3F + bob) * markerSize;

        poseStack.pushPose();
        float projection = distance > frame.projectionDistance ? (float) (frame.projectionDistance / distance) : 1.0F;
        poseStack.scale(projection, projection, projection);
        poseStack.translate(offset.x, offset.y, offset.z);

        if (frame.config.showBeams && horizontalDistance > 1.5D) {
            submitBeam(poseStack, collector, frame, waypoint.y, rgb, horizontalDistance);
        }
        if (frame.config.showMarkers) {
            if (distance < PULSE_DISTANCE) submitPulse(poseStack, collector, frame.time, rgb);
            if (!standingOnIt) submitGem(poseStack, collector, frame.time, rgb, gemCentre, gemHalfHeight);
        }
        if (frame.config.showLabels && !standingOnIt) {
            Vec3 labelPosition = base.add(0.0D, gemCentre + gemHalfHeight + 0.3D * markerSize, 0.0D);
            submitLabel(poseStack, collector, frame, waypoint, rgb, distance, labelPosition, (float) (labelPosition.y - base.y));
        }

        poseStack.popPose();
    }

    /**
     * A slim beacon beam through the whole height of the dimension; like vanilla's, it widens with
     * distance so it stays visible.
     */
    private static void submitBeam(PoseStack poseStack, SubmitNodeCollector collector, Frame frame, int waypointY, int rgb, double horizontalDistance) {
        float widen = (float) Math.max(1.0D, horizontalDistance / 64.0D);
        poseStack.pushPose();
        // the beam is built from the block corner, like vanilla's block entity renderer
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        BeaconRenderer.submitBeaconBeam(
                poseStack,
                collector,
                BeaconRenderer.BEAM_LOCATION,
                1.0F,
                frame.time % 40.0F,
                frame.beamBottom - waypointY,
                frame.beamTop - frame.beamBottom,
                ARGB.opaque(rgb),
                0.09F * widen,
                0.15F * widen);
        poseStack.popPose();
    }

    /** An octahedron that spins and bobs, lit from above: bright top facets, dark lower ones. */
    private static void submitGem(PoseStack poseStack, SubmitNodeCollector collector, float time, int rgb, float centre, float halfHeight) {
        float radius = halfHeight * 0.7F;
        float spin = time * 2.5F * Mth.DEG_TO_RAD;
        int[] shades = {
            ARGB.color(235, ARGB.srgbLerp(0.45F, rgb, 0xFFFFFF)),
            ARGB.color(235, ARGB.srgbLerp(0.15F, rgb, 0xFFFFFF)),
            ARGB.color(235, ARGB.scaleRGB(rgb, 0.75F)),
            ARGB.color(235, ARGB.scaleRGB(rgb, 0.5F)),
        };

        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            float[] xs = new float[4];
            float[] zs = new float[4];
            for (int corner = 0; corner < 4; corner++) {
                float angle = spin + corner * Mth.HALF_PI;
                xs[corner] = Mth.cos(angle) * radius;
                zs[corner] = Mth.sin(angle) * radius;
            }
            for (int corner = 0; corner < 4; corner++) {
                int next = (corner + 1) % 4;
                int alternate = corner % 2;
                triangle(consumer, matrix, shades[alternate], 0.0F, centre + halfHeight, 0.0F,
                        xs[corner], centre, zs[corner], xs[next], centre, zs[next]);
                triangle(consumer, matrix, shades[2 + alternate], 0.0F, centre - halfHeight, 0.0F,
                        xs[next], centre, zs[next], xs[corner], centre, zs[corner]);
            }
        });
    }

    /** Two rings spreading over the ground and fading, half a cycle apart, like a sonar ping. */
    private static void submitPulse(PoseStack poseStack, SubmitNodeCollector collector, float time, int rgb) {
        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            for (int ring = 0; ring < 2; ring++) {
                float progress = Mth.frac(time / 50.0F + ring * 0.5F);
                float outer = 0.3F + 1.5F * progress;
                float inner = Math.max(0.0F, outer - 0.06F - 0.1F * progress);
                float fade = 1.0F - progress;
                int colour = ARGB.color((int) (200 * fade * fade), ARGB.srgbLerp(0.3F, rgb, 0xFFFFFF));
                for (int segment = 0; segment < PULSE_SEGMENTS; segment++) {
                    float from = segment * Mth.TWO_PI / PULSE_SEGMENTS;
                    float to = (segment + 1) * Mth.TWO_PI / PULSE_SEGMENTS;
                    float y = 0.02F;
                    consumer.addVertex(matrix, Mth.cos(from) * inner, y, Mth.sin(from) * inner).setColor(colour);
                    consumer.addVertex(matrix, Mth.cos(from) * outer, y, Mth.sin(from) * outer).setColor(colour);
                    consumer.addVertex(matrix, Mth.cos(to) * outer, y, Mth.sin(to) * outer).setColor(colour);
                    consumer.addVertex(matrix, Mth.cos(to) * inner, y, Mth.sin(to) * inner).setColor(colour);
                }
            }
        });
    }

    /**
     * The name (in the waypoint's colour) over the distance, facing the camera. It is drawn dimly
     * through walls and brightly where nothing is in front of it, like a vanilla name tag, and grows
     * with distance so that it keeps its size on screen.
     */
    private static void submitLabel(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            Frame frame,
            Waypoint waypoint,
            int rgb,
            double distance,
            Vec3 labelPosition,
            float height) {
        boolean showName = frame.config.alwaysShowNames || distance < NAME_DISTANCE || isFocused(frame.camera, labelPosition);
        float scale = TEXT_SCALE * (float) Math.max(1.0D, distance / LABEL_GROWTH_START) * frame.config.labelScalePercent / 100.0F;

        poseStack.pushPose();
        poseStack.translate(0.0F, height, 0.0F);
        poseStack.mulPose(frame.camera.rotation());
        poseStack.scale(scale, -scale, scale);

        Component distanceText = Component.literal(formatDistance(distance));
        if (showName) {
            text(poseStack, collector, frame.font, Component.literal(waypoint.name), -2 * frame.font.lineHeight - 1, rgb);
            text(poseStack, collector, frame.font, distanceText, -frame.font.lineHeight, 0xE0E0E0);
        } else {
            text(poseStack, collector, frame.font, distanceText, -frame.font.lineHeight, rgb);
        }

        poseStack.popPose();
    }

    private static void text(PoseStack poseStack, SubmitNodeCollector collector, Font font, Component text, int y, int rgb) {
        float x = -font.width(text) / 2.0F;
        collector.submitText(poseStack, x, y, text.getVisualOrderText(), false, Font.DisplayMode.SEE_THROUGH,
                LightCoordsUtil.FULL_BRIGHT, ARGB.color(110, rgb), ARGB.color(96, 0), 0);
        collector.submitText(poseStack, x, y, text.getVisualOrderText(), false, Font.DisplayMode.NORMAL,
                LightCoordsUtil.FULL_BRIGHT, ARGB.opaque(rgb), 0, 0);
    }

    private static boolean isFocused(Camera camera, Vec3 target) {
        Vec3 direction = target.subtract(camera.position()).normalize();
        Vector3fc forward = camera.forwardVector();
        double cosine = direction.x * forward.x() + direction.y * forward.y() + direction.z * forward.z();
        return cosine > Math.cos(FOCUS_ANGLE * Mth.DEG_TO_RAD);
    }

    /** Metres below a kilometre, then kilometres to one decimal place. */
    static String formatDistance(double distance) {
        long metres = Math.round(distance);
        if (metres < 1000L) return metres + " m";
        return String.format(Locale.ROOT, "%.1f km", distance / 1000.0D);
    }

    private static void triangle(
            VertexConsumer consumer, Matrix4f matrix, int colour,
            float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2) {
        // the quad pipeline draws a triangle as a quad with its last corner repeated
        consumer.addVertex(matrix, x0, y0, z0).setColor(colour);
        consumer.addVertex(matrix, x1, y1, z1).setColor(colour);
        consumer.addVertex(matrix, x2, y2, z2).setColor(colour);
        consumer.addVertex(matrix, x2, y2, z2).setColor(colour);
    }
}
