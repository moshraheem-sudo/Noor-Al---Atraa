with open('app/src/main/java/com/example/ui/Screens.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('.size(64.dp)', '.size(76.dp)')
content = content.replace('.size(32.dp)', '.size(40.dp)')

with open('app/src/main/java/com/example/ui/Screens.kt', 'w', encoding='utf-8') as f:
    f.write(content)

