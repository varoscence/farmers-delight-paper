#!/usr/bin/env python3
"""Builds the Farmer's Delight resource pack ZIP from FDItems.java and downloaded textures."""

import re, json, os, sys, zipfile, hashlib
from collections import defaultdict

out_zip = sys.argv[1] if len(sys.argv) > 1 else 'pack.zip'

with open('src/main/java/com/ashblossom/farmersdelight/items/FDItems.java') as f:
    src = f.read()

# Extract (id, MATERIAL, cmd) from enum entries
items = re.findall(
    r'[A-Z_]+\(\s*"([a-z_0-9]+)"\s*,\s*"[^"]+"\s*,\s*Material\.([A-Z_]+)\s*,\s*(\d+)',
    src
)
print(f"Found {len(items)} items")

by_material = defaultdict(list)
for id_, mat, cmd in items:
    by_material[mat].append((id_, int(cmd)))

entries = {}  # zip path -> bytes

# pack.mcmeta
with open('src/main/resources/pack/pack.mcmeta', 'rb') as f:
    entries['pack.mcmeta'] = f.read()

# Model files (standard parent/textures format)
for id_, mat, cmd in items:
    model = {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"farmersdelight:item/{id_}"}
    }
    entries[f'assets/farmersdelight/models/item/{id_}.json'] = json.dumps(model, indent=2).encode()

# Item definition files (one per base material, CMD dispatch)
for mat, mat_items in by_material.items():
    mat_lower = mat.lower()
    cases = [
        {
            "when": cmd,  # integer — required in 1.21.5+ (was string array ["N"] in 1.21.4)
            "model": {"type": "minecraft:model", "model": f"farmersdelight:item/{id_}"}
        }
        for id_, cmd in mat_items
    ]
    defn = {
        "model": {
            "type": "minecraft:select",
            "property": "minecraft:custom_model_data",
            "cases": cases,
            "fallback": {"type": "minecraft:model", "model": f"minecraft:item/{mat_lower}"}
        }
    }
    entries[f'assets/minecraft/items/{mat_lower}.json'] = json.dumps(defn, indent=2).encode()
    print(f"  {mat_lower}.json: {len(mat_items)} items")

# Textures
tex_dir = 'src/main/resources/textures/item'
found, missing = 0, 0
for id_, mat, cmd in items:
    path = os.path.join(tex_dir, f'{id_}.png')
    if os.path.exists(path):
        with open(path, 'rb') as f:
            entries[f'assets/farmersdelight/textures/item/{id_}.png'] = f.read()
        found += 1
    else:
        missing += 1
print(f"Textures: {found} found, {missing} missing")

# Write ZIP
with zipfile.ZipFile(out_zip, 'w', zipfile.ZIP_DEFLATED) as zf:
    for path, data in sorted(entries.items()):
        zf.writestr(path, data)

# SHA1
with open(out_zip, 'rb') as f:
    sha1 = hashlib.sha1(f.read()).hexdigest()

sha1_file = out_zip.replace('.zip', '.sha1')
with open(sha1_file, 'w') as f:
    f.write(sha1)

print(f"Pack: {out_zip} ({os.path.getsize(out_zip)} bytes)")
print(f"SHA1: {sha1}")
