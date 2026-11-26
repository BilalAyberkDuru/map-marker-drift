package com.example.markerdrift.ui

import com.example.markerdrift.BuildConfig
import com.google.android.gms.maps.model.LatLng

/** Örnek konum. Gerçek adres değildir (README). */
data class Place(val name: String, val position: LatLng, val color: Int)

object SamplePlaces {
    val home = Place("Ev", LatLng(37.8450, 27.8400), 0xFF1E4FD8.toInt())
    val branch = Place("Şube", LatLng(37.8453, 27.8406), 0xFFE8590C.toInt())
    val all = listOf(home, branch)
}

/**
 * Git'e girmeyen local.places.properties dosyasındaki kişisel konumlar.
 * Dosya yoksa veya değerler geçersizse null döner; uygulama örnek konumları kullanır.
 */
object LocalPlaces {

    fun fromBuildConfig(): List<Place>? = parse(
        BuildConfig.PLACE_A_NAME, BuildConfig.PLACE_A_LAT, BuildConfig.PLACE_A_LNG,
        BuildConfig.PLACE_B_NAME, BuildConfig.PLACE_B_LAT, BuildConfig.PLACE_B_LNG,
    )

    fun parse(
        aName: String, aLat: String, aLng: String,
        bName: String, bLat: String, bLng: String,
    ): List<Place>? {
        val a = latLng(aLat, aLng) ?: return null
        val b = latLng(bLat, bLng) ?: return null
        return listOf(
            Place(aName.ifBlank { SamplePlaces.home.name }, a, SamplePlaces.home.color),
            Place(bName.ifBlank { SamplePlaces.branch.name }, b, SamplePlaces.branch.color),
        )
    }

    private fun latLng(lat: String, lng: String): LatLng? {
        val la = lat.trim().toDoubleOrNull() ?: return null
        val lo = lng.trim().toDoubleOrNull() ?: return null
        if (la !in -90.0..90.0 || lo !in -180.0..180.0) return null
        return LatLng(la, lo)
    }
}
