package com.example.markerdrift.geo

import org.junit.Assert.assertEquals
import org.junit.Test

class MercatorMathTest {

    private val lat = 37.845

    @Test
    fun `ekvatorda zoom 0'da birim basina metre sabite esit`() {
        assertEquals(156543.03, MercatorMath.metersPerDp(latitudeDeg = 0.0, zoom = 0.0), 0.01)
    }

    @Test
    fun `her zoom artisi metre-dp degerini yariya indirir`() {
        val z10 = MercatorMath.metersPerDp(lat, 10.0)
        val z11 = MercatorMath.metersPerDp(lat, 11.0)
        assertEquals(z10 / 2, z11, 1e-9)
    }

    @Test
    fun `kesirli zoom desteklenir`() {
        val z12 = MercatorMath.metersPerDp(lat, 12.0)
        val z12half = MercatorMath.metersPerDp(lat, 12.5)
        assertEquals(z12 / Math.sqrt(2.0), z12half, 1e-9)
    }

    // README'deki örnek: 37.845° enleminde 98 birimlik kayma
    @Test
    fun `README ornegi zoom 18 yaklasik 46 m`() {
        assertEquals(46.2, MercatorMath.groundErrorMeters(98.0, lat, 18.0), 0.1)
    }

    @Test
    fun `README ornegi zoom 12 yaklasik 3 km`() {
        assertEquals(2957.0, MercatorMath.groundErrorMeters(98.0, lat, 12.0), 1.0)
    }

    @Test
    fun `README ornegi zoom 5 yaklasik 380 km`() {
        assertEquals(378_500.0, MercatorMath.groundErrorMeters(98.0, lat, 5.0), 500.0)
    }

    @Test
    fun `sifir kayma sifir hata`() {
        assertEquals(0.0, MercatorMath.groundErrorMeters(0.0, lat, 5.0), 0.0)
    }

    @Test
    fun `px dp donusumu yogunluga boler`() {
        assertEquals(40.0, MercatorMath.pxToDp(px = 105.0, density = 2.625f), 1e-9)
        assertEquals(105.0, MercatorMath.dpToPx(dp = 40.0, density = 2.625f), 1e-9)
    }

    @Test
    fun `ayni nokta arasi mesafe sifir`() {
        assertEquals(0.0, MercatorMath.distanceMeters(lat, 27.84, lat, 27.84), 1e-9)
    }

    @Test
    fun `ayni enlemde dogu-bati mesafesi formulle uyumlu`() {
        // 1 dp'nin zoom 18'deki karşılığı kadar doğuya kayan nokta
        val m = MercatorMath.metersPerDp(lat, 18.0)
        val dLonDeg = 360.0 / (256.0 * Math.pow(2.0, 18.0))
        val d = MercatorMath.distanceMeters(lat, 27.84, lat, 27.84 + dLonDeg)
        assertEquals(m, d, m * 1e-6)
    }

    @Test
    fun `Ev ile Sube arasi yaklasik 63 m`() {
        // Örnek koordinatlar (README): Ev 37.8450,27.8400 — Şube 37.8453,27.8406
        val d = MercatorMath.distanceMeters(37.8450, 27.8400, 37.8453, 27.8406)
        assertEquals(62.0, d, 2.0)
    }
}
