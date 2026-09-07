from parse_and_insert import ramadhan_insert

with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8') as f:
    text = f.read()

target = 'يا أرحَمَ الرَّاحِمينَ.""")\n    )\n}'

if target in text:
    print("Found end of WorshipData!")
    new_text = text.replace(target, 'يا أرحَمَ الرَّاحِمينَ.""")\n,\n' + ramadhan_insert + '\n    )\n}')
    with open('app/src/main/java/com/example/data/WorshipData.kt', 'w', encoding='utf-8') as f:
        f.write(new_text)
    print("Inserted ramadhan_insert!")
else:
    print("Target not found")
