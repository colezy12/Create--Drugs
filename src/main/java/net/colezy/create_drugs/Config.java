package net.colezy.create_drugs;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue COOLDOWN_SECONDS = BUILDER
            .comment("Per-item cooldown after using a substance, in seconds. Each item has its own timer; 0 disables.")
            .defineInRange("cooldownSeconds", 3, 0, 120);
    public static final ModConfigSpec.IntValue EFFECT_SECONDS = BUILDER
            .comment("Duration of each consumable's benefit and drawback, in seconds.")
            .defineInRange("effectSeconds", 45, 1, 600);
    public static final ModConfigSpec.IntValue INTOXICATION_SECONDS = BUILDER
            .comment("Time without consuming needed for accumulated intoxication to clear.")
            .defineInRange("intoxicationSeconds", 120, 1, 1200);
    public static final ModConfigSpec.BooleanValue OVERUSE_PENALTIES = BUILDER
            .comment("Three or more uses before intoxication clears cause poison, nausea and weakness.")
            .define("overusePenalties", true);
    public static final ModConfigSpec.BooleanValue LONG_TERM_EFFECTS = BUILDER
            .comment("Enable persistent per-substance tolerance, dependence and withdrawal. Disabling pauses saved progress.")
            .define("longTermEffects", true);
    public static final ModConfigSpec.IntValue DEPENDENCE_PER_USE = BUILDER
            .defineInRange("dependencePerUse", 10, 1, 50);
    public static final ModConfigSpec.IntValue WITHDRAWAL_DELAY = BUILDER
            .comment("Online seconds after the last dose before withdrawal and recovery begin.")
            .defineInRange("withdrawalDelaySeconds", 180, 10, 3600);
    public static final ModConfigSpec.IntValue RECOVERY_SECONDS = BUILDER
            .comment("Online seconds of abstinence per point recovered after the withdrawal delay.")
            .defineInRange("recoverySecondsPerPoint", 30, 1, 3600);
    public static final ModConfigSpec.IntValue NICOTINE_DELAY = BUILDER
            .comment("Online seconds without nicotine before dependence can cause withdrawal.")
            .defineInRange("nicotineWithdrawalDelaySeconds", 120, 10, 3600);
    public static final ModConfigSpec.IntValue NICOTINE_THRESHOLD = BUILDER
            .defineInRange("nicotineDependenceThreshold", 20, 1, 100);
    public static final ModConfigSpec.IntValue PATCH_SECONDS = BUILDER
            .comment("One applied patch supplies nicotine for this many online seconds; patches do not stack.")
            .defineInRange("nicotinePatchSeconds", 600, 10, 7200);
    public static final ModConfigSpec.BooleanValue NICOTINE_DAMAGE = BUILDER
            .comment("Withdrawal deals half a heart every 10 seconds, but cannot reduce health below half a heart.")
            .define("nicotineWithdrawalDamage", true);
    public static final ModConfigSpec.BooleanValue NICOTINE_DISORIENTATION = BUILDER
            .comment("Withdrawal causes nausea, occasional darkness and unsettling sounds.")
            .define("nicotineWithdrawalDisorientation", true);
    public static final ModConfigSpec.BooleanValue NICOTINE_DROPS = BUILDER
            .comment("Withdrawal can drop one random main-inventory item every 30 seconds. Armor and offhand are excluded.")
            .define("nicotineWithdrawalItemDrops", true);
    public static final ModConfigSpec.DoubleValue NICOTINE_DROP_CHANCE = BUILDER
            .defineInRange("nicotineDropChance", 0.25, 0.0, 1.0);
    public static final ModConfigSpec.IntValue DRUNK_SECONDS = BUILDER
            .comment("How long each beer keeps you drunk, in seconds. Drunk level rises by one per beer (max 5).")
            .defineInRange("drunkSeconds", 180, 10, 3600);
    public static final ModConfigSpec.DoubleValue BOTTLE_INACCURACY = BUILDER
            .comment("Sober aim spread of a thrown empty beer bottle.")
            .defineInRange("bottleInaccuracy", 6.0, 0.0, 30.0);
    public static final ModConfigSpec.BooleanValue DRUNK_AIM_IMPROVES = BUILDER
            .comment("true: the drunker the thrower, the more accurate the throw. false: drunker means less accurate.")
            .define("drunkAimImproves", true);
    public static final ModConfigSpec.DoubleValue BOTTLE_DAMAGE = BUILDER
            .defineInRange("bottleDamage", 2.0, 0.0, 20.0);
    public static final ModConfigSpec.IntValue NICOTINE_ESCALATION_SECONDS = BUILDER
            .comment("Withdrawal gets one level worse (up to 4) every this many online seconds without nicotine.")
            .defineInRange("nicotineEscalationSeconds", 120, 10, 3600);
    public static final ModConfigSpec.BooleanValue NICOTINE_LETHAL = BUILDER
            .comment("From withdrawal level 2, withdrawal damage can kill (no half-heart floor).")
            .define("nicotineWithdrawalLethal", true);
    public static final ModConfigSpec.BooleanValue POPCORN_LUNG = BUILDER
            .comment("Heavy vaping causes saved lung damage (popcorn lung).")
            .define("popcornLung", true);
    public static final ModConfigSpec.IntValue VAPE_SAFE_PUFFS = BUILDER
            .comment("Recent puffs allowed before each further puff damages the lungs.")
            .defineInRange("vapeSafePuffs", 6, 1, 32);
    public static final ModConfigSpec.IntValue VAPE_LOAD_DECAY_SECONDS = BUILDER
            .comment("Online seconds for one recent puff to stop counting.")
            .defineInRange("vapeLoadDecaySeconds", 15, 1, 600);
    public static final ModConfigSpec.IntValue POPCORN_LUNG_FATAL = BUILDER
            .comment("Lung damage at which the lungs fail. Lower stages begin at 1/6, 1/3 and 2/3 of this.")
            .defineInRange("popcornLungFatalDamage", 30, 6, 200);
    public static final ModConfigSpec.BooleanValue POPCORN_LUNG_DEATH = BUILDER
            .comment("Allow fatal lung failure to kill the player (drowning damage, ignores the half-heart floor).")
            .define("popcornLungDeath", true);
    public static final ModConfigSpec.IntValue LUNG_RECOVERY_SECONDS = BUILDER
            .comment("Online seconds without recent vaping to heal one point of lung damage.")
            .defineInRange("lungRecoverySecondsPerPoint", 300, 1, 7200);
    public static final ModConfigSpec SPEC = BUILDER.build();
    private Config() {}
}
