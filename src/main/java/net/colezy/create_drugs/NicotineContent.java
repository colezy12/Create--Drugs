package net.colezy.create_drugs;

import net.minecraft.resources.ResourceLocation;
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
    public static final DeferredHolder<SoundEvent, SoundEvent> INHALE = Create_drugs.SOUNDS.register("inhale",
            () -> SoundEvent.createVariableRangeEvent(id("inhale")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WITHDRAWAL_SOUND = Create_drugs.SOUNDS.register("withdrawal",
            () -> SoundEvent.createVariableRangeEvent(id("withdrawal")));

    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(Create_drugs.MODID, name); }
    public static void init() {}
    private NicotineContent() {}
}
