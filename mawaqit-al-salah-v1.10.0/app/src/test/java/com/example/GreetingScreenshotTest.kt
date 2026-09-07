package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.AppLanguage
import com.example.data.model.PrayerType
import com.example.ui.screens.PrayerCardInfo
import com.example.ui.screens.SophisticatedPrayerItemCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        SophisticatedPrayerItemCard(
          currentLanguage = AppLanguage.ARABIC,
          prayerInfo = PrayerCardInfo(
            type = PrayerType.DHUHR,
            rawTime = "11:53",
            emoji = "☀️",
            icon = Icons.Default.WbSunny,
            description = "أذان الظهر"
          ),
          isNext = true,
          isNotificationEnabled = true,
          onToggleNotification = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
