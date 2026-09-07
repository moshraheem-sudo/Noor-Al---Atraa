with open('app/src/main/java/com/example/ui/Screens.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I want to specifically target the labelSmall inside the WorshipScreen grid.
content = content.replace('style = MaterialTheme.typography.labelSmall,', 'style = MaterialTheme.typography.labelMedium,')

with open('app/src/main/java/com/example/ui/Screens.kt', 'w', encoding='utf-8') as f:
    f.write(content)
