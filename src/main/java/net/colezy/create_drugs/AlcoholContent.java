package net.colezy.create_drugs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AlcoholContent {
    public static final int MAX_DRUNK_LEVEL = 5;
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Create_drugs.MODID);
    public static final DeferredHolder<MobEffect, MobEffect> DRUNK = Create_drugs.EFFECTS.register("drunk",
            () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xD59A32) {});
    public static final DeferredItem<EmptyBeerBottleItem> EMPTY_BEER_BOTTLE = Create_drugs.ITEMS.register("empty_beer_bottle",
            EmptyBeerBottleItem::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownBeerBottle>> THROWN_BOTTLE = ENTITIES.register("thrown_beer_bottle",
            () -> EntityType.Builder.<ThrownBeerBottle>of(ThrownBeerBottle::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(Create_drugs.MODID, "thrown_beer_bottle").toString()));

    /** 0 when sober, otherwise 1..MAX_DRUNK_LEVEL. */
    public static int drunkLevel(LivingEntity entity) {
        MobEffectInstance effect = entity.getEffect(DRUNK);
        return effect == null ? 0 : Math.min(effect.getAmplifier() + 1, MAX_DRUNK_LEVEL);
    }

    /** Each beer raises the drunk level by one and refreshes its duration. */
    public static void drink(LivingEntity entity) {
        int next = Math.min(drunkLevel(entity), MAX_DRUNK_LEVEL - 1);
        int ticks = Config.DRUNK_SECONDS.get() * 20;
        entity.addEffect(new MobEffectInstance(DRUNK, ticks, next));
        if (next >= 2) entity.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 100 * next, 0));
        if (next >= 4) entity.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, ticks / 2, 1));
    }

    public static float inaccuracy(LivingEntity thrower) {
        float base = Config.BOTTLE_INACCURACY.get().floatValue();
        float drunk = drunkLevel(thrower) / (float) MAX_DRUNK_LEVEL;
        return Config.DRUNK_AIM_IMPROVES.get() ? base * (1.0F - drunk) : base * (1.0F + drunk * 2.0F);
    }

    public static void register(IEventBus bus) { ENTITIES.register(bus); }
    public static void init() {}
    private AlcoholContent() {}
}
