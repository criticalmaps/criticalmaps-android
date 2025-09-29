package de.stephanlindauer.criticalmaps.vo

object Endpoints {
    // IMAGE_POST doesn't currently work. Once fixed CDN or API_GATEWAY should be used to prefix the URI.
    const val IMAGE_POST = "https://api.criticalmaps.net/gallery/"

    const val LOCATION_GET = "https://api-cdn.criticalmaps.net/locations"
    const val LOCATION_PUT = "https://api-gw.criticalmaps.net/locations"

    const val CHAT_GET = "https://api-gw.criticalmaps.net/messages"
    const val CHAT_POST = "https://api-gw.criticalmaps.net/messages"
}