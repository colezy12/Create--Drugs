package net.colezy.create_drugs;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class BeerItem extends DrugItem {
    public BeerItem() { super("beer", MobEffects.DAMAGE_RESISTANCE, 0, MobEffects.MOVEMENT_SLOWDOWN, 0); }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        int before = stack.getCount();
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && stack.getCount() < before) {
            AlcoholContent.drink(entity);
            ItemStack bottle = new ItemStack(AlcoholContent.EMPTY_BEER_BOTTLE.get());
            if (result.isEmpty()) return bottle;
            if (entity instanceof Player player) {
                if (!player.getInventory().add(bottle)) player.drop(bottle, false);
            } else entity.spawnAtLocation(bottle);
        }
        return result;
    }
}
