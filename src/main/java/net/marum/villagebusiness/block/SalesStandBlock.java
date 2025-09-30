package net.marum.villagebusiness.block;

import org.jetbrains.annotations.Nullable;

import net.marum.villagebusiness.block.entity.SalesStandBlockEntity;
import net.marum.villagebusiness.init.VillageBusinessBlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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

public class SalesStandBlock extends BaseEntityBlock {

    public SalesStandBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.POWERED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide()) {
            if (world.getBlockEntity(pos) instanceof SalesStandBlockEntity blockEntity) {
                MenuProvider screenHandlerFactory = blockEntity;
                if (screenHandlerFactory != null) {
                    player.openMenu(screenHandlerFactory);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return VillageBusinessBlockEntityTypeInit.SALES_STAND_ENTITY.create(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
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
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        boolean isPowered = world.hasNeighborSignal(pos);
        if (state.getValue(BlockStateProperties.POWERED) != isPowered) {
            world.setBlock(pos, state.setValue(BlockStateProperties.POWERED, isPowered), 3);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? null : createTickerHelper(type, VillageBusinessBlockEntityTypeInit.SALES_STAND_ENTITY, SalesStandBlockEntity::tick);
    }
}
