package de.stephanlindauer.criticalmass.vo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum City {

    BERLIN("Berlin", 123, 123),
    MUENCHEN("München", 123, 123),
    STUTTGART("Stuttgart", 123, 123),
    KOELN("Köln", 123, 123),
    HAMBURG("Hamburg", 123, 123),
    DRESDEN("Hamburg", 123, 123),
    FRANKFURTAM("Hamburg", 123, 123),
    FRANKFURTAO("Hamburg", 123, 123),
    LEIPZIG("Leipzig", 123, 123);

    private final double latitude;
    private final double longitude;
    private final String cityName;

    City(String cityName, double latitude, double longitude) {
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static CharSequence[] getAvailableCities() {
        List<City> cities = Arrays.asList(City.values());
        List<CharSequence> cityNames = new ArrayList<CharSequence>();

        for (City value : cities) {
            cityNames.add(value.cityName);
        }

        return cityNames.toArray(new CharSequence[cityNames.size()]);
    }

}
