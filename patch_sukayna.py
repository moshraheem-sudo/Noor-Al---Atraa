import re

with open("app/src/main/java/com/example/data/AhlAlBaytRepository.kt", "r", encoding="utf-8") as f:
    content = f.read()

ruqayya_entry = """        Person(
            id = 17,
            name = "رقية بنت الحسين (عليها السلام)",
            title = "يتيمة الحسين، عزيزة الحسين، شبيهة فاطمة",
            father = "الإمام الحسين بن علي",
            mother = "أم إسحاق بنت طلحة",
            birthDate = null,
            deathDate = HijriDate(5, 2, 61),
            imamateDuration = null,
            causeOfDeath = "توفيت حزناً وكمداً على أبيها في خربة الشام",
            killer = "ظلم بني أمية",
            childrenCount = null,
            famousChildren = emptyList(),
            wives = emptyList(),
            bio = "بنت الإمام الحسين (عليه السلام)، شهدت فاجعة كربلاء وهي طفلة صغيرة (حوالي 3 أو 4 سنوات)، وأسرت مع عماتها إلى الشام. توفيت في الخربة حزناً بعد رؤية رأس أبيها المقطوع. مرقدها الشريف في دمشق مزار يقصده الملايين."
        )"""

sukayna_entry = """        Person(
            id = 17,
            name = "رقية بنت الحسين (عليها السلام)",
            title = "يتيمة الحسين، عزيزة الحسين، شبيهة فاطمة",
            father = "الإمام الحسين بن علي",
            mother = "أم إسحاق بنت طلحة",
            birthDate = null,
            deathDate = HijriDate(5, 2, 61),
            imamateDuration = null,
            causeOfDeath = "توفيت حزناً وكمداً على أبيها في خربة الشام",
            killer = "ظلم بني أمية",
            childrenCount = null,
            famousChildren = emptyList(),
            wives = emptyList(),
            bio = "بنت الإمام الحسين (عليه السلام)، شهدت فاجعة كربلاء وهي طفلة صغيرة (حوالي 3 أو 4 سنوات)، وأسرت مع عماتها إلى الشام. توفيت في الخربة حزناً بعد رؤية رأس أبيها المقطوع. مرقدها الشريف في دمشق مزار يقصده الملايين."
        ),
        Person(
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

content = content.replace(ruqayya_entry, sukayna_entry)

with open("app/src/main/java/com/example/data/AhlAlBaytRepository.kt", "w", encoding="utf-8") as f:
    f.write(content)
