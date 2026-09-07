import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

start_idx = content.find('Pair("""أعمال الليلة الأولى"""')
end_idx = content.find('""")', start_idx) + 4
print(content[start_idx:end_idx])

