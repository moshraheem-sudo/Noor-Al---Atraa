import re

with open('app/src/main/java/com/example/ui/Screens.kt', 'r', encoding='utf-8') as f:
    content = f.read()

start_str = "data class WorshipCategory("
end_str = "val gridItems = listOf("

# update WorshipCategory
content = re.sub(r'data class WorshipCategory\(.*?\)', """data class WorshipCategory(
    val title: String,
    val color: androidx.compose.ui.graphics.Color,
    val iconRes: Int,
    val iconTint: androidx.compose.ui.graphics.Color? = null
)""", content, flags=re.DOTALL)

start_idx = content.find("val gridItems = listOf(")
end_idx = content.find("    Column(", start_idx)

new_grid = """val gridItems = listOf(
        WorshipCategory("أذكار", androidx.compose.ui.graphics.Color(0xFF0A3622), com.example.R.drawable.ic_worship_azkar),
        WorshipCategory("مناجاة", androidx.compose.ui.graphics.Color(0xFF0A3622), com.example.R.drawable.ic_worship_munajat),
        WorshipCategory("الزيارات", androidx.compose.ui.graphics.Color(0xFF0A3622), com.example.R.drawable.ic_worship_mosque),
        WorshipCategory("الادعية", androidx.compose.ui.graphics.Color(0xFF0A3622), com.example.R.drawable.ic_worship_duas),
        WorshipCategory("الصحيفة السجادية", androidx.compose.ui.graphics.Color(0xFF0A3622), com.example.R.drawable.ic_worship_sahifa),
        WorshipCategory("الاعمال", androidx.compose.ui.graphics.Color(0xFF0A3622), com.example.R.drawable.ic_worship_aamal),
        WorshipCategory("تعقيبات الصلاة", androidx.compose.ui.graphics.Color(0xFF0A3622), com.example.R.drawable.ic_worship_taqibat),
        WorshipCategory("المسبحة", androidx.compose.ui.graphics.Color(0xFF0A3622), com.example.R.drawable.ic_worship_masbaha),
        WorshipCategory("الادعية المختارة", androidx.compose.ui.graphics.Color(0xFF0D3F33), com.example.R.drawable.ic_worship_selected),
        WorshipCategory("اتجاه القبلة", androidx.compose.ui.graphics.Color(0xFF0D2545), com.example.R.drawable.ic_worship_qibla),
        WorshipCategory("عداد الركع", androidx.compose.ui.graphics.Color(0xFF402A12), com.example.R.drawable.ic_worship_counter),
        WorshipCategory("صلاة اهل البيت\\nعليهم السلام", androidx.compose.ui.graphics.Color(0xFF2F2169), com.example.R.drawable.ic_worship_mosque, androidx.compose.ui.graphics.Color(0xFFE0E0E0))
    )

"""

content = content[:start_idx] + new_grid + content[end_idx:]

with open('app/src/main/java/com/example/ui/Screens.kt', 'w', encoding='utf-8') as f:
    f.write(content)

