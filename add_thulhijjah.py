import re

with open("app/src/main/java/com/example/ui/Screens.kt", "r", encoding="utf-8") as f:
    screens_content = f.read()

screens_content = screens_content.replace('listOf("محرم الحرام", "صفر", "ربيع الأول", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة")', 'listOf("محرم الحرام", "صفر", "ربيع الأول", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")')

with open("app/src/main/java/com/example/ui/Screens.kt", "w", encoding="utf-8") as f:
    f.write(screens_content)

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    worship_content = f.read()

target = '        ),\n    )'

replacement = '''        ),
        """ذو الحجة""" to listOf(
            Pair("""أعمال اليوم الأول""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الثاني""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الثالث""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الرابع""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الخامس""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم السادس""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم السابع""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الثامن""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم التاسع""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم العاشر""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم السابع عشر""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الثامن عشر""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الرابع والعشرون""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الخامس والعشرون""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الثلاثون""", """تتوفر قريباً""")
        ),
    )'''

worship_content = worship_content.replace(target, replacement)

with open("app/src/main/java/com/example/data/WorshipData.kt", "w", encoding="utf-8") as f:
    f.write(worship_content)

print("SUCCESS")
