package net.marum.villagebusiness.block;

import com.mojang.serialization.MapCodec;
import net.marum.villagebusiness.block.entity.SalesStandBlockEntity;
import net.marum.villagebusiness.init.VillageBusinessBlockEntityTypeInit;
import net.marum.villagebusiness.network.PosOpeningData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SalesStandBlock extends BaseEntityBlock {

    public SalesStandBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.POWERED, false));
    }

    public static final MapCodec<SalesStandBlock> CODEC = simpleCodec(SalesStandBlock::new);

    @Override
    protected @NotNull MapCodec<? extends SalesStandBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState, Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult blockHitResult) {
        if (world.isClientSide) return InteractionResult.SUCCESS;
        if (world.getBlockEntity(pos) instanceof SalesStandBlockEntity blockEntity) {
            player.openMenu(blockEntity, buf -> PosOpeningData.of(pos).send(buf));
            return InteractionResult.CONSUME;
        }
        return super.useWithoutItem(blockState, world, pos, player, blockHitResult);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return VillageBusinessBlockEntityTypeInit.SALES_STAND_ENTITY.get().create(pos, state);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level world, @NotNull BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SalesStandBlockEntity) {
                Containers.dropContents(world, pos, (SalesStandBlockEntity) blockEntity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean notify) {
        boolean isPowered = world.hasNeighborSignal(pos);
        if (state.getValue(BlockStateProperties.POWERED) != isPowered) {
            world.setBlock(pos, state.setValue(BlockStateProperties.POWERED, isPowered), 3);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return world.isClientSide() ? null : createTickerHelper(type, VillageBusinessBlockEntityTypeInit.SALES_STAND_ENTITY.get(), SalesStandBlockEntity::tick);
    }
}
