# Asset handoff

All paths below are relative to `src/main/resources/assets/create_drugs/`.
Use lowercase filenames exactly as listed. Start with transparent 16x16 or 32x32 PNGs
for item/crop textures; Minecraft supports larger square textures too.

## Item textures (12)

Put these in `textures/item/`:

- `cannabis_seeds.png`
- `coca_seeds.png`
- `cannabis_leaf.png`
- `coca_leaf.png`
- `dried_cannabis.png`
- `coca_dust.png`
- `crystal_blend.png`
- `blotter_paper.png`
- `joint.png`
- `cocaine.png`
- `methamphetamine.png`
- `lsd.png`

There are **12 item textures**; the two crop blocks are additional registered content,
not separate inventory items. Seeds serve as their placeable inventory items.

## Crops (16 textures)

Put these in `textures/block/`:

- `cannabis_crop_stage0.png` through `cannabis_crop_stage7.png`
- `coca_crop_stage0.png` through `coca_crop_stage7.png`

Stage 0 is newly planted and stage 7 is mature. The models use vanilla crop-shaped
planes and a cutout render layer. Use transparent backgrounds around the plants.
Both crops currently use wheat stage textures until your replacements are detected.

## Effect icon

`textures/mob_effect/intoxication.png`: an 18x18 transparent PNG.
The vanilla nausea icon is the placeholder. The resource generator updates the
effect atlas to select your replacement when present.

## Sound

`sounds/consume.ogg`: a mono Ogg Vorbis clip. All four consumables share this sound
event (`create_drugs:consume`) after use completes. Vanilla eating animation sounds
also occur during the use animation. The current completion fallback is the vanilla
drinking event. Subtitles are already registered.

## Activate assets

### New in 1.1: villagers and workstations

Replace these 64x64 villager skin overlay PNGs directly (keep transparent areas and
the vanilla villager UV layout):

- `textures/entity/villager/profession/herbalist.png`
- `textures/entity/villager/profession/dealer.png`
- `textures/entity/zombie_villager/profession/herbalist.png`
- `textures/entity/zombie_villager/profession/dealer.png`

The included clothing placeholders are copied from Minecraft's farmer and cleric
textures, © Mojang. They are temporary local development references; replace them
with your own art before distributing an art-complete release. The resource generator
never replaces these PNGs.

Optional workstation cube textures (16x16 or 32x32):

- `textures/block/herbalist_table.png`
- `textures/block/dealer_counter.png`

When present, the generator uses each texture on all six faces. Until then, the
blocks reference vanilla cartography and smithing table models. Separate face textures
or custom geometry can be added later if your friend designs them.

### Rebuild

### New in 1.2: nicotine and beer

Additional transparent item textures (16x16 or 32x32), in `textures/item/`:

- `tobacco_seeds.png`, `tobacco_leaf.png`, `dried_tobacco.png`
- `nicotine_blend.png`, `vape_cartridge.png`, `vape.png`, `cigar.png`
- `nicotine_patch.png`, `malt.png`, `beer.png`, `beer_bucket.png`, `empty_beer_bottle.png`
  (the thrown bottle reuses this texture)

Additional crop stages in `textures/block/`:

- `tobacco_crop_stage0.png` through `tobacco_crop_stage7.png`

Additional 18x18 effect icons in `textures/mob_effect/`:

- `nicotine_withdrawal.png`
- `nicotine_patch.png`
- `drunk.png`

Additional mono Ogg Vorbis sound clips:

- `sounds/inhale.ogg`: vape/cigar completion sound.
- `sounds/withdrawal.ogg`: brief unsettling withdrawal sound, played occasionally.

These all use vanilla placeholders until supplied; run the generator after adding
them. The beer fluid currently uses amber-tinted vanilla water textures. A future
custom beer fluid texture would require adjusting its client texture registration;
the beer bottle and bucket PNGs already support automatic detection.

### Rebuild after adding assets

1. Add any subset of the files above.
2. From the project folder, run `python tools/generate_resources.py`.
3. Run `./gradlew.bat build` with Java 21, or launch `./gradlew.bat runClient`.

The generator only overwrites the generated JSON models, recipes, translations,
sound definitions and atlas entry; it never overwrites your images or sounds.
If you hand-edit those JSON files, also update the generator or avoid rerunning it.
To change display names or tooltip text, edit `assets/create_drugs/lang/en_us.json`
and the matching entries in the generator.
