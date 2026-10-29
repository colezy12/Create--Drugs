package net.colezy.create_drugs;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class NicotineItem extends Item {
    public static final int VAPE_PUFFS = 32;
    private final boolean vape;
    public NicotineItem(boolean vape) {
        super(vape ? new Properties().durability(VAPE_PUFFS + 1) : new Properties().stacksTo(16));
        this.vape = vape;
    }
    private boolean empty(ItemStack stack) { return vape && stack.getDamageValue() >= VAPE_PUFFS; }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (empty(stack)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.create_drugs.vape_empty"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof Player player)
                || player.getCooldowns().isOnCooldown(this) || empty(stack)) return stack;
        NicotineSystem.dose(player);
        if (vape) NicotineSystem.puff(player);
        if (Config.COOLDOWN_SECONDS.get() > 0) player.getCooldowns().addCooldown(this, Config.COOLDOWN_SECONDS.get() * 20);
        player.awardStat(Stats.ITEM_USED.get(this));
        server.sendParticles(vape ? ParticleTypes.CLOUD : ParticleTypes.SMOKE,
                player.getX(), player.getEyeY(), player.getZ(), 6, 0.15, 0.1, 0.15, 0.01);
        server.playSound(null, player.blockPosition(), NicotineContent.INHALE.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
        if (!player.getAbilities().instabuild) {
            if (vape) stack.setDamageValue(stack.getDamageValue() + 1);
            else stack.shrink(1);
        }
        return stack;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.create_drugs.nicotine").withStyle(ChatFormatting.GRAY));
        if (vape) tooltip.add(Component.translatable("tooltip.create_drugs.vape", Math.max(0, VAPE_PUFFS - stack.getDamageValue())).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.create_drugs.long_term").withStyle(ChatFormatting.DARK_RED));
    }
}
