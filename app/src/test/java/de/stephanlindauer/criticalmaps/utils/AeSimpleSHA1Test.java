package de.stephanlindauer.criticalmaps.utils;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static com.google.common.truth.Truth.assertThat;

public class AeSimpleSHA1Test {
    @Test
    public void testThatSHA1IsCalculatedCorrectly() throws URISyntaxException, FileNotFoundException {
        final String sha1ForProbe = AeSimpleSHA1.SHA1("probe");
        assertThat(sha1ForProbe).isEqualTo("a949c530710f9fca76b45776267c68967fa891e7");
    }
}
