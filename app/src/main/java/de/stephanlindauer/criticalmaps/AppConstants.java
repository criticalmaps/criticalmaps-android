package de.stephanlindauer.criticalmaps;

public class AppConstants {

    public static final int HTTP_TIMEOUT = 15;

    public static final float LOCATION_REFRESH_DISTANCE = 20; //20 meters
    public static final long LOCATION_REFRESH_TIME = 12 * 1000; //12 seconds
    public static final int MAX_LOCATION_AGE = 30 * 1000; //30 seconds

    public static final String MAIN_POST = "http://api.criticalmaps.net/postv2";
    public static final String IMAGE_POST = "http://api.criticalmaps.net/gallery/post.php";
    public static final String GET_TWITTER = "http://api.criticalmaps.net/twitter/get.php";


}
