import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

pattern = r'(Pair\("""أعمال اليوم الثلاثين""",\s*""")(.*?)(""")'
match = re.search(pattern, content, re.DOTALL)
if match:
    print("MATCH FOUND")
    print("Start index of group 2:", match.start(2))
    print("End index of group 2:", match.end(2))
else:
    print("NO MATCH")
