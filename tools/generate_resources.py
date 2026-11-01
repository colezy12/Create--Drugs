"""Regenerate JSON data and vanilla placeholder models (does not touch custom PNG/OGG files)."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
MOD = 'create_drugs'

def write(path, data):
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')

items = {
    'cannabis_seeds': ('Cannabis Seeds', 'wheat_seeds'),
    'coca_seeds': ('Coca Seeds', 'beetroot_seeds'),
    'cannabis_leaf': ('Cannabis Leaf', 'kelp'),
    'coca_leaf': ('Coca Leaf', 'dried_kelp'),
    'dried_cannabis': ('Dried Cannabis', 'green_dye'),
    'coca_dust': ('Coca Dust', 'lime_dye'),
    'crystal_blend': ('Crystal Blend', 'prismarine_crystals'),
    'blotter_paper': ('Blotter Paper', 'paper'),
    'joint': ('Cannabis Joint', 'stick'),
    'cocaine': ('Cocaine', 'sugar'),
    'methamphetamine': ('Methamphetamine', 'diamond'),
    'lsd': ('LSD', 'purple_dye'),
    'tobacco_seeds': ('Tobacco Seeds', 'pumpkin_seeds'),
    'tobacco_leaf': ('Tobacco Leaf', 'kelp'),
    'dried_tobacco': ('Dried Tobacco', 'brown_dye'),
    'nicotine_blend': ('Nicotine Blend', 'glowstone_dust'),
    'vape_cartridge': ('Vape Cartridge', 'amethyst_shard'),
    'vape': ('Vape', 'spyglass'),
    'cigar': ('Cigar', 'stick'),
    'nicotine_patch': ('Nicotine Patch', 'paper'),
    'malt': ('Malt', 'wheat'),
    'beer': ('Beer', 'honey_bottle'),
    'beer_bucket': ('Beer Bucket', 'water_bucket'),
    'empty_beer_bottle': ('Empty Beer Bottle', 'glass_bottle'),
}
lang = {'itemGroup.create_drugs': 'Create: Drugs',
        'block.create_drugs.cannabis_crop': 'Cannabis Crop',
        'block.create_drugs.coca_crop': 'Coca Crop',
        'effect.create_drugs.intoxication': 'Intoxication',
        'subtitles.create_drugs.consume': 'Substance consumed',
        'message.create_drugs.overuse': 'Overuse! Let your intoxication wear off.',
        'tooltip.create_drugs.overuse': 'Repeated use increases intoxication.',
        'tooltip.create_drugs.joint': 'Resistance I / Slowness I',
        'tooltip.create_drugs.cocaine': 'Speed II / Hunger II',
        'tooltip.create_drugs.methamphetamine': 'Haste II / Weakness I',
        'tooltip.create_drugs.lsd': 'Night Vision / Nausea'}
for name, (label, placeholder) in items.items():
    lang[f'item.{MOD}.{name}'] = label
    write(f'assets/{MOD}/models/item/{name}.json', {
        'parent': 'minecraft:item/generated', 'textures': {'layer0':
            f'{MOD}:item/{name}' if (ROOT / f'assets/{MOD}/textures/item/{name}.png').exists() else f'minecraft:item/{placeholder}'}})
write(f'assets/{MOD}/lang/en_us.json', lang)
lang.update({
    'block.create_drugs.herbalist_table': 'Herbalist Table',
    'block.create_drugs.dealer_counter': 'Dealer Counter',
    'entity.minecraft.villager.create_drugs.herbalist': 'Herbalist',
    'entity.minecraft.villager.create_drugs.dealer': 'Dealer',
    'tooltip.create_drugs.long_term': 'Repeated use builds lasting dependence. Check /drugstatus.',
    'message.create_drugs.dependence': '%s dependence: %s/100',
    'message.create_drugs.status': '%s: %s/100 dependence; %s seconds since last use (online time).',
    'message.create_drugs.reset': 'Cleared long-term drug history for %s player(s).'
})
lang.update({
    'block.create_drugs.tobacco_crop': 'Tobacco Crop',
    'block.create_drugs.beer': 'Beer',
    'fluid_type.create_drugs.beer': 'Beer',
    'effect.create_drugs.nicotine_withdrawal': 'Nicotine Withdrawal',
    'effect.create_drugs.nicotine_patch': 'Nicotine Patch Active',
    'tooltip.create_drugs.beer': 'Resistance I / Slowness I. Gets you drunk. Leaves an empty bottle you can throw.',
    'effect.create_drugs.drunk': 'Drunk',
    'entity.create_drugs.thrown_beer_bottle': 'Thrown Beer Bottle',
    'tooltip.create_drugs.empty_beer_bottle': 'Use to throw. The drunker you are, the straighter it flies.',
    'tooltip.create_drugs.nicotine': 'Supplies nicotine. Cigars and vapes share dependence.',
    'tooltip.create_drugs.vape': '%s / 32 puffs remaining. Refill with a cartridge in a deployer.',
    'tooltip.create_drugs.nicotine_patch': 'Hold use to apply. Passively prevents nicotine withdrawal; does not stack.',
    'message.create_drugs.nicotine_dose': 'Nicotine supplied. Dependence: %s/100',
    'message.create_drugs.nicotine_status': 'Nicotine: %s/100 dependence; %s seconds without nicotine; patch: %s seconds remaining; withdrawal level %s/4; lung damage %s/%s.',
    'message.create_drugs.withdrawal_worse': 'Your nicotine withdrawal is getting worse (level %s).',
    'message.create_drugs.lung_warning': 'Your chest feels tight. Stop vaping.',
    'message.create_drugs.lung_stage_0': 'Your lungs feel clear.',
    'message.create_drugs.lung_stage_1': 'You start coughing. Your lungs are irritated.',
    'message.create_drugs.lung_stage_2': 'You are short of breath. Popcorn lung is setting in.',
    'message.create_drugs.lung_stage_3': 'Severe popcorn lung! Every breath hurts. Stop vaping now.',
    'message.create_drugs.lung_stage_4': 'Your lungs are failing...',
    'message.create_drugs.nicotine_withdrawal': 'Nicotine withdrawal! Apply a patch to suppress the symptoms.',
    'message.create_drugs.withdrawal_drop': 'Withdrawal made you drop an item!',
    'message.create_drugs.vape_empty': 'Vape empty. Deploy a cartridge onto it to refill.',
    'message.create_drugs.patch_applied': 'Nicotine patch applied for %s online seconds.',
    'message.create_drugs.patch_expired': 'Nicotine patch expired.',
    'message.create_drugs.patch_already_active': 'A nicotine patch is already active.',
    'subtitles.create_drugs.inhale': 'Smoke exhaled',
    'subtitles.create_drugs.withdrawal': 'Unsettling whisper',
})
write(f'assets/{MOD}/lang/en_us.json', lang)
sounds = {'consume': {
    'subtitle': 'subtitles.create_drugs.consume',
    'sounds': [f'{MOD}:consume'] if (ROOT / f'assets/{MOD}/sounds/consume.ogg').exists()
        else [{'name': 'minecraft:entity.generic.drink', 'type': 'event'}]}}
for name, fallback in [('inhale', 'minecraft:block.fire.extinguish'), ('withdrawal', 'minecraft:entity.enderman.stare')]:
    sounds[name] = {'subtitle': f'subtitles.{MOD}.{name}', 'sounds': [f'{MOD}:{name}']
        if (ROOT / f'assets/{MOD}/sounds/{name}.ogg').exists() else [{'name': fallback, 'type': 'event'}]}
write(f'assets/{MOD}/sounds.json', sounds)
write('assets/minecraft/atlases/mob_effects.json', {'sources': [{'type': 'minecraft:single',
    'resource': f'{MOD}:mob_effect/{name}' if (ROOT / f'assets/{MOD}/textures/mob_effect/{name}.png').exists()
        else f'minecraft:mob_effect/{fallback}', 'sprite': f'{MOD}:{name}'}
    for name, fallback in [('intoxication', 'nausea'), ('nicotine_withdrawal', 'weakness'), ('nicotine_patch', 'regeneration'), ('drunk', 'resistance')]]})
write('pack.mcmeta', {'pack': {'pack_format': 34, 'description': 'Create: Drugs resources'}})

for crop in ['cannabis', 'coca', 'tobacco']:
    block = crop + '_crop'
    write(f'assets/{MOD}/blockstates/{block}.json', {'variants': {
        f'age={age}': {'model': f'{MOD}:block/{block}_stage{age}'} for age in range(8)}})
    for age in range(8):
        write(f'assets/{MOD}/models/block/{block}_stage{age}.json', {
            'parent': 'minecraft:block/crop', 'render_type': 'minecraft:cutout',
            'textures': {'crop': f'{MOD}:block/{block}_stage{age}'
                if (ROOT / f'assets/{MOD}/textures/block/{block}_stage{age}.png').exists()
                else f'minecraft:block/wheat_stage{age}'}})
    write(f'data/{MOD}/loot_table/blocks/{block}.json', {
        'type': 'minecraft:block', 'pools': [
            {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': f'{MOD}:{crop}_seeds'}]},
            {'rolls': 1, 'conditions': [{'condition': 'minecraft:block_state_property',
                'block': f'{MOD}:{block}', 'properties': {'age': '7'}}],
             'entries': [{'type': 'minecraft:item', 'name': f'{MOD}:{crop}_leaf',
                'functions': [{'function': 'minecraft:set_count', 'count': 2}]}]},
            {'rolls': 1, 'conditions': [{'condition': 'minecraft:block_state_property',
                'block': f'{MOD}:{block}', 'properties': {'age': '7'}},
                {'condition': 'minecraft:random_chance', 'chance': 0.5}],
             'entries': [{'type': 'minecraft:item', 'name': f'{MOD}:{crop}_seeds'}]}
        ], 'functions': [{'function': 'minecraft:explosion_decay'}]})
write('data/minecraft/tags/block/crops.json', {'replace': False, 'values': [f'{MOD}:{crop}_crop' for crop in ['cannabis', 'coca', 'tobacco']]})
write('data/minecraft/tags/item/villager_plantable_seeds.json', {'replace': False, 'values': [f'{MOD}:{crop}_seeds' for crop in ['cannabis', 'coca', 'tobacco']]})

def ingredient(item):
    return {'item': item if ':' in item else MOD + ':' + item}

def crafting(name, inputs, output, count=1):
    write(f'data/{MOD}/recipe/{name}.json', {
        'type': 'minecraft:crafting_shapeless', 'category': 'misc',
        'ingredients': [ingredient(x) for x in inputs], 'result': {'id': f'{MOD}:{output}', 'count': count}})
    write(f'data/{MOD}/advancement/recipes/{name}.json', {
        'parent': 'minecraft:recipes/root',
        'criteria': {'has_ingredient': {'trigger': 'minecraft:inventory_changed',
            'conditions': {'items': [{'items': [ingredient(inputs[0])['item']]}]}}},
        'requirements': [['has_ingredient']], 'rewards': {'recipes': [f'{MOD}:{name}']}})

def processing(name, kind, inputs, output, count=1, heat=None):
    recipe = {'type': 'create:' + kind, 'ingredients': [ingredient(x) for x in inputs],
              'results': [{'id': f'{MOD}:{output}', 'count': count}]}
    if kind in ['milling', 'mixing', 'crushing']: recipe['processing_time'] = 100
    if heat: recipe['heat_requirement'] = heat
    write(f'data/{MOD}/recipe/{name}.json', recipe)

# Intentionally fantastical game recipes; these are not real manufacturing processes.
crafting('cannabis_seeds', ['minecraft:wheat_seeds', 'minecraft:vine'], 'cannabis_seeds', 2)
crafting('coca_seeds', ['minecraft:wheat_seeds', 'minecraft:cocoa_beans'], 'coca_seeds', 2)
crafting('joint', ['dried_cannabis', 'minecraft:paper'], 'joint')
processing('dry_cannabis', 'haunting', ['cannabis_leaf'], 'dried_cannabis')
processing('mill_coca', 'milling', ['coca_leaf'], 'coca_dust', 2)
processing('crush_coca', 'crushing', ['coca_leaf'], 'coca_dust', 2)
processing('mix_cocaine', 'mixing', ['coca_dust', 'minecraft:glowstone_dust', 'minecraft:sugar'], 'cocaine', 2)
processing('mix_crystal_blend', 'mixing', ['minecraft:amethyst_shard', 'minecraft:redstone', 'minecraft:sugar'], 'crystal_blend', 2, 'heated')
processing('press_methamphetamine', 'pressing', ['crystal_blend'], 'methamphetamine')
processing('press_blotter_paper', 'pressing', ['minecraft:paper'], 'blotter_paper', 2)
processing('mix_lsd', 'mixing', ['blotter_paper', 'minecraft:ender_pearl', 'minecraft:purple_dye'], 'lsd', 2)
processing('deploy_joint', 'deploying', ['dried_cannabis', 'minecraft:paper'], 'joint')
crafting('tobacco_seeds', ['minecraft:wheat_seeds', 'minecraft:brown_mushroom'], 'tobacco_seeds', 2)
processing('dry_tobacco', 'haunting', ['tobacco_leaf'], 'dried_tobacco')
processing('mix_nicotine_blend', 'mixing', ['dried_tobacco', 'minecraft:glowstone_dust'], 'nicotine_blend', 2)
processing('deploy_cigar', 'deploying', ['dried_tobacco', 'tobacco_leaf'], 'cigar')
processing('mix_vape_cartridge', 'mixing', ['nicotine_blend', 'minecraft:amethyst_shard'], 'vape_cartridge', 2)
processing('deploy_nicotine_patch', 'deploying', ['minecraft:paper', 'nicotine_blend'], 'nicotine_patch', 2)
processing('refill_vape', 'deploying', ['vape', 'vape_cartridge'], 'vape')
write(f'data/{MOD}/recipe/mechanical_crafting_vape.json', {
    'type': 'create:mechanical_crafting', 'accept_mirrored': False,
    'pattern': [' I ', ' C ', ' R '], 'key': {'I': {'item': 'create:iron_sheet'},
        'C': {'item': f'{MOD}:vape_cartridge'}, 'R': {'item': 'minecraft:redstone'}},
    'result': {'id': f'{MOD}:vape', 'count': 1}})
processing('press_malt', 'pressing', ['minecraft:wheat'], 'malt', 2)
write(f'data/{MOD}/recipe/mix_beer.json', {
    'type': 'create:mixing', 'heat_requirement': 'heated', 'processing_time': 200,
    'ingredients': [ingredient('malt'), ingredient('minecraft:sugar'), ingredient('minecraft:brown_mushroom'),
        {'type': 'neoforge:single', 'fluid': 'minecraft:water', 'amount': 1000}],
    'results': [{'id': f'{MOD}:beer', 'amount': 1000}]})
write(f'data/{MOD}/recipe/fill_beer_bottle.json', {
    'type': 'create:filling', 'ingredients': [ingredient('minecraft:glass_bottle'),
        {'type': 'neoforge:single', 'fluid': f'{MOD}:beer', 'amount': 250}],
    'results': [{'id': f'{MOD}:beer', 'count': 1}]})
write(f'data/{MOD}/recipe/fill_empty_beer_bottle.json', {
    'type': 'create:filling', 'ingredients': [ingredient(f'{MOD}:empty_beer_bottle'),
        {'type': 'neoforge:single', 'fluid': f'{MOD}:beer', 'amount': 250}],
    'results': [{'id': f'{MOD}:beer', 'count': 1}]})
write(f'assets/{MOD}/blockstates/beer.json', {'variants': {'': {'model': f'{MOD}:block/beer'}}})
write(f'assets/{MOD}/models/block/beer.json', {'textures': {'particle': 'minecraft:block/water_still'}})
crafting('herbalist_table', ['minecraft:crafting_table', 'minecraft:flower_pot', 'cannabis_seeds'], 'herbalist_table')
crafting('dealer_counter', ['minecraft:barrel', 'minecraft:emerald', 'minecraft:paper'], 'dealer_counter')
for name, fallback in [('herbalist_table', 'cartography_table'), ('dealer_counter', 'smithing_table')]:
    custom = ROOT / f'assets/{MOD}/textures/block/{name}.png'
    model = {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{MOD}:block/{name}'}} if custom.exists() else {'parent': f'minecraft:block/{fallback}'}
    write(f'assets/{MOD}/models/block/{name}.json', model)
    write(f'assets/{MOD}/models/item/{name}.json', {'parent': f'{MOD}:block/{name}'})
    write(f'assets/{MOD}/blockstates/{name}.json', {'variants': {'': {'model': f'{MOD}:block/{name}'}}})
    write(f'data/{MOD}/loot_table/blocks/{name}.json', {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
         'entries': [{'type': 'minecraft:item', 'name': f'{MOD}:{name}'}]}]})
write('data/minecraft/tags/point_of_interest_type/acquirable_job_site.json', {
    'replace': False, 'values': [f'{MOD}:herbalist', f'{MOD}:dealer']})
write('data/minecraft/tags/block/mineable/axe.json', {
    'replace': False, 'values': [f'{MOD}:herbalist_table', f'{MOD}:dealer_counter']})
deaths = {
    'withdrawal_1': '%1$s could not go on without nicotine',
    'withdrawal_2': "%1$s's withdrawal finally caught up with them",
    'withdrawal_3': '%1$s should have kept a patch handy',
    'popcorn_lung_1': '%1$s vaped their lungs away',
    'popcorn_lung_2': '%1$s died of popcorn lung',
    'popcorn_lung_3': '%1$s took one puff too many',
    'beer_bottle': '%1$s was hit by a flying beer bottle',
}
for name in deaths:
    write(f'data/{MOD}/damage_type/{name}.json', {'message_id': f'{MOD}.{name}', 'scaling': 'never', 'exhaustion': 0.0})
write('data/minecraft/tags/damage_type/bypasses_armor.json', {'replace': False, 'values': [
    f'{MOD}:{n}' for n in deaths if n != 'beer_bottle']})
write('data/minecraft/tags/damage_type/no_knockback.json', {'replace': False, 'values': [
    f'{MOD}:{n}' for n in deaths if n != 'beer_bottle']})
lang_path = ROOT / f'assets/{MOD}/lang/en_us.json'
lang_now = json.loads(lang_path.read_text(encoding='utf-8'))
for name, text in deaths.items():
    lang_now[f'death.attack.{MOD}.{name}'] = text
lang_now[f'death.attack.{MOD}.beer_bottle.player'] = '%1$s was knocked out by %2$s with a beer bottle'
write(f'assets/{MOD}/lang/en_us.json', lang_now)
print('Generated resources.')
