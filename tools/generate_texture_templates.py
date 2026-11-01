"""Generate blank texture templates in texture_templates/ (outside src, so the game never uses them).

Each template mirrors the real resource path, so finished art can be copied into
src/main/resources/assets/create_drugs/ unchanged. Run: python tools/generate_texture_templates.py
"""
from pathlib import Path
from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parents[1] / 'texture_templates'
BASE = OUT / 'assets/create_drugs'

ITEMS = ['cannabis_seeds', 'coca_seeds', 'cannabis_leaf', 'coca_leaf', 'dried_cannabis', 'coca_dust',
         'crystal_blend', 'blotter_paper', 'joint', 'cocaine', 'methamphetamine', 'lsd',
         'tobacco_seeds', 'tobacco_leaf', 'dried_tobacco', 'nicotine_blend', 'vape_cartridge', 'vape',
         'cigar', 'nicotine_patch', 'malt', 'beer', 'beer_bucket', 'empty_beer_bottle']
CROPS = [f'{crop}_crop_stage{i}' for crop in ('cannabis', 'coca', 'tobacco') for i in range(8)]
BLOCKS = ['herbalist_table', 'dealer_counter']
EFFECTS = ['intoxication', 'nicotine_withdrawal', 'nicotine_patch', 'drunk']

GUIDE = (255, 0, 255, 255)
GRID = (160, 160, 160, 70)
CENTER = (0, 200, 255, 120)


def template(size, path, opaque=False):
    img = Image.new('RGBA', (size, size), (255, 255, 255, 255) if opaque else (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for i in range(0, size, 4):
        draw.line([(i, 0), (i, size - 1)], fill=GRID)
        draw.line([(0, i), (size - 1, i)], fill=GRID)
    draw.rectangle([0, 0, size - 1, size - 1], outline=GUIDE)
    draw.point([(size // 2, size // 2)], fill=CENTER)
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)


for name in ITEMS:
    template(16, BASE / f'textures/item/{name}.png')
for name in CROPS:
    template(16, BASE / f'textures/block/{name}.png')
for name in BLOCKS:
    template(16, BASE / f'textures/block/{name}.png', opaque=True)
for name in EFFECTS:
    template(18, BASE / f'textures/mob_effect/{name}.png')
print('Generated', len(ITEMS) + len(CROPS) + len(BLOCKS) + len(EFFECTS), 'templates in', OUT)
