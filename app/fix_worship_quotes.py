with open('app/src/main/java/com/example/data/WorshipData.kt', 'r', encoding='utf-8', errors='ignore') as f:
    text = f.read()

# Fix 1: remove trailing triple quotes after """),
text = text.replace('"""),"""', '"""),')

# Fix 2: fix prepended bismillah triple quotes
text = text.replace('"""بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ"""', '"""بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\n')
text = text.replace('"""بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحيم"""', '"""بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحيم\n')

with open('app/src/main/java/com/example/data/WorshipData.kt', 'w', encoding='utf-8') as f:
    f.write(text)

print('Updated WorshipData.kt successfully')
