with open("app/src/main/java/com/example/data/WorshipData.kt", "rb") as f:
    content = f.read()

part1 = content[:24]
# Find the second occurrence of b'ject WorshipData'
idx = content.find(b'ject WorshipData', 28 + 1)
part2 = content[idx:]

with open("app/src/main/java/com/example/data/WorshipData.kt", "wb") as f:
    f.write(part1)
    f.write(part2)
