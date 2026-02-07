package de.stephanlindauer.criticalmaps.model.gpx

import org.maplibre.android.geometry.LatLng

data class GpxPoi(
    val name: String,
    val position: LatLng
)
