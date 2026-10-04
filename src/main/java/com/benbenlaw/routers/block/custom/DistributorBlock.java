package com.benbenlaw.routers.block.custom;

import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.item.RoutersItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class DistributorBlock extends RouterBlock {

public static final MapCodec<DistributorBlock> CODEC = simpleCodec(DistributorBlock::new);

public @NonNull MapCodec<DistributorBlock> codec() {
    return CODEC;
}

public DistributorBlock(BlockBehaviour.Properties properties) {
    super(properties);
}

@Override
protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
    if (!level.isClientSide()) {
        if (player.getMainHandItem().is(RoutersItems.CONNECTOR)) return InteractionResult.FAIL;
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof DistributorBlockEntity entity1) {
            player.openMenu(new SimpleMenuProvider(entity1, entity1.getDisplayName()), pos);
        } else {
            throw new IllegalStateException("Our Container provider is missing!");
        }
    }
    return InteractionResult.SUCCESS;
}

@Override
public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
    return new DistributorBlockEntity(pos, state);
}
}
