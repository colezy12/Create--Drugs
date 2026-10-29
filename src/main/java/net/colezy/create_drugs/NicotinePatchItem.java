package net.colezy.create_drugs;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class NicotinePatchItem extends Item {
    public NicotinePatchItem() { super(new Properties().stacksTo(16)); }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!level.isClientSide && NicotineSystem.patchRemaining(player) > 0) {
            player.displayClientMessage(Component.translatable("message.create_drugs.patch_already_active"), true);
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player && NicotineSystem.applyPatch(player)) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.awardStat(Stats.ITEM_USED.get(this));
            player.displayClientMessage(Component.translatable("message.create_drugs.patch_applied", Config.PATCH_SECONDS.get()), true);
        }
        return stack;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.create_drugs.nicotine_patch").withStyle(ChatFormatting.GRAY));
    }
}
