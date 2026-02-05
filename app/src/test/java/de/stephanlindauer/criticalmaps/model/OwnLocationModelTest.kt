package de.stephanlindauer.criticalmaps.model

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.maplibre.android.geometry.LatLng

class OwnLocationModelTest {

    @Test
    fun `hasPreciseLocation - imprecise location is reported as imprecise`() {
        val tested = OwnLocationModel()

        tested.setLocation(LatLng(0.0, 0.0), 50.0f)

        assertThat(tested.hasPreciseLocation()).isFalse()
    }

    @Test
    fun `hasPreciseLocation - precise location is reported as precise`() {
        val tested = OwnLocationModel()

        tested.setLocation(LatLng(0.0, 0.0), 49.9f)

        assertThat(tested.hasPreciseLocation()).isTrue()
    }

    @Test
    fun `getLocationJson - throws when no location`() {
        val tested = OwnLocationModel()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            tested.getLocationJson()
        }
        assertThat(exception.message).isEqualTo("Location must be set before calling getLocationJson()")
    }

    @Test
    fun `getLocationJson - returns correct json`() {
        val tested = OwnLocationModel()

        val expected = """{"latitude":"40741895","longitude":"-73989308"}"""
        tested.setLocation(LatLng(40.741895, -73.989308), 1.1f)
        assertThat(tested.getLocationJson().toString()).isEqualTo(expected)
    }
}
