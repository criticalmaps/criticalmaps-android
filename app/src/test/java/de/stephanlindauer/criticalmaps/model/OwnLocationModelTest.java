package de.stephanlindauer.criticalmaps.model;

import org.junit.Test;
import org.maplibre.android.geometry.LatLng;


import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

public class OwnLocationModelTest {
    @Test
    public void hasPreciseLocation_impreciseLocationIsReportedAsImprecise() {
        OwnLocationModel tested = new OwnLocationModel();

        tested.setLocation(new LatLng(0d, 0d), 50.0f);

        assertThat(tested.hasPreciseLocation()).isFalse();
    }

    @Test
    public void hasPreciseLocation_preciseLocationIsReportedAsPrecise() {
        OwnLocationModel tested = new OwnLocationModel();

        tested.setLocation(new LatLng(0d, 0d), 49.9f);

        assertThat(tested.hasPreciseLocation()).isTrue();
    }

    @Test
    public void getLocationJson_throwsWhenNoLocation() {
        OwnLocationModel tested = new OwnLocationModel();

        assertThrows(NullPointerException.class, tested::getLocationJson);
    }

    @Test
    public void getLocationJson_returnsCorrectJson() {
        OwnLocationModel tested = new OwnLocationModel();

        String expected = "{\"latitude\":\"40741895\",\"longitude\":\"-73989308\"}";
        tested.setLocation(new LatLng(40.741895d, -73.989308d), 1.1f);
        assertThat(tested.getLocationJson().toString()).isEqualTo(expected);
    }
}
