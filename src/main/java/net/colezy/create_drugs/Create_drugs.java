package net.colezy.create_drugs;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.*;

@Mod(Create_drugs.MODID)
public class Create_drugs {
    public static final String MODID = "create_drugs";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<MobEffect, MobEffect> INTOXICATION = EFFECTS.register("intoxication",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x9872BC) {});
    public static final DeferredHolder<SoundEvent, SoundEvent> CONSUME = SOUNDS.register("consume",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "consume")));
    public static final DeferredBlock<DrugCropBlock> CANNABIS_CROP = BLOCKS.register("cannabis_crop",
            () -> new DrugCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT), () -> Create_drugs.CANNABIS_SEEDS.get()));
    public static final DeferredBlock<DrugCropBlock> COCA_CROP = BLOCKS.register("coca_crop",
            () -> new DrugCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT), () -> Create_drugs.COCA_SEEDS.get()));
    public static final DeferredItem<ItemNameBlockItem> CANNABIS_SEEDS = ITEMS.register("cannabis_seeds",
            () -> new ItemNameBlockItem(CANNABIS_CROP.get(), new Item.Properties()));
    public static final DeferredItem<ItemNameBlockItem> COCA_SEEDS = ITEMS.register("coca_seeds",
            () -> new ItemNameBlockItem(COCA_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> CANNABIS_LEAF = ITEMS.registerSimpleItem("cannabis_leaf");
    public static final DeferredItem<Item> COCA_LEAF = ITEMS.registerSimpleItem("coca_leaf");
    public static final DeferredItem<Item> DRIED_CANNABIS = ITEMS.registerSimpleItem("dried_cannabis");
    public static final DeferredItem<Item> COCA_DUST = ITEMS.registerSimpleItem("coca_dust");
    public static final DeferredItem<Item> CRYSTAL_BLEND = ITEMS.registerSimpleItem("crystal_blend");
    public static final DeferredItem<Item> BLOTTER_PAPER = ITEMS.registerSimpleItem("blotter_paper");
    public static final DeferredItem<DrugItem> JOINT = ITEMS.register("joint", () -> new DrugItem(
            "joint", MobEffects.DAMAGE_RESISTANCE, 0, MobEffects.MOVEMENT_SLOWDOWN, 0));
    public static final DeferredItem<DrugItem> COCAINE = ITEMS.register("cocaine", () -> new DrugItem(
            "cocaine", MobEffects.MOVEMENT_SPEED, 1, MobEffects.HUNGER, 1));
    public static final DeferredItem<DrugItem> METHAMPHETAMINE = ITEMS.register("methamphetamine", () -> new DrugItem(
            "methamphetamine", MobEffects.DIG_SPEED, 1, MobEffects.WEAKNESS, 0));
    public static final DeferredItem<DrugItem> LSD = ITEMS.register("lsd", () -> new DrugItem(
            "lsd", MobEffects.NIGHT_VISION, 0, MobEffects.CONFUSION, 0));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () ->
            CreativeModeTab.builder().title(Component.translatable("itemGroup.create_drugs"))
                    .icon(() -> JOINT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());
    public Create_drugs(IEventBus bus, ModContainer container) {
        NicotineContent.init();
        CocaineBlock.init();
        AlcoholContent.init();
        AlcoholContent.register(bus);
        BeerFluids.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        EFFECTS.register(bus);
        SOUNDS.register(bus);
        TABS.register(bus);
        LongTermEffects.ATTACHMENTS.register(bus);
        DrugVillagers.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }
}
