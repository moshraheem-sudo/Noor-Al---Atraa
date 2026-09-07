with open('app/src/main/java/com/example/ui/Screens.kt', 'r', encoding='utf-8') as f:
    content = f.read()

start_str = "val gridItems = listOf("
end_str = "    Column("

start_idx = content.find(start_str)
end_idx = content.find(end_str, start_idx)

if start_idx != -1 and end_idx != -1:
    new_grid = """val gridItems = listOf(
        WorshipCategory("أذكار", androidx.compose.ui.graphics.Color(0xFF0A3622), icon = Icons.Filled.MenuBook),
        WorshipCategory("مناجاة", androidx.compose.ui.graphics.Color(0xFF0A3622), icon = Icons.Filled.Person),
        WorshipCategory("الزيارات", androidx.compose.ui.graphics.Color(0xFF0A3622), iconRes = com.example.R.drawable.ic_mosque),
        WorshipCategory("الادعية", androidx.compose.ui.graphics.Color(0xFF0A3622), icon = Icons.Filled.Favorite),
        WorshipCategory("الصحيفة السجادية", androidx.compose.ui.graphics.Color(0xFF0A3622), icon = Icons.Filled.Book),
        WorshipCategory("الاعمال", androidx.compose.ui.graphics.Color(0xFF0A3622), icon = Icons.Filled.CheckCircle),
        WorshipCategory("تعقيبات الصلاة", androidx.compose.ui.graphics.Color(0xFF0A3622), icon = Icons.Filled.DateRange),
        WorshipCategory("المسبحة", androidx.compose.ui.graphics.Color(0xFF0A3622), icon = Icons.Filled.DonutLarge),
        WorshipCategory("الادعية المختارة", androidx.compose.ui.graphics.Color(0xFF0A3622), icon = Icons.Filled.List),
        WorshipCategory("اتجاه القبلة", androidx.compose.ui.graphics.Color(0xFF0D2545), icon = Icons.Filled.Explore),
        WorshipCategory("عداد الركع", androidx.compose.ui.graphics.Color(0xFF402A12), icon = Icons.Filled.ConfirmationNumber),
        WorshipCategory("صلاة اهل البيت عليهم السلام", androidx.compose.ui.graphics.Color(0xFF2F2169), iconRes = com.example.R.drawable.ic_mosque)
    )

"""
    content = content[:start_idx] + new_grid + content[end_idx:]
    with open('app/src/main/java/com/example/ui/Screens.kt', 'w', encoding='utf-8') as f:
        f.write(content)
    print("Fixed grid!")
else:
    print("Could not find boundaries")
