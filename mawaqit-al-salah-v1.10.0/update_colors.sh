#!/bin/bash
FILE="/app/applet/app/src/main/java/com/example/ui/screens/HomeScreen.kt"

# Remove the specific color imports that are being replaced
sed -i '/import com.example.ui.theme.HeroGradientEnd/d' $FILE
sed -i '/import com.example.ui.theme.HeroGradientStart/d' $FILE
sed -i '/import com.example.ui.theme.IslamicGold/d' $FILE
sed -i '/import com.example.ui.theme.SophisticatedBg/d' $FILE
sed -i '/import com.example.ui.theme.SophisticatedBorder/d' $FILE
sed -i '/import com.example.ui.theme.SophisticatedBorderSubtle/d' $FILE
sed -i '/import com.example.ui.theme.SophisticatedSurface/d' $FILE
sed -i '/import com.example.ui.theme.TealAccent/d' $FILE
sed -i '/import com.example.ui.theme.TealAccentLight/d' $FILE
sed -i '/import com.example.ui.theme.TealGlow10/d' $FILE
sed -i '/import com.example.ui.theme.TealGlow20/d' $FILE
sed -i '/import com.example.ui.theme.TealGlow40/d' $FILE
sed -i '/import com.example.ui.theme.TealGradientEnd/d' $FILE
sed -i '/import com.example.ui.theme.TealGradientStart/d' $FILE
sed -i '/import com.example.ui.theme.TextMain/d' $FILE
sed -i '/import com.example.ui.theme.TextMuted/d' $FILE
sed -i '/import com.example.ui.theme.TextSubtle/d' $FILE
sed -i '/import com.example.ui.theme.TextWhite/d' $FILE

# Replace usages
sed -i 's/HeroGradientStart/AppColors.current.heroGradientStart/g' $FILE
sed -i 's/HeroGradientEnd/AppColors.current.heroGradientEnd/g' $FILE
sed -i 's/TealAccentLight/AppColors.current.tealAccentLight/g' $FILE
sed -i 's/TealAccent/AppColors.current.tealAccent/g' $FILE
sed -i 's/TealGlow10/AppColors.current.tealGlow10/g' $FILE
sed -i 's/TealGlow20/AppColors.current.tealGlow20/g' $FILE
sed -i 's/TealGlow40/AppColors.current.tealGlow40/g' $FILE
sed -i 's/TextMain/AppColors.current.textMain/g' $FILE
sed -i 's/TextWhite/AppColors.current.textTitle/g' $FILE
sed -i 's/TextMuted/AppColors.current.textMuted/g' $FILE
sed -i 's/TextSubtle/AppColors.current.textSubtle/g' $FILE
sed -i 's/SophisticatedSurface/AppColors.current.surface/g' $FILE
sed -i 's/SophisticatedBorderSubtle/AppColors.current.borderSubtle/g' $FILE
sed -i 's/SophisticatedBorder/AppColors.current.border/g' $FILE
sed -i 's/SophisticatedBg/AppColors.current.bg/g' $FILE

# Add import for AppColors if missing
grep -q "import com.example.ui.theme.AppColors" $FILE || sed -i '/import com.example.ui.theme/a import com.example.ui.theme.AppColors' $FILE

