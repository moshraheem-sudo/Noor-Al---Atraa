package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PredefinedCities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("مواقيت الصلاة", appName)
  }

  @Test
  fun `predefined cities contain Karbala and Najaf`() {
    val karbala = PredefinedCities.list.find { it.id == "karbala" }
    val najaf = PredefinedCities.list.find { it.id == "najaf" }
    assertTrue(karbala != null)
    assertTrue(najaf != null)
    assertEquals(32.6143, karbala!!.latitude, 0.001)
    assertEquals(44.0228, karbala.longitude, 0.001)
  }

  @Test
  fun `offline astronomical calculation produces valid prayer times`() {
    val karbala = PredefinedCities.defaultCity
    val offlineData = com.example.utils.PrayerCalculator.calculateOfflinePrayerData(karbala)
    assertTrue(offlineData.fajir.isNotBlank())
    assertTrue(offlineData.sunrise.isNotBlank())
    assertTrue(offlineData.doher.isNotBlank())
    assertTrue(offlineData.sunset.isNotBlank())
    assertTrue(offlineData.maghrib.isNotBlank())
    assertTrue(offlineData.midnight.isNotBlank())
  }

  @Test
  fun `nearest city resolver returns valid city for coordinates`() {
    val nearest = com.example.utils.LocationHelper.findNearestPredefinedCity(32.60, 44.02)
    assertEquals("karbala", nearest.id)
  }
}
