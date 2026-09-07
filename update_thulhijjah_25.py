import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

replacement = """            Pair(\"\"\"أعمال اليوم الخامس والعشرون\"\"\", \"\"\"الصدقة والصيام في اليوم الخامس والعشرين من ذي الحجة: يوم شريف وهو اليوم الذي نزلت فيه سورة هل أتى في شأن أهل البيت (عليهم السلام) لأنّهم كانوا قد صاموا ثلاثة أيام وأعطوا فطورهم مسكيناً ويتيماً وأسيراً وأفطروا على الماء، وينبغي على شيعة أهل البيت (عليهم السلام) في هذه الأيام ولاسيّما في الليلة الخامسة والعشرين أن يتأسّوا بمولاهم في التصدّق على المساكين والأيتام وأن يجتهدوا في إطعامهم وأن يصوموا هذا اليوم. وعند بعض العلماء إنّ هذا اليوم هو يوم المباهلة فمن المناسب أن يقرأ فيه أيضاً زيارة الجامعة، ودعاء المباهلة.
*المصدر:مفاتيح الجنان.\"\"\")"""

lines = content.splitlines()
found_dhul_hijjah = False
new_lines = []
for line in lines:
    if '\"\"\"ذو الحجة\"\"\"' in line:
        found_dhul_hijjah = True
    
    if found_dhul_hijjah and 'Pair(\"\"\"أعمال اليوم الخامس والعشرون\"\"\", \"\"\"تتوفر قريباً\"\"\")' in line:
        new_lines.append(replacement)
        found_dhul_hijjah = False # to prevent further matches if any
    else:
        new_lines.append(line)

new_content = "\n".join(new_lines)
with open("app/src/main/java/com/example/data/WorshipData.kt", "w", encoding="utf-8") as f:
    f.write(new_content)

print("SUCCESS")
