# Texture pack templates

Blank, correctly named and sized PNGs. The folder layout mirrors the mod, so finished
files go in `src/main/resources/assets/create_drugs/` (or a resource pack's
`assets/create_drugs/`) with the same paths.

Every file has a magenta border, a faint 4px grid and a cyan center pixel as guides.
**Erase the guides before shipping.** Item, crop and effect templates are transparent;
block templates are opaque white. Keep filenames exactly as they are (lowercase).

## Sizes

| Folder | Size | Notes |
| --- | --- | --- |
| `textures/item/` (24) | 16x16 | Transparent background. Higher power-of-two sizes (32x32, 64x64) also work. |
| `textures/block/*_crop_stage0-7.png` (24) | 16x16 | Transparent background. Stage 0 is newly planted, stage 7 is fully grown. Crossed-plane model. |
| `textures/block/herbalist_table.png`, `dealer_counter.png` | 16x16 | Opaque; used on all six faces. |
| `textures/mob_effect/` (4) | 18x18 | Status effect icons: intoxication, nicotine_withdrawal, nicotine_patch, drunk. |

## Items

Cannabis: `cannabis_seeds`, `cannabis_leaf`, `dried_cannabis`, `joint`.
Coca and others: `coca_seeds`, `coca_leaf`, `coca_dust`, `crystal_blend`, `blotter_paper`,
`cocaine`, `methamphetamine`, `lsd`.
Tobacco and nicotine: `tobacco_seeds`, `tobacco_leaf`, `dried_tobacco`, `nicotine_blend`,
`vape_cartridge`, `vape`, `cigar`, `nicotine_patch`.
Beer: `malt`, `beer` (full bottle), `beer_bucket`, `empty_beer_bottle`
(also used for the thrown bottle in flight).

## Not templated

- **Villager skins** (64x64 overlays) already exist as placeholders in
  `textures/entity/villager/profession/` and `textures/entity/zombie_villager/profession/`
  (`herbalist.png`, `dealer.png`); edit those directly using the vanilla villager UV layout.
- **Beer fluid** currently uses tinted vanilla water textures; a custom one needs a code change.
- **Sounds** (`sounds/consume.ogg`, `inhale.ogg`, `withdrawal.ogg`): mono Ogg Vorbis.

## Using finished art

1. Copy the finished PNGs into `src/main/resources/assets/create_drugs/` at the same paths.
2. Run `python tools/generate_resources.py` so models and the effect atlas switch from
   vanilla placeholders to your textures.
3. Rebuild as usual.

To regenerate these blank templates (this overwrites them): `python tools/generate_texture_templates.py`.
