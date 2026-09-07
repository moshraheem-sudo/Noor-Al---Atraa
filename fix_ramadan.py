import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Find the start of Ramadan list
ramadan_start = content.find('        "رمضان" to listOf(')
ramadan_end = content.find('        )', ramadan_start) + 9

# Extract the text of Ramadan entries
ramadan_block = content[ramadan_start:ramadan_end]

# We need to extract just the text values from Pair(..., "text") from the ramadan block
# Actually, the user wants me to concatenate all of them to the end of "أعمال اليوم الثلاثين" in sha'ban.

# The current end of Sha'ban's اعمال اليوم الثلاثين:
# it ends at line 634: وَسَهِّل لَّنَا فِيهِ إيتَاءَ الزَّكَاةِ ،""")

parts = []
# Just grab the raw string from line 635 to 664.
lines = content.split('\n')
text_to_append = "\n" + "\n".join(lines[635:664]) + "\n"

# Remove the Pair(...) prefix and trailing """) from the extracted lines so it flows as one text.
text_to_append = re.sub(r'^\s*Pair\("[^"]+", """', '', text_to_append, flags=re.MULTILINE)
text_to_append = text_to_append.replace('""")', '')

# Replace the closing tag of اعمال اليوم الثلاثين
old_ending = 'وَسَهِّل لَّنَا فِيهِ إيتَاءَ الزَّكَاةِ ،""")'
new_ending = 'وَسَهِّل لَّنَا فِيهِ إيتَاءَ الزَّكَاةِ ،' + text_to_append + '""")'

content = content.replace(old_ending, new_ending)

# Replace the ramadan block with an empty list
content = content.replace(ramadan_block, '        "رمضان" to listOf(\n        )')

with open("app/src/main/java/com/example/data/WorshipData.kt", "w", encoding="utf-8") as f:
    f.write(content)

