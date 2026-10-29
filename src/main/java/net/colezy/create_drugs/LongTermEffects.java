package net.colezy.create_drugs;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Saved in player data; NeoForge copies this attachment when a player respawns. */
@EventBusSubscriber(modid = Create_drugs.MODID)
public final class LongTermEffects {
    public static final String[] SUBSTANCES = {"beer"};
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Create_drugs.MODID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> HISTORY =
            ATTACHMENTS.register("drug_history", () -> AttachmentType.builder(() -> new CompoundTag())
                    .serialize(CompoundTag.CODEC).copyOnDeath().build());

    public static int onUse(Player player, String substance, int duration) {
        if (!Config.LONG_TERM_EFFECTS.get() || player.isCreative() || player.isSpectator()
                || !java.util.Arrays.asList(SUBSTANCES).contains(substance)) return duration;
        CompoundTag history = player.getData(HISTORY);
        CompoundTag state = history.getCompound(substance);
        int points = points(state);
        // Tolerance affects only benefits; drawbacks retain their full duration.
        int adjusted = Math.max(20, duration * (100 - points / 2) / 100);
        state.putInt("points", Math.min(100, points + Config.DEPENDENCE_PER_USE.get()));
        state.putInt("abstinentSeconds", 0);
        state.putInt("recoverySeconds", 0);
        history.put(substance, state);
        player.displayClientMessage(Component.translatable("message.create_drugs.dependence",
                Component.translatable("item.create_drugs." + substance), points(state)), true);
        return adjusted;
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.isAlive() && player.tickCount % 20 == 0) advanceSecond(player);
    }

    public static void advanceSecond(Player player) {
        NicotineSystem.advanceSecond(player);
        if (!Config.LONG_TERM_EFFECTS.get() || player.isCreative() || player.isSpectator() || !player.hasData(HISTORY)) return;
        CompoundTag history = player.getData(HISTORY);
        int worstWithdrawal = 0;
        for (String substance : SUBSTANCES) {
            CompoundTag state = history.getCompound(substance);
            int points = points(state);
            if (points == 0) { history.remove(substance); continue; }
            int abstinence = Math.min(86400, Math.max(0, state.getInt("abstinentSeconds")) + 1);
            state.putInt("abstinentSeconds", abstinence);
            if (abstinence >= Config.WITHDRAWAL_DELAY.get()) {
                int recovery = Math.max(0, state.getInt("recoverySeconds")) + 1;
                if (recovery >= Config.RECOVERY_SECONDS.get()) { points--; recovery = 0; }
                state.putInt("recoverySeconds", recovery);
                state.putInt("points", points);
                worstWithdrawal = Math.max(worstWithdrawal, points);
            }
            history.put(substance, state);
        }
        // Short refreshes restore withdrawal after death or milk without permanently stacking attributes.
        if (worstWithdrawal >= 30) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, false, true));
        }
        if (worstWithdrawal >= 60)
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false, true));
        if (worstWithdrawal >= 90)
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0, false, false, true));
    }

    /** Death clears saved dependence for every substance except nicotine, which NicotineSystem handles itself. */
    public static void resetOnDeath(Player player) {
        if (!player.hasData(HISTORY)) return;
        CompoundTag history = player.getData(HISTORY);
        for (String substance : SUBSTANCES) history.remove(substance);
    }

    @SubscribeEvent
    public static void onClone(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) {
        if (event.isWasDeath()) resetOnDeath(event.getEntity());
    }

    public static int points(CompoundTag state) { return Math.clamp(state.getInt("points"), 0, 100); }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("drugstatus").executes(context -> {
            var player = context.getSource().getPlayerOrException();
            var history = player.getData(HISTORY);
            for (String substance : SUBSTANCES) {
                var state = history.getCompound(substance);
                context.getSource().sendSuccess(() -> Component.translatable("message.create_drugs.status",
                        Component.translatable("item.create_drugs." + substance), points(state),
                        state.getInt("abstinentSeconds")), false);
            }
            context.getSource().sendSuccess(() -> NicotineSystem.status(player), false);
            return 1;
        }));
        event.getDispatcher().register(Commands.literal("drugreset").requires(source -> source.hasPermission(2))
                .then(Commands.argument("players", EntityArgument.players()).executes(context -> {
                    var players = EntityArgument.getPlayers(context, "players");
                    for (var player : players) {
                        player.setData(HISTORY, new CompoundTag());
                        player.removeEffect(NicotineContent.WITHDRAWAL);
                        player.removeEffect(NicotineContent.PATCH_ACTIVE);
                    }
                    context.getSource().sendSuccess(() -> Component.translatable("message.create_drugs.reset", players.size()), true);
                    return players.size();
                })));
    }

    private LongTermEffects() {}
}
