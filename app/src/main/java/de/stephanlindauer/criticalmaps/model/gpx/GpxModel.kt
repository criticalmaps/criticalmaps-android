package de.stephanlindauer.criticalmaps.model.gpx

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpxModel @Inject constructor() {

    var uri: String? = null
    var tracks: MutableList<GpxTrack> = mutableListOf()
    var poiList: MutableList<GpxPoi> = mutableListOf()

    fun clear() {
        tracks.clear()
        poiList.clear()
        uri = null
    }
}
