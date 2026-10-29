package net.colezy.create_drugs;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownBeerBottle extends ThrowableItemProjectile {
    public ThrownBeerBottle(EntityType<? extends ThrownBeerBottle> type, Level level) { super(type, level); }
    public ThrownBeerBottle(Level level, LivingEntity owner) { super(AlcoholContent.THROWN_BOTTLE.get(), owner, level); }

    @Override
    protected Item getDefaultItem() { return AlcoholContent.EMPTY_BEER_BOTTLE.get(); }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(DrugDamage.bottle(this, getOwner()), Config.BOTTLE_DAMAGE.get().floatValue());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            discard();
        }
    }
}
