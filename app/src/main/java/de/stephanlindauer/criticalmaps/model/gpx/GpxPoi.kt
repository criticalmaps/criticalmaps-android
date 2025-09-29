package de.stephanlindauer.criticalmaps.model.gpx

import org.osmdroid.util.GeoPoint

data class GpxPoi(
    val name: String,
    val position: GeoPoint
)