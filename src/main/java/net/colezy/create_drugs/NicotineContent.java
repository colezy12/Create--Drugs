package net.colezy.create_drugs;

import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class NicotineContent {
    public static final DeferredBlock<DrugCropBlock> TOBACCO_CROP = Create_drugs.BLOCKS.register("tobacco_crop",
            () -> new DrugCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT), () -> NicotineContent.TOBACCO_SEEDS.get()));
    public static final DeferredItem<ItemNameBlockItem> TOBACCO_SEEDS = Create_drugs.ITEMS.register("tobacco_seeds",
            () -> new ItemNameBlockItem(TOBACCO_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> TOBACCO_LEAF = Create_drugs.ITEMS.registerSimpleItem("tobacco_leaf");
    public static final DeferredItem<Item> DRIED_TOBACCO = Create_drugs.ITEMS.registerSimpleItem("dried_tobacco");
    public static final DeferredItem<Item> NICOTINE_BLEND = Create_drugs.ITEMS.registerSimpleItem("nicotine_blend");
    public static final DeferredItem<Item> VAPE_CARTRIDGE = Create_drugs.ITEMS.registerSimpleItem("vape_cartridge");
    public static final DeferredItem<Item> MALT = Create_drugs.ITEMS.registerSimpleItem("malt");
    public static final DeferredItem<NicotineItem> VAPE = Create_drugs.ITEMS.register("vape", () -> new NicotineItem(true));
    public static final DeferredItem<NicotineItem> CIGAR = Create_drugs.ITEMS.register("cigar", () -> new NicotineItem(false));
    public static final DeferredItem<NicotinePatchItem> PATCH = Create_drugs.ITEMS.register("nicotine_patch", NicotinePatchItem::new);
    public static final DeferredItem<BeerItem> BEER = Create_drugs.ITEMS.register("beer", BeerItem::new);

    public static final DeferredHolder<MobEffect, MobEffect> WITHDRAWAL = Create_drugs.EFFECTS.register("nicotine_withdrawal",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x9A8856) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, id("withdrawal_speed"), -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<MobEffect, MobEffect> PATCH_ACTIVE = Create_drugs.EFFECTS.register("nicotine_patch",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xE7D5AE) {});
    public static final int POTION_BASE_DEPENDENCE = 10;
    public static final int POTION_STEP = 5;
    public static final int POTION_MAX_DEPENDENCE = 50;
    public static final int POTION_TIERS = (POTION_MAX_DEPENDENCE - POTION_BASE_DEPENDENCE) / POTION_STEP + 1;

    public static final DeferredHolder<MobEffect, MobEffect> NICOTINE = Create_drugs.EFFECTS.register("nicotine",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xC9A66B) {
                @Override public boolean isInstantenous() { return true; }
                @Override
                public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entity, int amplifier, double health) {
                    if (entity instanceof Player player && !player.level().isClientSide) {
                        NicotineSystem.dose(player, Math.min(POTION_MAX_DEPENDENCE, POTION_BASE_DEPENDENCE + POTION_STEP * amplifier));
                    }
                }
            });
    /** Index i grants 10 + 5 * i dependence; brewing another blend moves to the next tier. */
    public static final List<DeferredHolder<Potion, Potion>> POTIONS = IntStream.range(0, POTION_TIERS)
            .mapToObj(i -> Create_drugs.POTIONS.register("nicotine_" + (POTION_BASE_DEPENDENCE + POTION_STEP * i),
                    () -> new Potion(new MobEffectInstance(NICOTINE, 1, i))))
            .toList();

    public static void registerBrewing(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, NICOTINE_BLEND.get(), POTIONS.get(0));
        for (int i = 0; i < POTIONS.size() - 1; i++) builder.addMix(POTIONS.get(i), NICOTINE_BLEND.get(), POTIONS.get(i + 1));
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> INHALE = Create_drugs.SOUNDS.register("inhale",
            () -> SoundEvent.createVariableRangeEvent(id("inhale")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WITHDRAWAL_SOUND = Create_drugs.SOUNDS.register("withdrawal",
            () -> SoundEvent.createVariableRangeEvent(id("withdrawal")));

    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(Create_drugs.MODID, name); }
    public static void init() {}
    private NicotineContent() {}
}
