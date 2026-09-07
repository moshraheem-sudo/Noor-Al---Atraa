with open('app/src/main/java/com/example/ui/Screens.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('} else if (selectedCategory == "المناجاة") {', '} else if (selectedCategory == "مناجاة" || selectedCategory == "المناجاة") {')
content = content.replace('} else if (selectedCategory == "صلاة أهل البيت (ع)") {', '} else if (selectedCategory == "صلاة اهل البيت عليهم السلام" || selectedCategory == "صلاة أهل البيت (ع)") {')
content = content.replace('} else if (selectedCategory == "الدعوات النافعة والمختصرة") {', '} else if (selectedCategory == "الادعية المختارة" || selectedCategory == "الدعوات النافعة والمختصرة") {')
content = content.replace('when (selectedCategory) {\n                    "الأدعية" -> {', 'when (selectedCategory) {\n                    "الادعية", "الأدعية" -> {')
content = content.replace('when (selectedCategory) {\n                    "الزيارات" -> {', 'when (selectedCategory) {\n                    "الزيارات" -> {')
content = content.replace('when (selectedCategory) {\n                    "الاعمال", "الأعمال" -> {', 'when (selectedCategory) {\n                    "الاعمال", "الأعمال" -> {')
content = content.replace('when (selectedCategory) {\n                    "الصحيفة السجادية" -> {', 'when (selectedCategory) {\n                    "الصحيفة السجادية" -> {')

# Let's fix the "الأدعية" vs "الادعية" which is inside the `when` expression.
content = content.replace('"الأدعية" -> {', '"الادعية", "الأدعية" -> {')
content = content.replace('"الاذكار", "الأذكار" -> {', '"الاذكار", "أذكار" -> {') # wait, did I use أذكار or الاذكار? I used "أذكار"
content = content.replace('"الأعمال" -> {', '"الاعمال", "الأعمال" -> {')
content = content.replace('"الاذكار" -> {', '"أذكار", "الاذكار" -> {')

with open('app/src/main/java/com/example/ui/Screens.kt', 'w', encoding='utf-8') as f:
    f.write(content)
