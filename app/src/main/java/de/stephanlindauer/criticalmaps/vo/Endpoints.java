package de.stephanlindauer.criticalmaps.vo;

public class Endpoints {
//    IMAGE_POST doesn't currently work. Once fixed CDN or API_GATEWAY should be used to prefix the URI.
    public static final String IMAGE_POST = "https://api.criticalmaps.net/gallery/";

    public static final String LOCATION_GET = "https://api-cdn.criticalmaps.net/location";
    public static final String LOCATION_PUT = "https://api.criticalmaps.net/location";
}
