import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

match = re.search(r'"""رمضان"""\s*to\s*listOf\(.*?\)\s*\)\s*$', content, re.DOTALL)
if match:
    print(f"MATCH! Start: {match.start()} End: {match.end()}")
else:
    print("NO MATCH")
