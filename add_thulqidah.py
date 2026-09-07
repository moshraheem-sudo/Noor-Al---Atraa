import re

with open("app/src/main/java/com/example/ui/Screens.kt", "r", encoding="utf-8") as f:
    screens_content = f.read()

screens_content = screens_content.replace('listOf("محرم الحرام", "صفر", "ربيع الأول", "رجب", "شعبان", "رمضان", "شوال")', 'listOf("محرم الحرام", "صفر", "ربيع الأول", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة")')

with open("app/src/main/java/com/example/ui/Screens.kt", "w", encoding="utf-8") as f:
    f.write(screens_content)

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    worship_content = f.read()

target = '        ),\n    )'

replacement = '''        ),
        """ذو القعدة""" to listOf(
            Pair("""الأعمال العامة""", """تتوفر قريباً"""),
            Pair("""اليوم الحادي عشر""", """تتوفر قريباً"""),
            Pair("""اللّيلة الخامسة عشرة""", """تتوفر قريباً"""),
            Pair("""اليوم الثّالِثُ والعِشرون""", """تتوفر قريباً"""),
            Pair("""أعمال اليوم الخامس""", """تتوفر قريباً"""),
            Pair("""اليوم الثامن عشر إلى آخر كل شهر""", """تتوفر قريباً""")
        ),
    )'''

worship_content = worship_content.replace(target, replacement)

with open("app/src/main/java/com/example/data/WorshipData.kt", "w", encoding="utf-8") as f:
    f.write(worship_content)

print("SUCCESS")
