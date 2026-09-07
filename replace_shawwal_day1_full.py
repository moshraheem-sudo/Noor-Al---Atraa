import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

# We need to find the pair for اعمال اليوم الاول in shawwal.
# Looking at grep, it's lines 911-.. 
# Wait, my previous replacement replaced `Pair("""أعمال اليوم الأول""", """تتوفر قريباً""")` with a huge text, but apparently it was truncated, or maybe not?
# Let's see what is exactly in the file for Pair("""أعمال اليوم الأول"""
