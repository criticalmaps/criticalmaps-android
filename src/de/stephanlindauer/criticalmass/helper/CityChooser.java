package de.stephanlindauer.criticalmass.helper;

public class CityChooser {
    private static CityChooser instance;

    public static CityChooser getInstance() {
        if (CityChooser.instance == null) {
            CityChooser.instance = new CityChooser();
        }
        return CityChooser.instance;
    }
}
