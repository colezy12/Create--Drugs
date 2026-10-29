package net.colezy.create_drugs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Create_drugs.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientFluidEvents {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(BeerFluids.BEER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(BeerFluids.FLOWING_BEER.get(), RenderType.translucent());
        });
    }
    @SubscribeEvent
    public static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AlcoholContent.THROWN_BOTTLE.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
    }
    @SubscribeEvent
    public static void fluids(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override public ResourceLocation getStillTexture() { return ResourceLocation.withDefaultNamespace("block/water_still"); }
            @Override public ResourceLocation getFlowingTexture() { return ResourceLocation.withDefaultNamespace("block/water_flow"); }
            @Override public int getTintColor() { return 0xFFD59A32; }
        }, BeerFluids.BEER_TYPE);
    }
    private ClientFluidEvents() {}
}
