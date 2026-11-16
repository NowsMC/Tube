# Lists the org.bukkit classes and members each plugin jar references.
# usage: python scan-plugins.py <plugin jar folder> <output scan.json>
import json
import os
import re
import struct
import sys
import zipfile
from collections import Counter, defaultdict

PLUGINS = sys.argv[1]
OUT = sys.argv[2]

PREFIXES = {
    'bukkit': 'org/bukkit/',
    'craftbukkit': 'org/bukkit/craftbukkit/',
    'spigot': 'org/spigotmc/',
    'paper': 'io/papermc/paper/',
    'paperold': 'com/destroystokyo/paper/',
    'folia': 'io/papermc/paper/threadedregions/',
    'nms': 'net/minecraft/',
    'netty': 'io/netty/',
    'protocollib': 'com/comphenix/protocol/',
    'adventure': 'net/kyori/',
    'authlib': 'com/mojang/',
}


def parse_class(data):
    if data[:4] != b'\xca\xfe\xba\xbe':
        return None
    count = struct.unpack('>H', data[8:10])[0]
    pool = [None] * count
    i = 10
    n = 1
    while n < count:
        tag = data[i]
        if tag == 1:
            length = struct.unpack('>H', data[i + 1:i + 3])[0]
            pool[n] = ('utf8', data[i + 3:i + 3 + length].decode('utf-8', 'replace'))
            i += 3 + length
        elif tag in (3, 4):
            i += 5
        elif tag in (5, 6):
            i += 9
            n += 1
        elif tag == 7:
            pool[n] = ('class', struct.unpack('>H', data[i + 1:i + 3])[0])
            i += 3
        elif tag == 8 or tag == 16 or tag == 19 or tag == 20:
            i += 3
        elif tag in (9, 10, 11):
            pool[n] = ('ref', struct.unpack('>HH', data[i + 1:i + 5]))
            i += 5
        elif tag == 12:
            pool[n] = ('nat', struct.unpack('>HH', data[i + 1:i + 5]))
            i += 5
        elif tag == 15:
            i += 4
        elif tag in (17, 18):
            i += 5
        else:
            return None
        n += 1
    return pool


def utf8(pool, index):
    entry = pool[index]
    return entry[1] if entry and entry[0] == 'utf8' else ''


def references(pool):
    classes = set()
    members = set()
    for entry in pool:
        if not entry:
            continue
        if entry[0] == 'class':
            classes.add(utf8(pool, entry[1]))
        elif entry[0] == 'ref':
            owner = pool[entry[1][0]]
            nat = pool[entry[1][1]]
            if owner and nat and owner[0] == 'class' and nat[0] == 'nat':
                members.add((utf8(pool, owner[1]), utf8(pool, nat[1][0]), utf8(pool, nat[1][1])))
    return classes, members


def read_yaml_text(text, key):
    match = re.search(r'^' + re.escape(key) + r':\s*(.+)$', text, re.M)
    return match.group(1).strip() if match else None


report = {}
class_users = defaultdict(set)
member_users = defaultdict(set)

for name in sorted(os.listdir(PLUGINS)):
    if not name.endswith('.jar'):
        continue
    path = os.path.join(PLUGINS, name)
    info = {'size': os.path.getsize(path)}
    try:
        with zipfile.ZipFile(path) as jar:
            names = set(jar.namelist())
            own = {n[:-6] for n in names if n.endswith('.class')}
            for descriptor in ('plugin.yml', 'paper-plugin.yml', 'fabric.mod.json', 'META-INF/mods.toml', 'velocity-plugin.json', 'bungee.yml'):
                if descriptor in names:
                    info.setdefault('descriptors', []).append(descriptor)
                    if descriptor.endswith('.yml'):
                        text = jar.read(descriptor).decode('utf-8', 'replace')
                        info[descriptor] = {k: read_yaml_text(text, k) for k in ('name', 'version', 'main', 'api-version', 'folia-supported', 'depend', 'softdepend', 'load', 'bootstrapper', 'loader')}
            counts = Counter()
            bukkit_classes = set()
            bukkit_members = set()
            for entry in names:
                if not entry.endswith('.class'):
                    continue
                pool = parse_class(jar.read(entry))
                if not pool:
                    continue
                classes, members = references(pool)
                for cls in classes:
                    if cls in own:
                        continue
                    for label, prefix in PREFIXES.items():
                        if cls.startswith(prefix):
                            counts[label] += 1
                    if cls.startswith('org/bukkit/'):
                        bukkit_classes.add(cls)
                for owner, member, desc in members:
                    if owner in own:
                        continue
                    if owner.startswith('org/bukkit/'):
                        bukkit_members.add((owner, member, desc))
            info['refs'] = dict(counts)
            info['bukkit_class_count'] = len(bukkit_classes)
            info['bukkit_member_count'] = len(bukkit_members)
            for cls in bukkit_classes:
                class_users[cls].add(name)
            for member in bukkit_members:
                member_users[member].add(name)
    except Exception as error:
        info['error'] = str(error)
    report[name] = info

summary = {
    'plugins': report,
    'classes': {cls: sorted(users) for cls, users in class_users.items()},
    'members': {'%s#%s%s' % key: sorted(users) for key, users in member_users.items()},
}
json.dump(summary, open(OUT, 'w'), indent=1)
print(len(report), 'jars,', len(class_users), 'bukkit classes,', len(member_users), 'bukkit members')
