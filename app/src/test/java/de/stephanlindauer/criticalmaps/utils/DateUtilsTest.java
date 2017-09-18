package de.stephanlindauer.criticalmaps.utils;

import org.junit.Test;

import java.util.Date;

import static com.google.common.truth.Truth.assertThat;

public class DateUtilsTest {
    @Test
    public void isNotLongerAgoThen_longerTimeReturnsFalse() throws Exception {
        long sixMinutesInMs = 1000 * 60 * 6;
        Date sixMinutesAgo = new Date(new Date().getTime() - sixMinutesInMs);
        assertThat(DateUtils.isNotLongerAgoThen(sixMinutesAgo, 5, 55)).isFalse();
    }

    @Test
    public void isNotLongerAgoThen_shorterTimeReturnsTrue() throws Exception {
        long sixMinutesInMs = 1000 * 60 * 6;
        Date sixMinutesAgo = new Date(new Date().getTime() - sixMinutesInMs);
        assertThat(DateUtils.isNotLongerAgoThen(sixMinutesAgo, 6, 55)).isTrue();
    }
}
