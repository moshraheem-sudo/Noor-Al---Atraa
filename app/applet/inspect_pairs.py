import re

with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pairs = list(re.finditer(r'Pair\s*\(\s*"""', content))
print('Total Pair(""" found:', len(pairs))

# Let's inspect the first 5 Pairs
for idx, m in enumerate(pairs[:5]):
    start = m.start()
    end = min(len(content), start + 300)
    print(f"--- Pair {idx+1} ---")
    print(repr(content[start:end]))
