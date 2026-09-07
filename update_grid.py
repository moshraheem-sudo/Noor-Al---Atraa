import re

with open('app/src/main/java/com/example/ui/Screens.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update WorshipCategory
new_worship_category = """data class WorshipCategory(
    val title: String,
    val color: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val iconRes: Int? = null
)"""

content = re.sub(r'data class WorshipCategory\([^)]+\)', new_worship_category, content)

# 2. Update gridItems
new_grid_items = """val gridItems = listOf(
        WorshipCategory("أذكار", androidx.compose.ui.graphics.Color(0xFF0A3622), androidx.compose.material.icons.Icons.AutoMirrored.Filled.MenuBook),
        WorshipCategory("مناجاة", androidx.compose.ui.graphics.Color(0xFF0A3622), androidx.compose.material.icons.Icons.Filled.Person),
        WorshipCategory("الزيارات", androidx.compose.ui.graphics.Color(0xFF0A3622), iconRes = com.example.R.drawable.ic_mosque),
        WorshipCategory("الادعية", androidx.compose.ui.graphics.Color(0xFF0A3622), androidx.compose.material.icons.Icons.Filled.Favorite),
        WorshipCategory("الصحيفة السجادية", androidx.compose.ui.graphics.Color(0xFF0A3622), androidx.compose.material.icons.Icons.Filled.Book),
        WorshipCategory("الاعمال", androidx.compose.ui.graphics.Color(0xFF0A3622), androidx.compose.material.icons.Icons.Filled.Checklist),
        WorshipCategory("تعقيبات الصلاة", androidx.compose.ui.graphics.Color(0xFF0A3622), androidx.compose.material.icons.Icons.Filled.Alarm),
        WorshipCategory("المسبحة", androidx.compose.ui.graphics.Color(0xFF0A3622), androidx.compose.material.icons.Icons.Filled.DonutLarge),
        WorshipCategory("الادعية المختارة", androidx.compose.ui.graphics.Color(0xFF0A3622), androidx.compose.material.icons.Icons.Filled.LibraryBooks),
        WorshipCategory("اتجاه القبلة", androidx.compose.ui.graphics.Color(0xFF0D2545), androidx.compose.material.icons.Icons.Filled.Explore),
        WorshipCategory("عداد الركع", androidx.compose.ui.graphics.Color(0xFF402A12), androidx.compose.material.icons.Icons.Filled.Numbers),
        WorshipCategory("صلاة اهل البيت عليهم السلام", androidx.compose.ui.graphics.Color(0xFF2F2169), iconRes = com.example.R.drawable.ic_mosque)
    )"""

content = re.sub(r'val gridItems = listOf\(.*?\)', new_grid_items, content, flags=re.DOTALL)

# 3. Update the icon rendering and text
old_box = """Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(item.color, shape = CircleShape),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )"""

new_box = """Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(item.color, shape = CircleShape)
                                    .border(2.dp, androidx.compose.ui.graphics.Color(0xFFE2C275), CircleShape),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                if (item.icon != null) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = androidx.compose.ui.graphics.Color(0xFFE2C275),
                                        modifier = Modifier.size(36.dp)
                                    )
                                } else if (item.iconRes != null) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = item.iconRes),
                                        contentDescription = item.title,
                                        tint = androidx.compose.ui.graphics.Color(0xFFE2C275),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )"""

content = content.replace(old_box, new_box)

with open('app/src/main/java/com/example/ui/Screens.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated grid")
