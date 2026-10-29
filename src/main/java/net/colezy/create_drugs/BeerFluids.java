package net.colezy.create_drugs;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;

public final class BeerFluids {
    public static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Create_drugs.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Create_drugs.MODID);
    public static final DeferredHolder<FluidType, FluidType> BEER_TYPE = TYPES.register("beer",
            () -> new FluidType(FluidType.Properties.create().density(1000).viscosity(1000)));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> BEER = FLUIDS.register("beer", () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_BEER = FLUIDS.register("flowing_beer", () -> new BaseFlowingFluid.Flowing(properties()));
    public static final DeferredBlock<LiquidBlock> BEER_BLOCK = Create_drugs.BLOCKS.register("beer",
            () -> new LiquidBlock(BEER.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredItem<BucketItem> BEER_BUCKET = Create_drugs.ITEMS.register("beer_bucket",
            () -> new BucketItem(BEER.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(BEER_TYPE, BEER, FLOWING_BEER).block(BEER_BLOCK).bucket(BEER_BUCKET);
    }

    public static void register(IEventBus bus) { TYPES.register(bus); FLUIDS.register(bus); }
    private BeerFluids() {}
}
