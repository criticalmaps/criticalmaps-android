package de.stephanlindauer.criticalmaps.vo;

public class Endpoints {
    // IMAGE_POST doesn't currently work. Once fixed CDN or API_GATEWAY should be used to prefix the URI.
    public static final String IMAGE_POST = "https://api.criticalmaps.net/gallery/";

    public static final String LOCATION_GET = "https://api-cdn.criticalmaps.net/locations";
    public static final String LOCATION_PUT = "https://api-gw.criticalmaps.net/locations";

    public static final String CHAT_GET = "https://api-gw.criticalmaps.net/messages";
    public static final String CHAT_POST = "https://api-gw.criticalmaps.net/messages";
}
