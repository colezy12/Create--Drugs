package net.colezy.create_drugs;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.*;

@EventBusSubscriber(modid = Create_drugs.MODID)
public final class DrugVillagers {
    public static final DeferredRegister<PoiType> POIS = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Create_drugs.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Create_drugs.MODID);
    public static final DeferredBlock<Block> HERBALIST_TABLE = Create_drugs.BLOCKS.registerSimpleBlock("herbalist_table",
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
    public static final DeferredBlock<Block> DEALER_COUNTER = Create_drugs.BLOCKS.registerSimpleBlock("dealer_counter",
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
    public static final DeferredHolder<PoiType, PoiType> HERBALIST_POI = POIS.register("herbalist",
            () -> new PoiType(ImmutableSet.copyOf(HERBALIST_TABLE.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> DEALER_POI = POIS.register("dealer",
            () -> new PoiType(ImmutableSet.copyOf(DEALER_COUNTER.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<VillagerProfession, VillagerProfession> HERBALIST = PROFESSIONS.register("herbalist",
            () -> profession("herbalist", HERBALIST_POI));
    public static final DeferredHolder<VillagerProfession, VillagerProfession> DEALER = PROFESSIONS.register("dealer",
            () -> profession("dealer", DEALER_POI));

    private static VillagerProfession profession(String name, DeferredHolder<PoiType, PoiType> poi) {
        return new VillagerProfession(Create_drugs.MODID + ":" + name,
                holder -> holder.is(poi.getKey()), holder -> holder.is(poi.getKey()),
                ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CLERIC);
    }

    public static void register(IEventBus bus) {
        Create_drugs.ITEMS.registerSimpleBlockItem(HERBALIST_TABLE);
        Create_drugs.ITEMS.registerSimpleBlockItem(DEALER_COUNTER);
        POIS.register(bus);
        PROFESSIONS.register(bus);
    }

    @SubscribeEvent
    public static void trades(VillagerTradesEvent event) {
        if (event.getType() == HERBALIST.get()) {
            add(event, 1, buy(Create_drugs.CANNABIS_LEAF.get(), 16, 1, 2), sell(Create_drugs.CANNABIS_SEEDS.get(), 4, 2, 2));
            add(event, 2, buy(Create_drugs.COCA_LEAF.get(), 16, 1, 5), sell(Create_drugs.COCA_SEEDS.get(), 4, 2, 5));
            add(event, 3, buy(Create_drugs.DRIED_CANNABIS.get(), 8, 1, 10), sell(Create_drugs.BLOTTER_PAPER.get(), 4, 2, 10));
            add(event, 4, buy(Create_drugs.COCA_DUST.get(), 12, 2, 15), sell(Create_drugs.DRIED_CANNABIS.get(), 4, 3, 15));
            add(event, 5, buy(Create_drugs.JOINT.get(), 4, 2, 30), sell(Create_drugs.JOINT.get(), 1, 4, 30));
        }
        if (event.getType() == DEALER.get()) {
            add(event, 1, buy(Create_drugs.JOINT.get(), 4, 2, 2), sell(Create_drugs.JOINT.get(), 1, 4, 2));
            add(event, 2, buy(Create_drugs.COCAINE.get(), 4, 3, 5), sell(Create_drugs.COCAINE.get(), 1, 6, 5));
            add(event, 3, buy(Create_drugs.METHAMPHETAMINE.get(), 4, 4, 10), sell(Create_drugs.METHAMPHETAMINE.get(), 1, 8, 10));
            add(event, 4, buy(Create_drugs.LSD.get(), 4, 3, 15), sell(Create_drugs.LSD.get(), 1, 6, 15));
            add(event, 5, buy(Create_drugs.CRYSTAL_BLEND.get(), 8, 3, 30), sell(Create_drugs.CRYSTAL_BLEND.get(), 2, 5, 30));
        }
    }

    private static void add(VillagerTradesEvent event, int level, VillagerTrades.ItemListing buy, VillagerTrades.ItemListing sell) {
        event.getTrades().get(level).add(buy);
        event.getTrades().get(level).add(sell);
    }

    private static VillagerTrades.ItemListing buy(Item item, int count, int emeralds, int xp) {
        return (entity, random) -> new MerchantOffer(new ItemCost(item, count), new ItemStack(Items.EMERALD, emeralds), 12, xp, 0.05F);
    }

    private static VillagerTrades.ItemListing sell(Item item, int count, int emeralds, int xp) {
        return (entity, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), new ItemStack(item, count), 12, xp, 0.05F);
    }

    private DrugVillagers() {}
}
