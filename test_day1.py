with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

start = content.find('Pair("""أعمال اليوم الأول""", """يوم عيد الفطر')
if start == -1:
    print("Not found")
else:
    end = content.find('""")', start)
    text = content[start:end+4]
    print(text[-200:])
