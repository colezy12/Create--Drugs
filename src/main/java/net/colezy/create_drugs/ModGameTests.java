package net.colezy.create_drugs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Create_drugs.MODID)
@PrefixGameTestTemplate(false)
public class ModGameTests {
    @GameTest(template = "empty")
    public static void recipesLoadWithCreate(GameTestHelper helper) {
        String[] names = {"press_cocaine_block", "cannabis_seeds", "coca_seeds", "joint", "dry_cannabis", "mill_coca",
                "crush_coca", "mix_cocaine", "mix_crystal_blend", "press_methamphetamine",
                "press_blotter_paper", "mix_lsd", "deploy_joint", "herbalist_table", "dealer_counter",
                "tobacco_seeds", "dry_tobacco", "mix_nicotine_blend", "deploy_cigar", "mix_vape_cartridge",
                "deploy_nicotine_patch", "refill_vape", "mechanical_crafting_vape", "press_malt", "fill_beer_bottle"};
        for (String name : names) {
            var recipe = helper.getLevel().getRecipeManager().byKey(id(name));
            helper.assertTrue(recipe.isPresent(), "Missing or invalid recipe: " + name);
            helper.assertTrue(!recipe.orElseThrow().value().getResultItem(helper.getLevel().registryAccess()).isEmpty(),
                    "Recipe has no output: " + name);
        }
        var recipe = helper.getLevel().getRecipeManager().byKey(id("mix_cocaine")).orElseThrow().value();
        helper.assertTrue(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()).toString().equals("create:mixing"),
                "Cocaine must use Create's actual mixing recipe type");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void survivalUseAndPerItemCooldown(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(Create_drugs.COCAINE.get(), 2);
        Create_drugs.COCAINE.get().finishUsingItem(stack, helper.getLevel(), player);
        helper.assertTrue(stack.getCount() == 1, "One dose must be consumed");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED) && player.hasEffect(MobEffects.HUNGER),
                "Benefit and drawback must both apply");
        helper.assertTrue(player.getCooldowns().isOnCooldown(Create_drugs.COCAINE.get()), "Used drug must have a cooldown");
        helper.assertTrue(!player.getCooldowns().isOnCooldown(Create_drugs.JOINT.get()), "Cooldown must not be global");
        Create_drugs.COCAINE.get().finishUsingItem(stack, helper.getLevel(), player);
        helper.assertTrue(stack.getCount() == 1, "Cooldown must block another dose of the same drug");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mixedOveruseAndCreativeInventory(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        // Minecraft's mock overrides isCreative but does not initialize ability flags.
        GameType.CREATIVE.updatePlayerAbilities(player.getAbilities());
        DrugItem[] drugs = {Create_drugs.JOINT.get(), Create_drugs.COCAINE.get(), Create_drugs.METHAMPHETAMINE.get()};
        for (DrugItem item : drugs) {
            player.getCooldowns().removeCooldown(item);
            var stack = new ItemStack(item, 2);
            item.finishUsingItem(stack, helper.getLevel(), player);
            helper.assertTrue(stack.getCount() == 2, "Creative use must preserve inventory");
        }
        helper.assertTrue(player.hasEffect(MobEffects.POISON), "Third mixed dose must trigger overuse");
        helper.assertTrue(player.getEffect(Create_drugs.INTOXICATION).getAmplifier() == 2, "Intoxication must accumulate");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cropDropsAndReplanting(GameTestHelper helper) {
        DrugCropBlock[] crops = {Create_drugs.CANNABIS_CROP.get(), Create_drugs.COCA_CROP.get(), NicotineContent.TOBACCO_CROP.get()};
        var seeds = new net.minecraft.world.item.Item[] {Create_drugs.CANNABIS_SEEDS.get(), Create_drugs.COCA_SEEDS.get(), NicotineContent.TOBACCO_SEEDS.get()};
        var leaves = new net.minecraft.world.item.Item[] {Create_drugs.CANNABIS_LEAF.get(), Create_drugs.COCA_LEAF.get(), NicotineContent.TOBACCO_LEAF.get()};
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
        for (int i = 0; i < crops.length; i++) {
            var crop = crops[i];
            var mature = crop.getStateForAge(crop.getMaxAge());
            helper.getLevel().setBlockAndUpdate(pos, mature);
            var harvester = new com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour();
            helper.assertTrue(harvester.isValidCrop(helper.getLevel(), pos, mature), "Create must recognize mature crop");
            var drops = Block.getDrops(mature, helper.getLevel(), pos, null);
            final int index = i;
            helper.assertTrue(drops.stream().anyMatch(s -> s.is(seeds[index])), "Mature crop must return a seed");
            helper.assertTrue(drops.stream().anyMatch(s -> s.is(leaves[index]) && s.getCount() == 2), "Mature crop must yield leaves");
            var young = crop.getStateForAge(0);
            helper.assertTrue(!harvester.isValidCrop(helper.getLevel(), pos, young), "Create must leave immature crops growing");
            helper.assertTrue(young.canSurvive(helper.getLevel(), pos), "Crop must replant on farmland");
            var youngDrops = Block.getDrops(young, helper.getLevel(), pos, null);
            helper.assertTrue(youngDrops.stream().noneMatch(s -> s.is(leaves[index])), "Immature crop must not yield leaves");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void historySurvivesSaveAndDeath(GameTestHelper helper) {
        var original = helper.makeMockPlayer(GameType.SURVIVAL);
        LongTermEffects.onUse(original, "beer", 900);
        LongTermEffects.onUse(original, "lsd", 900);
        var saved = new net.minecraft.nbt.CompoundTag();
        original.saveWithoutId(saved);
        var reloaded = helper.makeMockPlayer(GameType.SURVIVAL);
        reloaded.load(saved);
        helper.assertTrue(reloaded.getData(LongTermEffects.HISTORY).equals(original.getData(LongTermEffects.HISTORY)),
                "Player NBT save/load must preserve substance history");
        var respawned = helper.makeMockPlayer(GameType.SURVIVAL);
        respawned.copyAttachmentsFrom(reloaded, true);
        helper.assertTrue(respawned.getData(LongTermEffects.HISTORY).equals(reloaded.getData(LongTermEffects.HISTORY)),
                "Death cloning must preserve substance history");
        LongTermEffects.onUse(respawned, "beer", 900);
        helper.assertTrue(!respawned.getData(LongTermEffects.HISTORY).equals(reloaded.getData(LongTermEffects.HISTORY)),
                "Respawned history must be an independent copy");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void toleranceWithdrawalAndRecovery(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(LongTermEffects.onUse(player, "beer", 900) == 900, "First use has full benefit duration");
        helper.assertTrue(LongTermEffects.onUse(player, "beer", 900) < 900, "Repeated use builds tolerance");
        helper.assertTrue(LongTermEffects.onUse(player, "lsd", 900) == 900, "Non-dependence drugs never build tolerance");
        var history = player.getData(LongTermEffects.HISTORY);
        var state = history.getCompound("beer");
        state.putInt("points", 65);
        state.putInt("abstinentSeconds", Config.WITHDRAWAL_DELAY.get());
        state.putInt("recoverySeconds", 0);
        LongTermEffects.advanceSecond(player);
        helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS) && player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                "High dependence must produce withdrawal");
        player.removeAllEffects();
        LongTermEffects.advanceSecond(player);
        helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS), "Milk must not erase saved dependence");
        var respawned = helper.makeMockPlayer(GameType.SURVIVAL);
        respawned.copyAttachmentsFrom(player, true);
        LongTermEffects.advanceSecond(respawned);
        helper.assertTrue(respawned.hasEffect(MobEffects.WEAKNESS), "Withdrawal must return after death");
        state.putInt("recoverySeconds", Config.RECOVERY_SECONDS.get() - 1);
        LongTermEffects.advanceSecond(player);
        helper.assertTrue(state.getInt("points") == 64, "Abstinence must reduce dependence");
        LongTermEffects.onUse(player, "beer", 900);
        helper.assertTrue(state.getInt("abstinentSeconds") == 0, "New dose resets abstinence clock");
        state.putInt("points", 1);
        state.putInt("abstinentSeconds", Config.WITHDRAWAL_DELAY.get());
        state.putInt("recoverySeconds", Config.RECOVERY_SECONDS.get() - 1);
        LongTermEffects.advanceSecond(player);
        LongTermEffects.advanceSecond(player);
        helper.assertTrue(!history.contains("beer"), "Full recovery clears the substance entry");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void villagerJobsAndTrades(GameTestHelper helper) {
        var professions = new net.minecraft.world.entity.npc.VillagerProfession[] {DrugVillagers.HERBALIST.get(), DrugVillagers.DEALER.get()};
        var workstations = new Block[] {DrugVillagers.HERBALIST_TABLE.get(), DrugVillagers.DEALER_COUNTER.get()};
        var villager = net.minecraft.world.entity.EntityType.VILLAGER.create(helper.getLevel());
        for (int index = 0; index < professions.length; index++) {
            var profession = professions[index];
            var poi = net.minecraft.world.entity.ai.village.poi.PoiTypes.forState(workstations[index].defaultBlockState());
            helper.assertTrue(poi.isPresent() && profession.acquirableJobSite().test(poi.orElseThrow()), "Workstation must match profession");
            helper.assertTrue(poi.orElseThrow().is(net.minecraft.tags.PoiTypeTags.ACQUIRABLE_JOB_SITE), "Unemployed villagers must be able to find workstation");
            var trades = net.minecraft.world.entity.npc.VillagerTrades.TRADES.get(profession);
            for (int level = 1; level <= 5; level++) {
                helper.assertTrue(trades.get(level).length == 2, "Each level must have one buy and one sell trade");
                var buy = trades.get(level)[0].getOffer(villager, villager.getRandom());
                var sell = trades.get(level)[1].getOffer(villager, villager.getRandom());
                helper.assertTrue(buy.getResult().is(net.minecraft.world.item.Items.EMERALD), "Villager must pay emeralds");
                helper.assertTrue(sell.getCostA().is(net.minecraft.world.item.Items.EMERALD), "Villager must accept emeralds");
                helper.assertTrue(buy.getMaxUses() == 12 && sell.getMaxUses() == 12, "Trades must have bounded stock");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void unemployedVillagerClaimsDealerCounter(GameTestHelper helper) {
        helper.getLevel().setDayTime(2000);
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) helper.setBlock(x, 1, z, Blocks.STONE);
        helper.setBlock(1, 2, 1, DrugVillagers.DEALER_COUNTER.get());
        var villager = helper.spawn(net.minecraft.world.entity.EntityType.VILLAGER, new BlockPos(2, 2, 2));
        villager.setVillagerData(villager.getVillagerData().setProfession(net.minecraft.world.entity.npc.VillagerProfession.NONE));
        helper.succeedWhen(() -> helper.assertTrue(villager.getVillagerData().getProfession() == DrugVillagers.DEALER.get(),
                "Unemployed villager should claim the counter and become a dealer"));
    }

    @GameTest(template = "empty")
    public static void nicotineWithdrawalDamageAndPatch(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var state = NicotineSystem.state(player);
        state.putInt("points", 80);
        state.putInt("abstinentSeconds", Config.NICOTINE_DELAY.get() - 1);
        state.putInt("withdrawalSeconds", 9);
        float before = player.getHealth();
        NicotineSystem.advanceSecond(player);
        helper.assertTrue(player.hasEffect(NicotineContent.WITHDRAWAL) && player.hasEffect(MobEffects.CONFUSION),
                "Abstaining must trigger withdrawal and disorientation");
        helper.assertTrue(player.getHealth() < before, "Withdrawal must deal periodic damage");
        player.setHealth(1);
        state.putInt("withdrawalSeconds", 19);
        NicotineSystem.advanceSecond(player);
        helper.assertTrue(player.getHealth() == 1, "Withdrawal must not kill a player");
        helper.assertTrue(NicotineSystem.applyPatch(player), "First patch should apply");
        helper.assertTrue(!NicotineSystem.applyPatch(player), "Patches must not stack");
        int points = LongTermEffects.points(state);
        for (int i = 0; i < 40; i++) NicotineSystem.advanceSecond(player);
        helper.assertTrue(!player.hasEffect(NicotineContent.WITHDRAWAL), "Patch must suppress withdrawal");
        helper.assertTrue(LongTermEffects.points(state) == points, "Patch supplies nicotine without adding dependence or curing it");
        helper.assertTrue(NicotineSystem.patchRemaining(player) == Config.PATCH_SECONDS.get() - 40, "Patch timer must advance");
        state.putInt("patchSeconds", 1);
        NicotineSystem.advanceSecond(player);
        helper.assertTrue(NicotineSystem.patchRemaining(player) == 0 && !player.hasEffect(NicotineContent.PATCH_ACTIVE), "Patch must expire");
        state.putInt("abstinentSeconds", Config.NICOTINE_DELAY.get() - 1);
        NicotineSystem.advanceSecond(player);
        helper.assertTrue(player.hasEffect(NicotineContent.WITHDRAWAL), "Withdrawal must return after expired patch and grace period");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nicotineSaveDeathAndRecovery(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        NicotineSystem.dose(player);
        NicotineSystem.applyPatch(player);
        NicotineSystem.advanceSecond(player);
        var saved = new net.minecraft.nbt.CompoundTag();
        player.saveWithoutId(saved);
        var loaded = helper.makeMockPlayer(GameType.SURVIVAL);
        loaded.load(saved);
        var respawned = helper.makeMockPlayer(GameType.SURVIVAL);
        respawned.copyAttachmentsFrom(loaded, true);
        helper.assertTrue(NicotineSystem.patchRemaining(respawned) == Config.PATCH_SECONDS.get() - 1, "Patch duration must survive saves and death");
        helper.assertTrue(LongTermEffects.points(NicotineSystem.state(respawned)) == Config.DEPENDENCE_PER_USE.get(), "Nicotine dependence must survive death");
        NicotineSystem.advanceSecond(respawned);
        helper.assertTrue(respawned.hasEffect(NicotineContent.PATCH_ACTIVE), "Respawn must restore patch indicator");
        var state = NicotineSystem.state(respawned);
        state.putInt("patchSeconds", 0);
        state.putInt("points", 1);
        state.putInt("abstinentSeconds", Config.NICOTINE_DELAY.get());
        state.putInt("recoverySeconds", Config.RECOVERY_SECONDS.get() - 1);
        NicotineSystem.advanceSecond(respawned);
        helper.assertTrue(LongTermEffects.points(state) == 0, "Abstinence must allow full nicotine recovery");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void withdrawalDropsOneItemAndProtectsCreative(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        player.getInventory().items.set(0, new ItemStack(net.minecraft.world.item.Items.DIAMOND, 4));
        player.getInventory().armor.set(3, new ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
        player.getInventory().offhand.set(0, new ItemStack(net.minecraft.world.item.Items.SHIELD));
        helper.assertTrue(NicotineSystem.dropRandomItem(player), "Withdrawal must be able to toss an inventory item");
        helper.assertTrue(player.getInventory().items.getFirst().getCount() == 3, "Only one item, not a stack, should drop");
        helper.assertTrue(!player.getInventory().armor.get(3).isEmpty() && !player.getInventory().offhand.getFirst().isEmpty(), "Armor and offhand must be excluded");
        var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(3), drop -> drop.getItem().is(net.minecraft.world.item.Items.DIAMOND));
        helper.assertTrue(drops.stream().mapToInt(drop -> drop.getItem().getCount()).sum() == 1, "Dropped item must exist in the world exactly once");
        player.getInventory().items.clear();
        helper.assertTrue(!NicotineSystem.dropRandomItem(player), "Empty inventory should be safe");
        var creative = helper.makeMockPlayer(GameType.CREATIVE);
        creative.getInventory().items.set(0, new ItemStack(net.minecraft.world.item.Items.DIAMOND, 4));
        helper.assertTrue(!NicotineSystem.dropRandomItem(creative), "Creative players must not drop items");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nicotineItemsAndVapeRefill(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var vape = new ItemStack(NicotineContent.VAPE.get());
        for (int puff = 0; puff < NicotineItem.VAPE_PUFFS; puff++) {
            player.getCooldowns().removeCooldown(NicotineContent.VAPE.get());
            NicotineContent.VAPE.get().finishUsingItem(vape, helper.getLevel(), player);
        }
        player.getCooldowns().removeCooldown(NicotineContent.VAPE.get());
        helper.assertTrue(vape.getCount() == 1 && vape.getDamageValue() == 32, "Vape must retain its body when empty");
        NicotineContent.VAPE.get().finishUsingItem(vape, helper.getLevel(), player);
        helper.assertTrue(vape.getDamageValue() == 32, "Empty vape must not dispense extra puffs");
        helper.assertTrue(!player.getCooldowns().isOnCooldown(NicotineContent.CIGAR.get()), "Vape cooldown must not be global");
        var cigars = new ItemStack(NicotineContent.CIGAR.get(), 2);
        NicotineContent.CIGAR.get().finishUsingItem(cigars, helper.getLevel(), player);
        helper.assertTrue(cigars.getCount() == 1, "Smoking must consume one cigar");
        var recipe = helper.getLevel().getRecipeManager().byKey(id("refill_vape")).orElseThrow().value();
        helper.assertTrue(recipe.getIngredients().getFirst().test(vape), "Refill recipe must accept an empty damaged vape");
        helper.assertTrue(recipe.getResultItem(helper.getLevel().registryAccess()).getDamageValue() == 0, "Refill restores all puffs");
        var patches = new ItemStack(NicotineContent.PATCH.get(), 2);
        NicotineContent.PATCH.get().finishUsingItem(patches, helper.getLevel(), player);
        NicotineContent.PATCH.get().finishUsingItem(patches, helper.getLevel(), player);
        helper.assertTrue(patches.getCount() == 1, "Only one patch may be consumed while active");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void beerFluidAndBottles(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        var brew = (com.simibubi.create.content.processing.recipe.ProcessingRecipe<?, ?>) manager.byKey(id("mix_beer")).orElseThrow().value();
        helper.assertTrue(brew.getFluidResults().getFirst().getFluid() == BeerFluids.BEER.get()
                && brew.getFluidResults().getFirst().getAmount() == 1000, "Mixer must produce one bucket of beer fluid");
        var filling = (com.simibubi.create.content.processing.recipe.ProcessingRecipe<?, ?>) manager.byKey(id("fill_beer_bottle")).orElseThrow().value();
        helper.assertTrue(filling.getFluidIngredients().getFirst().test(new net.neoforged.neoforge.fluids.FluidStack(BeerFluids.BEER.get(), 250)), "Spout must accept beer fluid");
        helper.assertTrue(BeerFluids.BEER.get().getBucket() == BeerFluids.BEER_BUCKET.get(), "Beer must support buckets");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var result = NicotineContent.BEER.get().finishUsingItem(new ItemStack(NicotineContent.BEER.get()), helper.getLevel(), player);
        helper.assertTrue(result.is(AlcoholContent.EMPTY_BEER_BOTTLE.get()), "Last beer returns empty bottle");
        helper.assertTrue(player.getCooldowns().isOnCooldown(NicotineContent.BEER.get()), "Beer must set its own cooldown");
        player.getCooldowns().removeCooldown(NicotineContent.BEER.get());
        var stack = new ItemStack(NicotineContent.BEER.get(), 2);
        NicotineContent.BEER.get().finishUsingItem(stack, helper.getLevel(), player);
        helper.assertTrue(stack.getCount() == 1 && player.getInventory().countItem(AlcoholContent.EMPTY_BEER_BOTTLE.get()) == 1, "Stacked beer consumes one and returns one bottle");
        var creative = helper.makeMockPlayer(GameType.CREATIVE);
        GameType.CREATIVE.updatePlayerAbilities(creative.getAbilities());
        var creativeBeer = new ItemStack(NicotineContent.BEER.get());
        NicotineContent.BEER.get().finishUsingItem(creativeBeer, helper.getLevel(), creative);
        helper.assertTrue(creativeBeer.getCount() == 1 && creative.getInventory().countItem(AlcoholContent.EMPTY_BEER_BOTTLE.get()) == 0, "Creative mode must not duplicate bottles");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void drunkLevelsAndBottleThrowing(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(AlcoholContent.drunkLevel(player) == 0, "Players start sober");
        for (int i = 1; i <= 6; i++) {
            player.getCooldowns().removeCooldown(NicotineContent.BEER.get());
            NicotineContent.BEER.get().finishUsingItem(new ItemStack(NicotineContent.BEER.get()), helper.getLevel(), player);
            helper.assertTrue(AlcoholContent.drunkLevel(player) == Math.min(i, AlcoholContent.MAX_DRUNK_LEVEL), "Each beer adds one drunk level up to the cap");
        }
        float drunkAim = AlcoholContent.inaccuracy(player);
        player.removeEffect(AlcoholContent.DRUNK);
        helper.assertTrue(drunkAim < AlcoholContent.inaccuracy(player), "Drunk throwers are more accurate by default");
        var bottles = new ItemStack(AlcoholContent.EMPTY_BEER_BOTTLE.get(), 2);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bottles);
        AlcoholContent.EMPTY_BEER_BOTTLE.get().use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(bottles.getCount() == 1, "Throwing consumes one bottle");
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(ThrownBeerBottle.class, player.getBoundingBox().inflate(4)).isEmpty(), "A bottle projectile must spawn");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void withdrawalEscalatesAndHeavyVapingKills(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var state = NicotineSystem.state(player);
        state.putInt("points", 80);
        state.putInt("abstinentSeconds", Config.NICOTINE_DELAY.get());
        int step = Config.NICOTINE_ESCALATION_SECONDS.get();
        helper.assertTrue(NicotineSystem.withdrawalLevel(0) == 0 && NicotineSystem.withdrawalLevel(step) == 1
                && NicotineSystem.withdrawalLevel(step * 99) == NicotineSystem.MAX_LEVEL, "Withdrawal level rises with time and caps");
        state.putInt("withdrawalSeconds", step * 3);
        NicotineSystem.advanceSecond(player);
        var effect = player.getEffect(NicotineContent.WITHDRAWAL);
        helper.assertTrue(effect != null && effect.getAmplifier() == 3, "Longer withdrawal must raise the effect amplifier");
        helper.assertTrue(player.hasEffect(MobEffects.HUNGER) && player.hasEffect(MobEffects.WEAKNESS), "Longer withdrawal adds symptoms");
        var doomed = helper.makeMockPlayer(GameType.SURVIVAL);
        var doomedState = NicotineSystem.state(doomed);
        doomedState.putInt("points", 80);
        doomedState.putInt("abstinentSeconds", Config.NICOTINE_DELAY.get());
        doomedState.putInt("withdrawalSeconds", step * NicotineSystem.LETHAL_LEVEL + 5);
        doomed.setHealth(1);
        for (int i = 0; i < 10 && doomed.isAlive(); i++) NicotineSystem.advanceSecond(doomed);
        helper.assertTrue(!doomed.isAlive(), "Withdrawal at level 2 must be able to kill");
        var vaper = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < Config.VAPE_SAFE_PUFFS.get(); i++) NicotineSystem.puff(vaper);
        helper.assertTrue(NicotineSystem.lungDamage(NicotineSystem.state(vaper)) == 0, "Moderate vaping is safe");
        for (int i = 0; i < 40; i++) NicotineSystem.puff(vaper);
        var lungs = NicotineSystem.state(vaper);
        helper.assertTrue(NicotineSystem.lungStage(NicotineSystem.lungDamage(lungs)) == 4, "Heavy vaping reaches fatal lung damage");
        for (int i = 0; i < 30 && vaper.isAlive(); i++) NicotineSystem.advanceSecond(vaper);
        helper.assertTrue(!vaper.isAlive(), "Fatal lung damage must eventually kill");
        var recovered = helper.makeMockPlayer(GameType.SURVIVAL);
        recovered.copyAttachmentsFrom(vaper, true);
        NicotineSystem.resetOnDeath(recovered);
        helper.assertTrue(NicotineSystem.lungDamage(NicotineSystem.state(recovered)) == 0, "Lung damage resets on death");
        var survivor = helper.makeMockPlayer(GameType.SURVIVAL);
        var survivorState = NicotineSystem.state(survivor);
        survivorState.putInt("points", 50);
        survivorState.putInt("withdrawalSeconds", step * 3);
        survivorState.putInt("lungDamage", 12);
        NicotineSystem.resetOnDeath(survivor);
        helper.assertTrue(NicotineSystem.withdrawalLevel(survivorState.getInt("withdrawalSeconds")) == 0
                && NicotineSystem.lungDamage(survivorState) == 0 && LongTermEffects.points(survivorState) == 0,
                "Death resets withdrawal level, lungs and dependence");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void deathClearsBeerDependence(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var beer = player.getData(LongTermEffects.HISTORY).getCompound("beer");
        beer.putInt("points", 90);
        player.getData(LongTermEffects.HISTORY).put("beer", beer);
        LongTermEffects.resetOnDeath(player);
        helper.assertTrue(!player.getData(LongTermEffects.HISTORY).contains("beer"), "Death must clear beer dependence");
        helper.succeed();
    }

    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(Create_drugs.MODID, name); }
}
