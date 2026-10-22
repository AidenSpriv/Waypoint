package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PilotDataSeeder
import com.example.data.model.LatLng
import com.example.data.model.PilotArea
import com.example.data.remote.GeoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Waypoint", appName)
    }

    @Test
    fun `geoUtils calculates realistic walking distance`() {
        val msheirebMetro = LatLng(25.2868, 51.5315)
        val souqWaqifMetro = LatLng(25.2879, 51.5358)
        val distance = GeoUtils.distanceMeters(msheirebMetro, souqWaqifMetro)

        // The distance between Msheireb and Souq Waqif is approx 400-500 meters
        assertTrue("Distance should be between 300m and 600m", distance in 300.0..600.0)
    }

    @Test
    fun `pilot data seeder has valid Doha facilities and barriers`() {
        val facilities = PilotDataSeeder.INITIAL_FACILITIES
        val barriers = PilotDataSeeder.INITIAL_BARRIERS

        assertTrue("Should have seeded facilities", facilities.isNotEmpty())
        assertTrue("Should have seeded barriers", barriers.isNotEmpty())

        val defaultPilot = PilotArea.DEFAULT
        assertNotNull(defaultPilot)
        assertEquals("doha_msheireb", defaultPilot.id)
    }

    @Test
    fun `leaflet_map html asset can be read`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val content = context.assets.open("leaflet_map.html").bufferedReader().use { it.readText() }
        assertTrue("Asset should not be empty", content.isNotEmpty())
        assertTrue("Asset should contain Leaflet", content.contains("L.map"))
    }
}
