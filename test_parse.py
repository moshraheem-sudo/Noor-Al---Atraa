import re

with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8') as f:
    text = f.read()

# Let's extract the "شعبان" block
match = re.search(r'("شعبان" to listOf\((.*?)\n        \))', text, re.DOTALL)
if match:
    print(f"Found! Length: {len(match.group(2))}")
    print(match.group(2)[-500:])
else:
    print("Not found")

