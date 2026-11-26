package com.example.markerdrift.geo

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Web Mercator hesapları.
 *
 * Birim notu: Google Maps SDK for Android'de zoom 0'da dünya 256 **dp** genişliğindedir.
 * Bu yüzden buradaki "ekran birimi" fiziksel piksel değil dp'dir. Fiziksel px için
 * önce [pxToDp] ile ekran yoğunluğuna bölün.
 */
object MercatorMath {

    /** WGS84 ekvator yarıçapı; 156543.03 = 2π·R / 256 sabiti bundan gelir. */
    const val EARTH_RADIUS_M = 6_378_137.0

    /** Zoom 0'da, ekvatorda 1 dp'nin metre karşılığı. */
    const val METERS_PER_DP_Z0 = 156_543.03

    fun metersPerDp(latitudeDeg: Double, zoom: Double): Double =
        METERS_PER_DP_Z0 * cos(Math.toRadians(latitudeDeg)) / 2.0.pow(zoom)

    /** Ekranda [offsetDp] kadarlık kaymanın o zoom'da haritadaki metre karşılığı. */
    fun groundErrorMeters(offsetDp: Double, latitudeDeg: Double, zoom: Double): Double =
        offsetDp * metersPerDp(latitudeDeg, zoom)

    fun pxToDp(px: Double, density: Float): Double = px / density

    fun dpToPx(dp: Double, density: Float): Double = dp * density

    /** Haversine; Mercator sabitiyle tutarlı olsun diye aynı yarıçap kullanılır. */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dp = p2 - p1
        val dl = Math.toRadians(lon2 - lon1)
        val h = sin(dp / 2).pow(2) + cos(p1) * cos(p2) * sin(dl / 2).pow(2)
        return 2 * EARTH_RADIUS_M * asin(sqrt(h))
    }
}
