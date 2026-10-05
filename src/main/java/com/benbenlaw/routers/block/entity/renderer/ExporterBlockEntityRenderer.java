package com.benbenlaw.routers.block.entity.renderer;

import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.api.screen.client.RouterUIRenderers;
import com.benbenlaw.routers.block.custom.ExporterBlock;
import com.benbenlaw.routers.block.custom.ImporterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class ExporterBlockEntityRenderer
        implements BlockEntityRenderer<ExporterBlockEntity, ExporterBlockEntityRendererState> {

    public ExporterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public ExporterBlockEntityRendererState createRenderState() {
        return new ExporterBlockEntityRendererState();
    }

    @Override
    public void extractRenderState(ExporterBlockEntity blockEntity, ExporterBlockEntityRendererState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {

        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        if (blockEntity.getLevel() == null) return;
        if (!(blockEntity.getBlockState().getBlock() instanceof ExporterBlock)) return;

        state.exporterPosition =
                new GlobalPos(blockEntity.getLevel().dimension(), blockEntity.getBlockPos());

        state.exporterFacing =
                blockEntity.getBlockState().getValue(ExporterBlock.FACING);

        state.importerPositions =
                blockEntity.importerPositions == null
                        ? new ArrayList<>()
                        : new ArrayList<>(blockEntity.importerPositions);

        state.upgradeItems = blockEntity.getUpgradeItemHandler();
    }

    @Override
    public void submit(ExporterBlockEntityRendererState state,PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {

        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(Tags.Items.TOOLS_WRENCH)) return;

        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        if (state == null) return;
        if (state.exporterPosition == null) return;
        if (state.importerPositions == null || state.importerPositions.isEmpty()) return;
        if (state.upgradeItems == null) return;

        Vec3 camera = cameraRenderState.pos;

        Vec3 exporterPos = getBeamStart(state.exporterPosition.pos(), level);
        ItemStacksResourceHandler itemHandler = state.upgradeItems;

        Set<String> uniqueKeys = new HashSet<>();
        List<float[]> uniqueColors = new ArrayList<>();

        for (int slot = 0; slot < itemHandler.size(); slot++) {
            ItemStack stack = itemHandler.getResource(slot).toStack();
            float[] tint = getBeamTint(stack);

            if (tint[0] == 0f && tint[1] == 0f && tint[2] == 0f) continue;

            String key = tint[0] + "," + tint[1] + "," + tint[2];
            if (uniqueKeys.add(key)) {
                uniqueColors.add(tint);
            }
        }

        if (uniqueColors.isEmpty()) return;

        for (GlobalPos importerPosRaw : state.importerPositions) {

            Vec3 importerPos = getBeamStart(importerPosRaw.pos(), level);

            Vec3 delta = importerPos.subtract(exporterPos);
            double length = delta.length();
            if (length < 0.001) continue;

            Vec3 dir = delta.normalize();

            Vec3 up = new Vec3(0, 1, 0);
            Vec3 perp = dir.cross(up);
            if (perp.lengthSqr() < 1e-6) perp = dir.cross(new Vec3(1, 0, 0));
            perp = perp.normalize();
            Vec3 side = dir.cross(perp).normalize();

            int count = uniqueColors.size();
            double radius = 0.03;

            for (int i = 0; i < count; i++) {

                double angle = (2 * Math.PI * i) / count;
                Vec3 offset = perp.scale(Math.cos(angle) * radius)
                        .add(side.scale(Math.sin(angle) * radius));

                Vec3 beamStartWorld = exporterPos.add(offset).subtract(camera);

                float[] tint = uniqueColors.get(i);

                submitNodeCollector.submitCustomGeometry(poseStack, NO_CULL_BEAM, (pose, consumer) -> {

                    float beamRadius = 0.0075f;
                    float time = (System.currentTimeMillis() % 10000L) / 10000.0f;
                    float vOffset = -(time * 2f);

                    PoseStack local = new PoseStack();
                    local.translate(
                            (float) beamStartWorld.x,
                            (float) beamStartWorld.y,
                            (float) beamStartWorld.z
                    );

                    Quaternionf rotation = new Quaternionf().rotationTo(
                            new Vector3f(0, 1, 0),
                            new Vector3f((float) dir.x, (float) dir.y, (float) dir.z)
                    );

                    local.mulPose(rotation);

                    for (int face = 0; face < 4; face++) {
                        local.pushPose();
                        local.mulPose(Axis.YP.rotationDegrees(face * 90f));

                        PoseStack.Pose facePose = local.last();

                        addBeamVertex(consumer, facePose,
                                -beamRadius, (float) length, -beamRadius,
                                tint[0], tint[1], tint[2], 1f,
                                0f, vOffset + (float) length,
                                0xF000F0, OverlayTexture.NO_OVERLAY);

                        addBeamVertex(consumer, facePose,
                                beamRadius, (float) length, -beamRadius,
                                tint[0], tint[1], tint[2], 1f,
                                1f, vOffset + (float) length,
                                0xF000F0, OverlayTexture.NO_OVERLAY);

                        addBeamVertex(consumer, facePose,
                                beamRadius, 0f, -beamRadius,
                                tint[0], tint[1], tint[2], 1f,
                                1f, vOffset,
                                0xF000F0, OverlayTexture.NO_OVERLAY);

                        addBeamVertex(consumer, facePose,
                                -beamRadius, 0f, -beamRadius,
                                tint[0], tint[1], tint[2], 1f,
                                0f, vOffset,
                                0xF000F0, OverlayTexture.NO_OVERLAY);

                        local.popPose();
                    }
                });
            }
        }
    }

    private static void addBeamVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float r, float g, float b, float a, float u, float v, int light, int overlay) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0f, 1f, 0f);
    }

    private Vec3 getBeamStart(BlockPos pos, Level level) {
        var state = level.getBlockState(pos);

        Direction facing = Direction.UP;

        if (state.hasProperty(ExporterBlock.FACING))
            facing = state.getValue(ExporterBlock.FACING);
        else if (state.hasProperty(ImporterBlock.FACING))
            facing = state.getValue(ImporterBlock.FACING);

        VoxelShape shape = state.getShape(level, pos, CollisionContext.empty());
        AABB bounds = shape.isEmpty() ? new AABB(0, 0, 0, 1, 1, 1) : shape.bounds();

        double cx = (bounds.minX + bounds.maxX) * 0.5;
        double cy = (bounds.minY + bounds.maxY) * 0.5;
        double cz = (bounds.minZ + bounds.maxZ) * 0.5;

        final double INSET = 0.2;

        switch (facing) {
            case UP -> cy = bounds.maxY - INSET;
            case DOWN -> cy = bounds.minY + INSET;
            case NORTH -> cz = bounds.minZ + INSET;
            case SOUTH -> cz = bounds.maxZ - INSET;
            case WEST -> cx = bounds.minX + INSET;
            case EAST -> cx = bounds.maxX - INSET;
        }

        return new Vec3(pos.getX() + cx, pos.getY() + cy, pos.getZ() + cz);
    }

    private float[] getBeamTint(ItemStack stack) {
        if (stack.isEmpty()) return new float[]{0f, 0f, 0f};

        for (ButtonType type : RouterButtonTypes.all()) {
            if (stack.is(type.getUnlockedBy())) return RouterUIRenderers.getColor(type);
        }

        return new float[]{0f, 0f, 0f};
    }

    private static final RenderType NO_CULL_BEAM = RenderType.create("no_cull_beam", RenderSetup.builder(
            RenderPipelines.BEACON_BEAM_OPAQUE)
                    .withTexture("Sampler0", Identifier.withDefaultNamespace("textures/entity/beacon/beacon_beam.png"))
                    .sortOnUpload()
                    .createRenderSetup()
    );

    @Override public boolean shouldRender(ExporterBlockEntity be, Vec3 cameraPos) {
        return true;
    }

    @Override public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override public int getViewDistance() {
        return 64;
    }

    @Override
    public @NonNull AABB getRenderBoundingBox(ExporterBlockEntity blockEntity) {
        return AABB.encapsulatingFullBlocks(blockEntity.getBlockPos().above(32).north(32).east(32),
                blockEntity.getBlockPos().below(32).south(32).west(32));
    }
}