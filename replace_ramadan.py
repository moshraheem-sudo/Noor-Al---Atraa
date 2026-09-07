import re

with open("app/src/main/java/com/example/data/WorshipData.kt", "r", encoding="utf-8") as f:
    content = f.read()

# The format is typically:
# """رمضان""" to listOf(
#     Pair(...),
#     Pair(...)
# ),
# """next_key""" to listOf(

# We can match `"""رمضان""" to listOf(` up to the matching closing `),` that precedes the next `"""something""" to listOf` or the end of the `mapOf(` 
# It's safer to use regex that matches `"""رمضان""" to listOf\((.*?)\n\s*\)(?=\s*,\s*"""|\s*\)$)`

pattern = r'("""رمضان"""\s*to\s*listOf\()(.*?)(\n\s*\))'
# Wait, this might match too little or too much depending on nested parenthesis.
# Let's count parentheses instead.

start_idx = content.find('"""رمضان""" to listOf(')
if start_idx == -1:
    print("Cannot find رمضان section")
    exit(1)

# Find the start of the list
list_start = content.find('(', start_idx)
paren_count = 1
idx = list_start + 1

while idx < len(content) and paren_count > 0:
    if content[idx] == '(':
        paren_count += 1
    elif content[idx] == ')':
        paren_count -= 1
    idx += 1

if paren_count == 0:
    # idx is now the index right after the closing parenthesis of the listOf
    end_idx = idx
    new_ramadan = '"""رمضان""" to listOf(\n            Pair("""شهر رمضان""", """تتوفر قريباً""")\n        )'
    new_content = content[:start_idx] + new_ramadan + content[end_idx:]
    with open("app/src/main/java/com/example/data/WorshipData.kt", "w", encoding="utf-8") as f:
        f.write(new_content)
    print("Replaced successfully")
else:
    print("Failed to find matching parenthesis")

