import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Remove the watermark if it exists
watermark1 = "بواسطة تطبيق حقيبة المؤمن \n#تطبيق_حقيبة_المؤمن @haqybatelmomen"
watermark2 = "بواسطة تطبيق حقيبة المؤمن \n#تطبيق_حقيبة_المؤمن @haqybatelmomen\n"
watermark3 = "\nبواسطة تطبيق حقيبة المؤمن \n#تطبيق_حقيبة_المؤمن @haqybatelmomen"

content = content.replace(watermark1, "")
content = content.replace(watermark2, "")
content = content.replace(watermark3, "")

with open("app/src/main/java/com/example/data/WorshipData.kt", "w", encoding="utf-8") as f:
    f.write(content)

print("Removed watermark")
