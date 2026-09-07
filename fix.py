with open('app/src/main/java/com/example/data/WorshipData.kt', 'rb') as f:
    text = f.read().decode('utf-8', errors='replace')
    
text = text.replace('رُكَّعٌ ."""),"""', 'رُكَّعٌ ."""),')
with open('app/src/main/java/com/example/data/WorshipData.kt', 'wb') as f:
    f.write(text.encode('utf-8'))
