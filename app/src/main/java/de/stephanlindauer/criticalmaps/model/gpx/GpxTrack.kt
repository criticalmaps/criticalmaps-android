package de.stephanlindauer.criticalmaps.model.gpx

import org.maplibre.android.geometry.LatLng

data class GpxTrack(
    val name: String,
    val waypoints: List<LatLng>
)
