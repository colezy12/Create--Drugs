package net.colezy.create_drugs;
import java.util.function.Supplier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
/** Vanilla crop behaviour permits harvesting by Create's mechanical harvester. */
public class DrugCropBlock extends CropBlock {
    private final Supplier<? extends ItemLike> seed;
    public DrugCropBlock(Properties properties, Supplier<? extends ItemLike> seed) {
        super(properties);
        this.seed = seed;
    }
    @Override
    protected ItemLike getBaseSeedId() { return seed.get(); }
}
