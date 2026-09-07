import re

with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8') as f:
    text = f.read()

match = re.search(r'("شعبان" to listOf\((.*?)\n        \))', text, re.DOTALL)
if match:
    print(match.group(2)[-1000:])
