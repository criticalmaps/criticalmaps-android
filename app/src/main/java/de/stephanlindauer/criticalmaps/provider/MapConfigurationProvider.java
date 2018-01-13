package de.stephanlindauer.criticalmaps.provider;

import org.osmdroid.config.DefaultConfigurationProvider;

// TODO this is only to prevent the DefaultConfigurationProvider constructor from creating
//      unnecessary directories on sdcard; can be removed with osmdroid 6.0 as that behaviour
//      will be changed
public class MapConfigurationProvider extends DefaultConfigurationProvider {

    public MapConfigurationProvider() {
    }
}
