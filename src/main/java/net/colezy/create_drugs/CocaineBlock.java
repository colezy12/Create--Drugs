package net.colezy.create_drugs;

import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Cocaine compressed by a Create mechanical press. The pressed item is turned into a flat 1px plate placed on top of the
 * depot (table) it was pressed on. It cannot be picked up; right-click it to snort it.
 */
@EventBusSubscriber(modid = Create_drugs.MODID)
public final class CocaineBlock {
    /** A 1px tall plate that sits on the depot it was pressed on, so it is shifted down by the depot's 3px gap. */
    public static final class PlateBlock extends Block {
        private static final VoxelShape SHAPE = Block.box(1, -3, 1, 15, -2, 15);

        public PlateBlock(Properties properties) { super(properties); }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

        @Override
        protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
            return BlockEntityBehaviour.get(level, pos.below(), DepotBehaviour.TYPE) != null;
        }

        @Override
        protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                         BlockPos pos, BlockPos neighborPos) {
            return direction == Direction.DOWN && !canSurvive(state, level, pos)
                    ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : state;
        }
    }

    public static final DeferredBlock<PlateBlock> BLOCK = Create_drugs.BLOCKS.register("cocaine_block",
            () -> new PlateBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(0.1F).sound(SoundType.SAND).noCollission().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredItem<Item> ITEM = Create_drugs.ITEMS.register("cocaine_block",
            () -> new net.minecraft.world.item.BlockItem(BLOCK.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip,
                                            net.minecraft.world.item.TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.create_drugs.cocaine_block").withStyle(ChatFormatting.GRAY));
                }
            });

    private static boolean snort(Player player, Level level) {
        if (player.isSpectator() || player.getCooldowns().isOnCooldown(Create_drugs.COCAINE.get())) return false;
        Create_drugs.COCAINE.get().finishUsingItem(new ItemStack(Create_drugs.COCAINE.get()), level, player);
        return true;
    }

    /** Turns freshly pressed cocaine blocks held by nearby depots into plates placed on the depot. */
    @SubscribeEvent
    public static void convertPressed(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 5 != 0) return;
        ServerLevel level = player.serverLevel();
        int cx = player.blockPosition().getX() >> 4, cz = player.blockPosition().getZ() >> 4;
        for (int x = cx - 2; x <= cx + 2; x++) for (int z = cz - 2; z <= cz + 2; z++) {
            var chunk = level.getChunkSource().getChunkNow(x, z);
            if (chunk == null) continue;
            for (var be : java.util.List.copyOf(chunk.getBlockEntities().values())) {
                DepotBehaviour depot = BlockEntityBehaviour.get(be, DepotBehaviour.TYPE);
                if (depot == null || !isBlock(depot.getHeldItemStack())) continue;
                BlockPos above = be.getBlockPos().above();
                if (!level.getBlockState(above).isAir()) continue;
                level.setBlockAndUpdate(above, BLOCK.get().defaultBlockState());
                ItemStack held = depot.getHeldItemStack();
                if (held.getCount() > 1) held.shrink(1); else depot.removeHeldItem();
                be.setChanged();
                depot.blockEntity.notifyUpdate();
            }
        }
    }

    public static boolean isBlock(ItemStack stack) { return stack.is(ITEM.get()); }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        var level = event.getLevel();
        if (level.getBlockState(event.getPos()).is(BLOCK.get())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            if (!level.isClientSide && snort(event.getEntity(), level)) level.removeBlock(event.getPos(), false);
            return;
        }
        BlockPos abovePos = event.getPos().above();
        if (level.getBlockState(abovePos).is(BLOCK.get()) && BlockEntityBehaviour.get(level, event.getPos(), DepotBehaviour.TYPE) != null) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            if (!level.isClientSide && snort(event.getEntity(), level)) level.removeBlock(abovePos, false);
            return;
        }
        DepotBehaviour depot = BlockEntityBehaviour.get(level, event.getPos(), DepotBehaviour.TYPE);
        if (depot == null || !isBlock(depot.getHeldItemStack())) return;
        // Always swallow the click so Create's depot never hands the the compressed block to the player.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (level.isClientSide) return;
        if (!snort(event.getEntity(), level)) return;
        ItemStack held = depot.getHeldItemStack();
        if (held.getCount() > 1) held.shrink(1); else depot.removeHeldItem();
        depot.blockEntity.notifyUpdate();
    }

    @SubscribeEvent
    public static void pickup(ItemEntityPickupEvent.Pre event) {
        if (isBlock(event.getItemEntity().getItem())) event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
    }

    public static void init() {}
    private CocaineBlock() {}
}
