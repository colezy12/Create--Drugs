package net.colezy.create_drugs;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class DrugItem extends Item {
    private final String name;
    private final Holder<MobEffect> benefit;
    private final Holder<MobEffect> drawback;
    private final int benefitLevel;
    private final int drawbackLevel;
    public DrugItem(String name, Holder<MobEffect> benefit, int benefitLevel,
                   Holder<MobEffect> drawback, int drawbackLevel) {
        super(new Properties().stacksTo(16));
        this.name = name;
        this.benefit = benefit;
        this.benefitLevel = benefitLevel;
        this.drawback = drawback;
        this.drawbackLevel = drawbackLevel;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(player.getItemInHand(hand));
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            if (entity instanceof Player p && p.getCooldowns().isOnCooldown(this)) return stack;
            int duration = Config.EFFECT_SECONDS.get() * 20;
            int benefitDuration = entity instanceof Player player ? LongTermEffects.onUse(player, name, duration) : duration;
            entity.addEffect(new MobEffectInstance(benefit, benefitDuration, benefitLevel));
            entity.addEffect(new MobEffectInstance(drawback, duration, drawbackLevel));
            MobEffectInstance previous = entity.getEffect(Create_drugs.INTOXICATION);
            int exposure = previous == null ? 0 : Math.min(previous.getAmplifier() + 1, 3);
            entity.addEffect(new MobEffectInstance(Create_drugs.INTOXICATION,
                    Config.INTOXICATION_SECONDS.get() * 20, exposure));
            if (Config.OVERUSE_PENALTIES.get() && exposure >= 2) {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 1));
                if (entity instanceof Player player) player.displayClientMessage(
                        Component.translatable("message.create_drugs.overuse").withStyle(ChatFormatting.RED), true);
            }
            level.playSound(null, entity.blockPosition(), Create_drugs.CONSUME.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
            if (entity instanceof Player player) {
                player.awardStat(Stats.ITEM_USED.get(this));
                if (Config.COOLDOWN_SECONDS.get() > 0) player.getCooldowns().addCooldown(this, Config.COOLDOWN_SECONDS.get() * 20);
                if (!player.getAbilities().instabuild) stack.shrink(1);
            } else stack.shrink(1);
        }
        return stack;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.create_drugs." + name).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.create_drugs.overuse").withStyle(ChatFormatting.DARK_RED));
        if (name.equals("beer")) tooltip.add(Component.translatable("tooltip.create_drugs.long_term").withStyle(ChatFormatting.DARK_RED));
    }
}
