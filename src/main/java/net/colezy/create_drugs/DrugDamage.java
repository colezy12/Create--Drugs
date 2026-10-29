package net.colezy.create_drugs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Custom damage types; their death messages live in the lang file as death.attack.create_drugs.*. */
public final class DrugDamage {
    public static final int WITHDRAWAL_VARIANTS = 3;
    public static final int LUNG_VARIANTS = 3;

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Create_drugs.MODID, name));
    }

    private static DamageSource of(Level level, String name, Entity direct, Entity owner) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key(name)), direct, owner);
    }

    public static DamageSource withdrawal(Player player) {
        return of(player.level(), "withdrawal_" + (1 + player.getRandom().nextInt(WITHDRAWAL_VARIANTS)), null, null);
    }

    public static DamageSource lung(Player player) {
        return of(player.level(), "popcorn_lung_" + (1 + player.getRandom().nextInt(LUNG_VARIANTS)), null, null);
    }

    public static DamageSource bottle(Entity bottle, Entity thrower) {
        return of(bottle.level(), "beer_bottle", bottle, thrower);
    }

    private DrugDamage() {}
}
