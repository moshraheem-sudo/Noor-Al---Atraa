import re

with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace unwanted strings
content = content.replace("بواسطة تطبيق حقيبة المؤمن", "")
content = content.replace("#تطبيق_حقيبة_المؤمن @haqybatelmomen", "")
content = content.replace("بواسطة تطبيق حقيبة المؤمن ", "")

# Fix any leftover empty lines or double newlines caused by the removal
content = re.sub(r'\n\s*\n\s*\n', '\n\n', content)

# I should also fix the titles for "شعبان" if there are any typos. 
# "أعمال الليله الخامس" -> "صلاة الليلة الخامسة" ?
# The titles currently are:
# "اليوم الأول"
# "صلاة الليلة الثانية"
# "الأعمال اليومية لشهر شعبان"
# "أعمال اليوم الثاني"
# "صلاة الليلة الثالثة"
# "أعمال اليوم الثالث"
# "أعمال صلاة يوم الرابع" -> "صلاة الليلة الرابعة"
# "أعمال الليله الخامس" -> "صلاة الليلة الخامسة"
# "أعمال الليلة السادسة" -> "صلاة الليلة السادسة"
# "اليلة السابعة" -> "صلاة الليلة السابعة"
# "صلاة الليلة الثامنه" -> "صلاة الليلة الثامنة"
# "صلاة الليلة التاسعة"
# "صلاة الليلة العاشرة"
# "صلاة الليلة الحادية عشرة"
# "صلاة الليلة الثانية عشرة"
# "أعمال اليوم الثاني عشر" (But the text says "صلاة الليلة الثالثة عشرة")
# "أعمال اليوم الثالث عشر" (Text says "صلاة الليلة الرابعة عشرة")
# "اعمال اليوم الرابع عشر" (Text says "صلاة الليلة الخامسة عشرة")

# Let's fix the Pair keys to be more appropriate and consistent
replacements = {
    'Pair("أعمال صلاة يوم الرابع",': 'Pair("صلاة الليلة الرابعة",',
    'Pair("أعمال الليله الخامس",': 'Pair("صلاة الليلة الخامسة",',
    'Pair("أعمال الليلة السادسة",': 'Pair("صلاة الليلة السادسة",',
    'Pair("اليلة السابعة",': 'Pair("صلاة الليلة السابعة",',
    'Pair("صلاة الليلة الثامنه",': 'Pair("صلاة الليلة الثامنة",',
    'Pair("أعمال اليوم الثاني عشر",': 'Pair("صلاة الليلة الثالثة عشرة",',
    'Pair("أعمال اليوم الثالث عشر",': 'Pair("صلاة الليلة الرابعة عشرة",',
    'Pair("اعمال اليوم الرابع عشر",': 'Pair("صلاة الليلة الخامسة عشرة",',
}

for old, new in replacements.items():
    content = content.replace(old, new)

with open('app/src/main/java/com/example/data/WorshipData.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Done cleaning.")
