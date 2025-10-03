package net.marum.villagebusiness.block;

import com.mojang.serialization.MapCodec;
import net.marum.villagebusiness.block.entity.RequestStandBlockEntity;
import net.marum.villagebusiness.init.VillageBusinessBlockEntityTypeInit;
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

public class RequestStandBlock extends BaseEntityBlock {

    public RequestStandBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.POWERED, false));
    }

    public static final MapCodec<RequestStandBlock> CODEC = simpleCodec(RequestStandBlock::new);

    @Override
    protected @NotNull MapCodec<? extends RequestStandBlock> codec() {
        return CODEC;
    }


    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level world, BlockPos pos, Player player, BlockHitResult blockHitResult) {
        if (world.isClientSide) return InteractionResult.SUCCESS;
        if (world.getBlockEntity(pos) instanceof RequestStandBlockEntity blockEntity) {
            player.openMenu(blockEntity);
            return InteractionResult.CONSUME;
        }
        return super.useWithoutItem(blockState, world, pos, player, blockHitResult);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return VillageBusinessBlockEntityTypeInit.REQUEST_STAND_ENTITY.create(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof RequestStandBlockEntity) {
                Containers.dropContents(world, pos, (RequestStandBlockEntity) blockEntity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        boolean isPowered = world.hasNeighborSignal(pos);
        if (state.getValue(BlockStateProperties.POWERED) != isPowered) {
            world.setBlock(pos, state.setValue(BlockStateProperties.POWERED, isPowered), 3);
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? null : createTickerHelper(type, VillageBusinessBlockEntityTypeInit.REQUEST_STAND_ENTITY, RequestStandBlockEntity::tick);
    }
}
