package net.colezy.create_drugs;

import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/** Nicotine is shared by cigars and vapes, independently of the other substance timers. */
@net.neoforged.fml.common.EventBusSubscriber(modid = Create_drugs.MODID)
public final class NicotineSystem {
    private static final String KEY = "nicotine";

    public static CompoundTag state(Player player) {
        CompoundTag history = player.getData(LongTermEffects.HISTORY);
        if (!history.contains(KEY)) history.put(KEY, new CompoundTag());
        return history.getCompound(KEY);
    }

    public static int patchRemaining(Player player) {
        if (!player.hasData(LongTermEffects.HISTORY)) return 0;
        return Math.clamp(player.getData(LongTermEffects.HISTORY).getCompound(KEY).getInt("patchSeconds"), 0, 7200);
    }

    public static void dose(Player player) {
        if (!Config.LONG_TERM_EFFECTS.get() || player.isCreative() || player.isSpectator()) return;
        CompoundTag state = state(player);
        state.putInt("points", Math.min(100, LongTermEffects.points(state) + Config.DEPENDENCE_PER_USE.get()));
        state.putInt("abstinentSeconds", 0);
        state.putInt("recoverySeconds", 0);
        state.putInt("withdrawalSeconds", 0);
        player.removeEffect(NicotineContent.WITHDRAWAL);
        player.displayClientMessage(Component.translatable("message.create_drugs.nicotine_dose", LongTermEffects.points(state)), true);
    }

    public static boolean applyPatch(Player player) {
        if (patchRemaining(player) > 0) return false;
        CompoundTag state = state(player);
        state.putInt("patchSeconds", Config.PATCH_SECONDS.get());
        state.putInt("abstinentSeconds", 0);
        state.putInt("recoverySeconds", 0);
        state.putInt("withdrawalSeconds", 0);
        player.removeEffect(NicotineContent.WITHDRAWAL);
        player.addEffect(new MobEffectInstance(NicotineContent.PATCH_ACTIVE, Config.PATCH_SECONDS.get() * 20, 0, false, false, true));
        return true;
    }

    public static void advanceSecond(Player player) {
        if (player.level().isClientSide || player.isCreative() || player.isSpectator() || !player.hasData(LongTermEffects.HISTORY)) return;
        CompoundTag history = player.getData(LongTermEffects.HISTORY);
        if (!history.contains(KEY)) return;
        CompoundTag state = history.getCompound(KEY);
        lungSecond(player, state);
        int remaining = patchRemaining(player);
        if (remaining > 0) {
            state.putInt("patchSeconds", remaining - 1);
            state.putInt("abstinentSeconds", 0);
            state.putInt("withdrawalSeconds", 0);
            player.removeEffect(NicotineContent.WITHDRAWAL);
            if (remaining == 1) {
                player.removeEffect(NicotineContent.PATCH_ACTIVE);
                player.displayClientMessage(Component.translatable("message.create_drugs.patch_expired"), true);
            } else {
                player.addEffect(new MobEffectInstance(NicotineContent.PATCH_ACTIVE, (remaining - 1) * 20, 0, false, false, true));
            }
            return;
        }
        player.removeEffect(NicotineContent.PATCH_ACTIVE);
        if (!Config.LONG_TERM_EFFECTS.get()) { player.removeEffect(NicotineContent.WITHDRAWAL); return; }
        int points = LongTermEffects.points(state);
        if (points == 0) {
            if (lungDamage(state) == 0 && state.getInt("vapeLoad") <= 0) history.remove(KEY);
            player.removeEffect(NicotineContent.WITHDRAWAL);
            return;
        }
        int abstinence = Math.min(86400, Math.max(0, state.getInt("abstinentSeconds")) + 1);
        state.putInt("abstinentSeconds", abstinence);
        if (abstinence < Config.NICOTINE_DELAY.get()) return;
        int recovery = Math.max(0, state.getInt("recoverySeconds")) + 1;
        if (recovery >= Config.RECOVERY_SECONDS.get()) { points--; recovery = 0; }
        state.putInt("points", points);
        state.putInt("recoverySeconds", recovery);
        if (points < Config.NICOTINE_THRESHOLD.get()) {
            state.putInt("withdrawalSeconds", 0);
            player.removeEffect(NicotineContent.WITHDRAWAL);
            return;
        }
        int withdrawal = Math.min(86400, Math.max(0, state.getInt("withdrawalSeconds")) + 1);
        state.putInt("withdrawalSeconds", withdrawal);
        int level = withdrawalLevel(withdrawal);
        player.addEffect(new MobEffectInstance(NicotineContent.WITHDRAWAL, 60, level, false, false, true));
        if (withdrawal == 1) player.displayClientMessage(Component.translatable("message.create_drugs.nicotine_withdrawal"), true);
        else if (withdrawal % Config.NICOTINE_ESCALATION_SECONDS.get() == 0 && level > 0 && withdrawal / Config.NICOTINE_ESCALATION_SECONDS.get() == level)
            player.displayClientMessage(Component.translatable("message.create_drugs.withdrawal_worse", level), true);
        // Symptoms stack up as the withdrawal lengthens.
        if (level >= 1) player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, level >= 4 ? 2 : level >= 2 ? 1 : 0, false, false, true));
        if (level >= 1) player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, level >= 3 ? 1 : 0, false, false, true));
        if (level >= 2) player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, level >= 3 ? 1 : 0, false, false, true));
        if (Config.NICOTINE_DISORIENTATION.get()) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80 + level * 40, 0, false, false, true));
            if (level >= 3) player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, true));
            int darknessInterval = Math.max(10, 30 - level * 5);
            if (withdrawal % darknessInterval == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60 + level * 20, 0, false, false, true));
                player.level().playSound(null, player.blockPosition(), NicotineContent.WITHDRAWAL_SOUND.get(), SoundSource.PLAYERS, 0.35F, 0.8F);
            }
        }
        if (Config.NICOTINE_DAMAGE.get() && withdrawal % Math.max(3, 10 - level * 2) == 0) {
            boolean lethal = Config.NICOTINE_LETHAL.get() && level >= LETHAL_LEVEL;
            float amount = 1.0F + level * 0.5F;
            float damage = lethal ? amount : Math.min(amount, Math.max(0.0F, player.getHealth() - 1.0F));
            if (damage > 0) player.hurt(DrugDamage.withdrawal(player), damage);
        }
        int dropInterval = Math.max(10, 30 - level * 5);
        double dropChance = Math.min(1.0, Config.NICOTINE_DROP_CHANCE.get() + level * 0.1);
        if (Config.NICOTINE_DROPS.get() && withdrawal % dropInterval == 0 && player.getRandom().nextDouble() < dropChance)
            dropRandomItem(player);
    }

    public static final int MAX_LEVEL = 4;
    public static final int LETHAL_LEVEL = 2;

    /** 0 at the start of withdrawal, rising by one every escalation interval up to MAX_LEVEL. */
    public static int withdrawalLevel(int withdrawalSeconds) {
        return Math.min(MAX_LEVEL, Math.max(0, withdrawalSeconds) / Config.NICOTINE_ESCALATION_SECONDS.get());
    }

    /** One vape puff. Heavy vaping builds lung damage, which saves with the player and heals very slowly. */
    public static void puff(Player player) {
        if (!Config.POPCORN_LUNG.get() || player.isCreative() || player.isSpectator()) return;
        CompoundTag state = state(player);
        int load = Math.max(0, state.getInt("vapeLoad")) + 1;
        state.putInt("vapeLoad", load);
        state.putInt("vapeDecay", 0);
        int excess = load - Config.VAPE_SAFE_PUFFS.get();
        if (excess > 0) {
            int before = lungDamage(state);
            int after = Math.min(Config.POPCORN_LUNG_FATAL.get() + 5, before + excess);
            state.putInt("lungDamage", after);
            state.putInt("lungHealSeconds", 0);
            if (lungStage(before) != lungStage(after))
                player.displayClientMessage(Component.translatable("message.create_drugs.lung_stage_" + lungStage(after))
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
            else if (after > 0) player.displayClientMessage(Component.translatable("message.create_drugs.lung_warning"), true);
        }
    }

    public static int lungDamage(CompoundTag state) { return Math.max(0, state.getInt("lungDamage")); }

    /** 0 healthy, 1 coughing, 2 short of breath, 3 severe, 4 fatal. */
    public static int lungStage(int damage) {
        int fatal = Config.POPCORN_LUNG_FATAL.get();
        if (damage >= fatal) return 4;
        if (damage >= fatal * 2 / 3) return 3;
        if (damage >= fatal / 3) return 2;
        if (damage >= Math.max(1, fatal / 6)) return 1;
        return 0;
    }

    private static void lungSecond(Player player, CompoundTag state) {
        if (!Config.POPCORN_LUNG.get()) return;
        int load = Math.max(0, state.getInt("vapeLoad"));
        if (load > 0) {
            int decay = state.getInt("vapeDecay") + 1;
            if (decay >= Config.VAPE_LOAD_DECAY_SECONDS.get()) { load--; decay = 0; }
            state.putInt("vapeLoad", load);
            state.putInt("vapeDecay", decay);
        } else if (lungDamage(state) > 0) {
            int heal = state.getInt("lungHealSeconds") + 1;
            if (heal >= Config.LUNG_RECOVERY_SECONDS.get()) { state.putInt("lungDamage", lungDamage(state) - 1); heal = 0; }
            state.putInt("lungHealSeconds", heal);
        }
        int stage = lungStage(lungDamage(state));
        if (stage == 0) return;
        int tick = state.getInt("lungTick") + 1;
        state.putInt("lungTick", tick % 1200);
        if (stage >= 2) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, stage >= 3 ? 1 : 0, false, false, true));
        if (stage >= 2) player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, false, true));
        if (stage >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0, false, false, true));
        }
        int coughInterval = stage == 1 ? 20 : stage == 2 ? 12 : 6;
        if (stage < 4 && tick % coughInterval == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));
            player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 0.8F, 0.6F);
            if (player.level() instanceof net.minecraft.server.level.ServerLevel server)
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, player.getX(), player.getEyeY(), player.getZ(), 4, 0.15, 0.1, 0.15, 0.01);
            if (stage >= 3) {
                float damage = Math.min(1.0F, Math.max(0.0F, player.getHealth() - 1.0F));
                if (damage > 0) player.hurt(DrugDamage.lung(player), damage);
            }
        }
        // Fatal stage: the lungs can no longer supply air, and the damage is not protected by the half-heart floor.
        if (stage == 4 && Config.POPCORN_LUNG_DEATH.get() && tick % 3 == 0) {
            player.hurt(DrugDamage.lung(player), 2.0F);
            if (!player.isAlive()) {
                state.putInt("lungDamage", 0);
                state.putInt("vapeLoad", 0);
            }
        }
    }

    public static boolean dropRandomItem(Player player) {
        if (player.level().isClientSide || player.isCreative() || player.isSpectator()) return false;
        var slots = new ArrayList<Integer>();
        for (int i = 0; i < player.getInventory().items.size(); i++)
            if (!player.getInventory().items.get(i).isEmpty()) slots.add(i);
        if (slots.isEmpty()) return false;
        var stack = player.getInventory().items.get(slots.get(player.getRandom().nextInt(slots.size())));
        var dropped = stack.split(1);
        // Use the standard toss event so protection mods can cancel; restore the item if they do.
        if (player.drop(dropped, false) == null) { stack.grow(1); return false; }
        player.getInventory().setChanged();
        player.displayClientMessage(Component.translatable("message.create_drugs.withdrawal_drop"), true);
        return true;
    }

    /** Clears dependence, escalated withdrawal, lung damage and the abstinence clock on death. An active patch persists. */
    public static void resetOnDeath(Player player) {
        if (!player.hasData(LongTermEffects.HISTORY)) return;
        CompoundTag history = player.getData(LongTermEffects.HISTORY);
        if (!history.contains(KEY)) return;
        CompoundTag state = history.getCompound(KEY);
        for (String key : new String[] {"points", "withdrawalSeconds", "abstinentSeconds", "recoverySeconds", "lungDamage",
                "lungHealSeconds", "lungTick", "vapeLoad", "vapeDecay"})
            state.remove(key);
        player.removeEffect(NicotineContent.WITHDRAWAL);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void onClone(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) {
        if (event.isWasDeath()) resetOnDeath(event.getEntity());
    }

    public static Component status(Player player) {
        CompoundTag state = player.getData(LongTermEffects.HISTORY).getCompound(KEY);
        return Component.translatable("message.create_drugs.nicotine_status", LongTermEffects.points(state),
                state.getInt("abstinentSeconds"), patchRemaining(player), withdrawalLevel(state.getInt("withdrawalSeconds")),
                lungDamage(state), Config.POPCORN_LUNG_FATAL.get());
    }

    private NicotineSystem() {}
}
