import re

with open("app/src/main/java/com/example/data/AhlAlBaytRepository.kt", "r", encoding="utf-8") as f:
    content = f.read()

sukayna_entry = """        Person(
            id = 18,
            name = "سكينة بنت الحسين (عليها السلام)",
            title = "آمنة، أميمة، عقيلة قريش",
            father = "الإمام الحسين بن علي",
            mother = "الرباب بنت امرئ القيس",
            birthDate = null,
            deathDate = HijriDate(5, 3, 117),
            imamateDuration = null,
            causeOfDeath = "أسباب طبيعية",
            killer = null,
            childrenCount = null,
            famousChildren = emptyList(),
            wives = listOf("عبد الله بن الحسن (الزوج)"),
            bio = "ابنة الإمام الحسين (عليه السلام) والرباب. السيدة الجليلة العالمة والعابدة، حضرت واقعة الطف وشهدت استشهاد أبيها وإخوتها، وسُبيت مع عمّتها الحوراء زينب (عليها السلام) إلى الشام. عُرفت بالفصاحة والبلاغة، وكان الإمام الحسين يحبها حباً شديداً وقال فيها: (لعمرك إنني لأحب داراً.. تحل بها سكينة والرباب). ينفي محققو الشيعة الروايات الموضوعة حول تعدد أزواجها، ويؤكدون أنها لم تتزوج سوى ابن عمها عبد الله بن الحسن الذي استشهد بكربلاء. توفيت في المدينة المنورة."
        )"""

zayd_entry = """        Person(
            id = 18,
            name = "سكينة بنت الحسين (عليها السلام)",
            title = "آمنة، أميمة، عقيلة قريش",
            father = "الإمام الحسين بن علي",
            mother = "الرباب بنت امرئ القيس",
            birthDate = null,
            deathDate = HijriDate(5, 3, 117),
            imamateDuration = null,
            causeOfDeath = "أسباب طبيعية",
            killer = null,
            childrenCount = null,
            famousChildren = emptyList(),
            wives = listOf("عبد الله بن الحسن (الزوج)"),
            bio = "ابنة الإمام الحسين (عليه السلام) والرباب. السيدة الجليلة العالمة والعابدة، حضرت واقعة الطف وشهدت استشهاد أبيها وإخوتها، وسُبيت مع عمّتها الحوراء زينب (عليها السلام) إلى الشام. عُرفت بالفصاحة والبلاغة، وكان الإمام الحسين يحبها حباً شديداً وقال فيها: (لعمرك إنني لأحب داراً.. تحل بها سكينة والرباب). ينفي محققو الشيعة الروايات الموضوعة حول تعدد أزواجها، ويؤكدون أنها لم تتزوج سوى ابن عمها عبد الله بن الحسن الذي استشهد بكربلاء. توفيت في المدينة المنورة."
        ),
        Person(
            id = 19,
            name = "زيد بن علي (عليهما السلام)",
            title = "زيد الشهيد، حليف القرآن",
            father = "الإمام علي بن الحسين زين العابدين",
            mother = "جيدا (أم ولد)",
            birthDate = null,
            deathDate = HijriDate(2, 2, 122),
            imamateDuration = null,
            causeOfDeath = "استشهد وصُلب بعد خروجه وثورته بالكوفة",
            killer = "جيش هشام بن عبد الملك بقيادة يوسف بن عمر",
            childrenCount = 4,
            famousChildren = listOf("يحيى", "الحسين", "محمد", "عيسى"),
            wives = emptyList(),
            bio = "هو ابن الإمام زين العابدين (عليه السلام) ومن أصحاب الإمامين الباقر والصادق (عليهما السلام). عالمٌ وفقيه وعابد وشجاع وبليغ، والمشهور أنه ولد سنة 75 هـ. خرج على حكم بني أمية داعيًا إلى الحق والعدل، وثار في الكوفة، ثم غُدر به وقُتل وصُلب."
        )"""

content = content.replace(sukayna_entry, zayd_entry)

with open("app/src/main/java/com/example/data/AhlAlBaytRepository.kt", "w", encoding="utf-8") as f:
    f.write(content)
