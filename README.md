# Create: Drugs

A Minecraft **1.21.1 / NeoForge 21.1.264** addon targeting **Create 6.0.10**.
Real-world item names use deliberately fictional Minecraft recipes and game effects.
Textures and sounds currently reference vanilla assets; no custom art is required to play.

## Build and install

Use **JDK 21** (not Java 26) and run `./gradlew.bat build` on Windows.
The jar is `build/libs/create_drugs-1.2.0.jar`. Replace the older addon jar when updating;
do not install both versions together.
Install NeoForge 21.1.264 for Minecraft 1.21.1, install the official Create 6.0.10
release and its required dependencies, then put this jar in the instance's `mods` folder.
Both client and server need the mod. This is not a Fabric or Forge 1.20.1 build.

Development: `./gradlew.bat runClient`. Create and its development dependencies are
downloaded automatically. Official dependency reference:
https://wiki.createmod.net/developers/depend-on-create/neoforge-1.21.1

## Recipe browser (JEI)

JEI 19.21.0.247 is included automatically when launching the development client
with `./gradlew.bat runClient` (refresh Gradle in your IDE after updating).
Open your inventory and search for `@create_drugs`. Hover an item and press **R**
for its recipes or **U** for its uses. Create supplies the machine recipe categories,
including mixing, pressing, milling, crushing, haunting and deploying.

For a normal launcher installation, install the **NeoForge / Minecraft 1.21.1**
version of [JEI](https://modrinth.com/mod/jei) alongside Create and this addon.
JEI is a separate optional mod; it is not embedded inside the addon jar.
Development dependency setup follows the
[official JEI guide](https://github.com/mezz/JustEnoughItems/wiki/Getting-Started-%5Bminecraft-1.21-and-1.21.1%5D).

## Survival progression

Craft cannabis seeds from wheat seeds + a vine, coca seeds from wheat seeds + cocoa beans,
or tobacco seeds from wheat seeds + a brown mushroom.
Plant on farmland with light and water nearby. All three crops have eight growth stages,
support bone meal, and yield two leaves plus seeds when mature. Immature plants only
return a seed. Create mechanical harvesters can harvest the crops and reset their age.

| Output | Fictional game recipe | Machine |
| --- | --- | --- |
| Dried cannabis | Cannabis leaf | Encased fan blowing through soul fire (haunting) |
| Joint | Dried cannabis + paper | Crafting, or deploy paper onto dried cannabis |
| 2 coca dust | Coca leaf | Millstone or crushing wheels |
| 2 cocaine | Coca dust + glowstone dust + sugar | Mixer and basin, no heat |
| 2 crystal blend | Amethyst shard + redstone + sugar | Mixer and basin over a heated blaze burner |
| Methamphetamine | Crystal blend | Mechanical press over depot/belt |
| 2 blotter paper | Paper | Mechanical press over depot/belt |
| 2 LSD | Blotter paper + ender pearl + purple dye | Mixer and basin, no heat |

Recipes are normal Create recipes, so belts, funnels, basins, depots and deployers
can automate production. The addon adds no new kinetic machine blocks.
All items, including the new nicotine products, beer bucket and both workstations,
are available in the **Create: Drugs** creative tab.

## Effects

Hold use/right-click for 1.6 seconds. Consumption works at full hunger.

| Item | Benefit | Drawback |
| --- | --- | --- |
| Cannabis joint | Resistance I | Slowness I |
| Cocaine | Speed II | Hunger II |
| Methamphetamine | Haste II | Weakness I |
| LSD | Night Vision | Nausea |
| Beer | Resistance I | Slowness I |

Benefits and drawbacks last 45 seconds by default. Each substance has its own 3-second cooldown (`cooldownSeconds`, 0 disables); there is no global cooldown. Intoxication accumulates across substances and clears after 120 seconds
without another use. The third and subsequent doses within that window add Poison I
for 10 seconds, Nausea for 20 seconds and Weakness II for 30 seconds. Milk clears
effects using normal Minecraft behaviour. Creative mode preserves the consumed stack.
These are gameplay mechanics, not simulations of real substances.

## Persistent long-term effects

Only **alcohol (beer) and nicotine** cause dependence. Joints, cocaine, methamphetamine
and LSD have short-term effects only. Survival players accumulate a dependence score
for each, from 0 to 100. Each use adds 10 points by default. Existing dependence reduces the duration
of the next dose's benefit by up to 50%; the drawback keeps its full duration.
Creative and spectator mode neither accumulate nor recover dependence.

After three online minutes without that substance, withdrawal begins when its score
is at least 30. At the same time, the score starts recovering by one point every
30 online seconds. Each substance recovers independently, so taking a different
substance does not reset the first substance's withdrawal.

| Dependence | Withdrawal |
| --- | --- |
| 30–59 | Weakness I and Mining Fatigue I |
| 60–89 | Also Slowness I |
| 90–100 | Also Hunger I |

Scores and recovery timers **save with the player and survive death, reconnects,
server restarts and dimension changes**. Offline time does not advance recovery.
Withdrawal returns within one second after respawning or drinking milk while its
conditions still apply. After recovery, refreshed penalties expire within three
seconds. Ordinary short-term intoxication/potion effects still follow vanilla death rules.

- `/drugstatus`: view dependence scores, online abstinence timers, and remaining nicotine patch time.
- `/drugreset <players>`: operator-only reset; accepts a player name or selectors
  such as `@a`. Existing short withdrawal effects expire within three seconds.
- Config options: `longTermEffects`, `dependencePerUse`, `withdrawalDelaySeconds`,
  `recoverySecondsPerPoint`. Disabling long-term effects pauses saved history and
  stops applying withdrawal; it does not delete player progress.

## Vapes, cigars and nicotine patches (1.2)

Vapes and cigars share a **separate nicotine dependence score** and time
since the last nicotine dose. Hold use for 1.6 seconds to smoke/vape. Each completed
use adds 10 dependence points in survival mode. A cigar is consumed; a vape uses one
of its 32 puffs. Empty vapes remain in your inventory for refilling.

At 20 or more dependence points, going **120 online seconds** without nicotine
causes withdrawal. These defaults are configurable:

- Nicotine Withdrawal slows movement by 15% per level.
- Nausea, plus a darkness pulse and unsettling sound every 30 seconds.
- Half a heart of damage every 10 seconds, stopping at half a heart of health.
- Every 30 seconds, a 25% chance to drop **one item** from a randomly selected occupied
  main-inventory slot. Whole stacks, armor and offhand items are not dropped. Items
  remain in the world for pickup; they can still be lost to normal world hazards or
  despawning. Standard item-toss cancellation hooks are respected.

**Escalation:** the longer you go without nicotine, the worse it gets. Every
`nicotineEscalationSeconds` (default 120) of withdrawal raises the level by one, up to 4:

| Level | Added effects |
| --- | --- |
| 1 | Weakness I, Mining Fatigue I |
| 2 | Weakness II, Hunger I, damage every 6 s, darkness/drops every 20 s |
| 3 | Mining Fatigue II, Hunger II, periodic Blindness, damage every 4 s |
| 4 | Weakness III, damage every 3 s, darkness/drops every 10 s |

Damage per hit grows from half a heart (level 0) to 1.5 hearts (level 4), and the
darkness/drop checks speed up by 5 seconds per level (minimum 10 s).

Drop chance rises 10% per level. Below level 2, damage never drops you below half a
heart. **From level 2 (240 seconds of withdrawal), withdrawal damage can kill you.**
Set `nicotineWithdrawalLethal = false` to keep the half-heart floor at every level.
A patch or any nicotine resets the level, and dying resets it too.

**Death messages:** custom damage types give each cause its own chat message, with
three random variants each for nicotine withdrawal ("could not go on without nicotine")
and popcorn lung ("vaped their lungs away"), plus "hit by a flying beer bottle" /
"knocked out by <player> with a beer bottle" for thrown bottles. Edit the
`death.attack.create_drugs.*` blocks in the lang file (and the generator) to reword them.
These damage types bypass armor but not Totems of Undying.
Any nicotine (dose or patch) resets the level to 0.

**Popcorn lung:** vapes (not cigars or patches) build saved lung damage. You can take
`vapeSafePuffs` (default 6) recent puffs freely; each puff past that adds more damage
than the last. Recent puffs stop counting after `vapeLoadDecaySeconds` (15) each.
Lung damage only heals, 1 point per 300 online seconds, while you are not vaping.
With the default fatal value of 30:

| Lung damage | Stage |
| --- | --- |
| 5+ | Coughing: periodic brief slowness, smoke and breath sounds |
| 10+ | Short of breath: Slowness I and Mining Fatigue I, more coughing |
| 20+ | Severe: Slowness II, Weakness, Hunger, coughing hurts |
| 30+ | Lung failure: 1 heart of drowning damage every 3 seconds until you die |

Dying (from any cause) resets escalated withdrawal, lung damage and the abstinence
clock, and clears all dependence (beer and nicotine); only an active patch is kept. Patches do not help
the lungs; only time away from vapes does. Creative/spectator players are exempt.

Hold use with a **Nicotine Patch** to apply it. It passively supplies nicotine for
**600 online seconds (10 minutes)** without occupying an equipment slot. It prevents
withdrawal damage and drops immediately, removes the Nicotine Withdrawal effect,
and prevents new disorientation pulses. Existing nausea/darkness fades within four
seconds. A patch does not increase or reduce dependence and does not stack; applying
another while one is active does not consume it. After it expires, the normal
120-second grace period begins again.

Nicotine scores, abstinence/recovery counters and remaining patch time survive
death, reconnects and server restarts. Offline time does not advance them. Milk cannot
erase the saved state. Continued abstinence without a patch reduces dependence by
one point every `recoverySecondsPerPoint` seconds after the grace period. Creative
and spectator players are exempt from dependence changes and withdrawal penalties.

Nicotine settings in `create_drugs-server.toml`:

| Setting | Default |
| --- | --- |
| `nicotineWithdrawalDelaySeconds` | 120 |
| `nicotineDependenceThreshold` | 20 |
| `nicotinePatchSeconds` | 600 |
| `nicotineWithdrawalDamage` | true |
| `nicotineWithdrawalDisorientation` | true |
| `nicotineWithdrawalItemDrops` | true |
| `nicotineDropChance` | 0.25 |
| `nicotineEscalationSeconds` | 120 |
| `nicotineWithdrawalLethal` | true |
| `popcornLung` | true |
| `vapeSafePuffs` | 6 |
| `vapeLoadDecaySeconds` | 15 |
| `popcornLungFatalDamage` | 30 |
| `popcornLungDeath` | true |
| `lungRecoverySecondsPerPoint` | 300 |

The shared `longTermEffects` switch also disables nicotine dependence/withdrawal;
already-applied patch timers continue counting down. `/drugreset <players>` clears
nicotine history and active patches as well as other substances.

## Create production: nicotine and beer

These are fictional Minecraft processing recipes. All appear in JEI.

| Output | Inputs | Create machine |
| --- | --- | --- |
| Dried tobacco | Tobacco leaf | Haunting fan (soul fire) |
| 2 nicotine blend | Dried tobacco + glowstone dust | Mixer/basin, no heat |
| Cigar | Dried tobacco, with tobacco leaf in deployer hand | Deployer over belt/depot |
| 2 vape cartridges | Nicotine blend + amethyst shard | Mixer/basin, no heat |
| Vape (32 puffs) | Iron sheet above cartridge above redstone | Three vertical mechanical crafters, outputs connected |
| Refilled vape | Used vape, with cartridge in deployer hand | Deployer over belt/depot |
| 2 nicotine patches | Paper, with nicotine blend in deployer hand | Deployer over belt/depot |
| 2 malt | Wheat | Mechanical press over belt/depot |
| 1,000 mB beer | Malt + sugar + brown mushroom + 1,000 mB water | Heated mixer/basin |
| Beer bottle | Glass bottle or empty beer bottle + 250 mB beer | Spout over belt/depot |

Beer is a registered fluid: Create pipes, pumps, tanks and spouts can handle it,
and it has a bucket for placing/collecting it. One bucket yields four bottles.
Beer bottles apply the benefits/drawbacks above, participate in intoxication and
long-term dependence, and return an **empty beer bottle** when consumed in survival.

**Drunk:** each beer raises your Drunk level by one (max 5) for `drunkSeconds`
(default 180). Level 2+ adds nausea and level 5 adds Slowness II.
**Throwing bottles:** use an empty beer bottle to throw it; it deals `bottleDamage`
(default 2) and shatters. Aim spread is `bottleInaccuracy` (default 6) and, by default,
shrinks as you get drunker (perfectly straight at level 5). Set `drunkAimImproves = false`
to reverse this so drunkenness worsens aim. Empty bottles can be refilled at a spout.
Malt is pressed to avoid competing with Create's
existing wheat-to-flour milling recipe.

## Buying and selling with villagers

These are two custom **villager professions**, with normal trading screens,
profession levels, limited stock, demand pricing, discounts and workstation restocks.
Place a workstation where an adult unemployed villager can reach it during working
hours. Nitwits and villagers already locked to another profession cannot switch.
Workstations are craftable and appear in JEI; they are not added to village worldgen.

- **Herbalist Table:** crafting table + flower pot + cannabis seeds (shapeless).
- **Dealer Counter:** barrel + emerald + paper (shapeless).

Each level unlocks both listed offers. Prices below are base prices before normal
villager price adjustments; each offer has 12 uses before restocking.

| Level | Herbalist buys from you | Herbalist sells to you |
| --- | --- | --- |
| Novice | 16 cannabis leaves → 1 emerald | 2 emeralds → 4 cannabis seeds |
| Apprentice | 16 coca leaves → 1 emerald | 2 emeralds → 4 coca seeds |
| Journeyman | 8 dried cannabis → 1 emerald | 2 emeralds → 4 blotter paper |
| Expert | 12 coca dust → 2 emeralds | 3 emeralds → 4 dried cannabis |
| Master | 4 joints → 2 emeralds | 4 emeralds → 1 joint |

| Level | Dealer buys from you | Dealer sells to you |
| --- | --- | --- |
| Novice | 4 joints → 2 emeralds | 4 emeralds → 1 joint |
| Apprentice | 4 cocaine → 3 emeralds | 6 emeralds → 1 cocaine |
| Journeyman | 4 methamphetamine → 4 emeralds | 8 emeralds → 1 methamphetamine |
| Expert | 4 LSD → 3 emeralds | 6 emeralds → 1 LSD |
| Master | 8 crystal blend → 3 emeralds | 5 emeralds → 2 crystal blend |

The villagers use vanilla farmer/cleric clothing placeholders, ready for replacement
with your friend's textures. The workstations use vanilla table model placeholders.

Settings are generated in `config/create_drugs-server.toml` in the game instance
with the targeted NeoForge version. World-specific overrides may also be placed in
the world's `serverconfig` folder. Configure `effectSeconds`, `cooldownSeconds`,
`intoxicationSeconds`, and `overusePenalties`. Restart the world after editing.

## Your artwork and sounds

See [ASSETS.md](ASSETS.md). Add your files, run `python tools/generate_resources.py`,
then rebuild. The generator detects your assets and switches the corresponding
models from vanilla placeholders. It never modifies PNG or OGG files.

## Verification

`./gradlew.bat runGameTestServer` starts an isolated Minecraft test server with Create.
Tests cover all twenty-five recipe registrations, survival consumption, per-item cooldowns,
creative inventory, mixed-substance overuse, both crops' drops/replanting, and
Create harvester recognition of mature versus immature crops. Additional tests cover
player save/load and death attachment copying, tolerance, withdrawal and recovery,
every villager trade level, workstation registration, and actual villager job acquisition.
Nicotine/beer tests cover saved nicotine and patch state, expiry and reapplication,
withdrawal damage and its health floor, safe single-item drops, finite vape charges,
Create refill ingredients, beer mixer/spout fluid inputs/outputs, and bottle returns.
The test class and empty test structure are excluded from the release jar.

Manual client checks: open the creative tab; plant/bone-meal all three crops; run a Create
harvester over mature crops; process each recipe in the machines above; consume each
item; check sounds, tooltips and effect icons. Client rendering and full moving
contraption behaviour require an interactive play-test.
