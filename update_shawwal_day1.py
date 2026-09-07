import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """وَلا عَاجِزٌ عَمَّا تُسْأَلُ ، وَأَنْتَ عَلَى كُلِّ شَيْءٍ قَدِيْرٌ ، وَلا حَوْلَ وَلا قُوَّةَ إِلَّا بِاللهِ الْعَلِيِّ الْعَظِيمِ .
*المصدر : الصحيفة السجّادية .\"\"\")"""

replacement = """وَلا عَاجِزٌ عَمَّا تُسْأَلُ ، وَأَنْتَ عَلَى كُلِّ شَيْءٍ قَدِيْرٌ ، وَلا حَوْلَ وَلا قُوَّةَ إِلَّا بِاللهِ الْعَلِيِّ الْعَظِيمِ .
*المصدر : الصحيفة السجّادية .
بواسطة تطبيق حقيبة المؤمن 
#تطبيق_حقيبة_المؤمن @haqybatelmomen\"\"\")"""

new_content = content.replace(target, replacement, 1)

with open("app/src/main/java/com/example/data/WorshipData.kt", "w", encoding="utf-8") as f:
    f.write(new_content)

print("SUCCESS")
