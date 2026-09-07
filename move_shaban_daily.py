import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

# 1. Find the shaban section start
shaban_start_match = re.search(r'"""شعبان"""\s*to\s*listOf\(', content)
if not shaban_start_match:
    print("Could not find shaban start")
    exit(1)
shaban_start_index = shaban_start_match.end()

# 2. Extract "الأعمال اليومية لشهر شعبان"
# Looking for `Pair("""الأعمال اليومية لشهر شعبان""", """..."""),`
pattern_daily = r'\n\s*Pair\("""الأعمال اليومية لشهر شعبان""",\s*""".*?"""\),?'
daily_match = re.search(pattern_daily, content[shaban_start_index:], re.DOTALL)

if not daily_match:
    print("Could not find daily match")
    exit(1)

daily_str = daily_match.group(0)

# 3. Remove it from the original content
# Let's replace ONLY the first occurrence AFTER the shaban start index
part1 = content[:shaban_start_index]
part2 = content[shaban_start_index:]
part2 = part2.replace(daily_str, "", 1)

# 4. Insert it at the start of shaban list
# But ensure there is a newline after the insertion, and proper formatting
final_content = part1 + daily_str + part2

with open("app/src/main/java/com/example/data/WorshipData.kt", "w", encoding="utf-8") as f:
    f.write(final_content)

print("SUCCESS")
