package com.example.markerdrift.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalPlacesTest {

    private fun parse(
        aName: String = "A", aLat: String = "37.1", aLng: String = "27.1",
        bName: String = "B", bLat: String = "37.2", bLng: String = "27.2",
    ) = LocalPlaces.parse(aName, aLat, aLng, bName, bLat, bLng)

    @Test
    fun `gecerli degerler iki yer dondurur`() {
        val places = parse()!!
        assertEquals(listOf("A", "B"), places.map { it.name })
        assertEquals(37.1, places[0].position.latitude, 1e-12)
        assertEquals(27.2, places[1].position.longitude, 1e-12)
    }

    @Test
    fun `renkler ornek yerlerle ayni`() {
        val places = parse()!!
        assertEquals(SamplePlaces.home.color, places[0].color)
        assertEquals(SamplePlaces.branch.color, places[1].color)
    }

    @Test
    fun `dosya yoksa (bos alanlar) null`() {
        assertNull(LocalPlaces.parse("", "", "", "", "", ""))
    }

    @Test
    fun `sayi olmayan koordinat null`() {
        assertNull(parse(aLat = "abc"))
    }

    @Test
    fun `aralik disi koordinat null`() {
        assertNull(parse(aLat = "91"))
        assertNull(parse(bLng = "-181"))
    }

    @Test
    fun `ad bossa varsayilan ad kullanilir`() {
        val places = parse(aName = "", bName = "")!!
        assertEquals(listOf(SamplePlaces.home.name, SamplePlaces.branch.name), places.map { it.name })
    }
}
