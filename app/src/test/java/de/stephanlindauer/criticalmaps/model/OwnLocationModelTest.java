package de.stephanlindauer.criticalmaps.model;

import org.json.JSONException;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.util.Date;

import static com.google.common.truth.Truth.assertThat;

public class OwnLocationModelTest {
    @Test
    public void hasPreciseLocation_impreciseLocationIsReportedAsImprecise() throws Exception {
        OwnLocationModel tested = new OwnLocationModel();

        tested.setLocation(new GeoPoint(0d, 0d), 50.0f);

        assertThat(tested.hasPreciseLocation()).isFalse();
    }

    @Test
    public void hasPreciseLocation_preciseLocationIsReportedAsPrecise() {
        OwnLocationModel tested = new OwnLocationModel();

        tested.setLocation(new GeoPoint(0d, 0d), 49.9f);

        assertThat(tested.hasPreciseLocation()).isTrue();
    }

    @Test(expected = NullPointerException.class)
    public void getLocationJson_throwsWhenNoLocation() {
        // TODO use JUnit's assertThrows() instead when 4.13 is out
        OwnLocationModel tested = new OwnLocationModel();
        tested.getLocationJson();
    }

    @Test
    public void getLocationJson_returnsCorrectJson() throws JSONException{
        OwnLocationModel tested = new OwnLocationModel();

        String expected = "{\"latitude\":\"40741895\",\"longitude\":\"-73989308\"}";
        tested.setLocation(new GeoPoint(40.741895d, -73.989308d), 1.1f);
        assertThat(tested.getLocationJson().toString()).isEqualTo(expected);
    }
}
