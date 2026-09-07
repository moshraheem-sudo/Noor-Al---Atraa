import re

with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8', errors='ignore') as f:
    text = f.read()

# Let's see all occurrences of Pair("""...""", """...""") or broken ones.
lines = text.split('\n')

for idx, l in enumerate(lines[:30]):
    print(f"{idx+1}: {repr(l)}")
