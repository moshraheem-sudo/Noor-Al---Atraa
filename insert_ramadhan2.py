from parse_and_insert import ramadhan_insert

with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    new_lines.append(line)
    if "صلاة ليلة الثلاثين من شهر شعبان" in line:
        pass # just to find where we are

with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8') as f:
    text = f.read()

target = 'صلاة ليلة الثلاثين من شهر شعبان : عن النبيّ ـ صلّى الله عليه وآله ـ قال : من صلّى ليلة الثلاثين من شعبان ركعتين ، يقرأ في كل ركعة فاتحة الكتاب و( سبح اسم ربك الأعلى ) عشر مرات ، فإذا فرغ من صلاته صلّى على النبيّ ـ صلى الله عليه وآله ـ مائة مرة ، فو الذي بعثني بالحق نبيًّا إن الله يرفع له ألف ألف مدينة في جنة النعيم ولو اجتمع أهل السماوات والأرض على إحصاء ثوابه ما قدروا ، وقضى الله له ألف حاجة .""")\n        )'

if target in text:
    new_text = text.replace(target, target + ',\n        "رمضان" to listOf(\n' + ramadhan_insert + '\n        )')
    with open('app/src/main/java/com/example/data/WorshipData.kt', 'w', encoding='utf-8') as f:
        f.write(new_text)
    print("Inserted Ramadan successfully!")
else:
    print("Target not found!")
