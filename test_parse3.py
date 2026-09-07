import re

with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8') as f:
    text = f.read()

from parse_and_insert import shaban_append

target = 'يَا أَحْكَمَ الْحَاكِمِينَ  » .""")\n        )'
if target in text:
    print("Target found!")
    new_text = text.replace(target, 'يَا أَحْكَمَ الْحَاكِمِينَ  » ."""),\n' + shaban_append + '        )')
    with open('app/src/main/java/com/example/data/WorshipData.kt', 'w', encoding='utf-8') as f:
        f.write(new_text)
    print("Replaced and saved!")
else:
    print("Target not found")
