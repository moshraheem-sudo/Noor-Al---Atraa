import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """العاشر : قال الشّيخ في المصباح : اغتسل في آخر اللّيل واجلس في مصلاّك إلى طلوع الفجر .\"\"\"),"""

replacement = """العاشر : قال الشّيخ في المصباح : اغتسل في آخر اللّيل واجلس في مصلاّك إلى طلوع الفجر .
بواسطة تطبيق حقيبة المؤمن 
#تطبيق_حقيبة_المؤمن @haqybatelmomen\"\"\"),"""

new_content = content.replace(target, replacement, 1)

with open("app/src/main/java/com/example/data/WorshipData.kt", "w", encoding="utf-8") as f:
    f.write(new_content)

print("SUCCESS")
