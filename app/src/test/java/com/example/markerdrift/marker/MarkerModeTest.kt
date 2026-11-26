package com.example.markerdrift.marker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkerModeTest {

    private val pin = PinGeometry(widthDp = 36f, bodyHeightDp = 48f, paddingBottomDp = 40f)

    @Test
    fun `duzeltilmis modda uc tam referans noktasinda`() {
        val spec = MarkerMode.FIXED.spec(pin)
        assertEquals(0.5f, spec.anchorU)
        assertEquals(1.0f, spec.anchorV)
        assertEquals(0f, spec.tipOffsetDp.x, 1e-6f)
        assertEquals(0f, spec.tipOffsetDp.y, 1e-6f)
    }

    @Test
    fun `duzeltilmis mod seffaf bosluk kullanmaz`() {
        assertEquals(48f, MarkerMode.FIXED.spec(pin).heightDp)
    }

    @Test
    fun `yanlis capa ucu ikon yuksekliginin yarisi kadar asagi kaydirir`() {
        val spec = MarkerMode.BUGGY_ANCHOR.spec(pin)
        assertEquals(0.5f, spec.anchorV)
        assertEquals(0f, spec.tipOffsetDp.x, 1e-6f)
        assertEquals(24f, spec.tipOffsetDp.y, 1e-4f) // +y = ekranda aşağı
    }

    @Test
    fun `seffaf bosluk ucu bosluk kadar yukari kaydirir`() {
        val spec = MarkerMode.BUGGY_PADDING.spec(pin)
        assertEquals(1.0f, spec.anchorV)
        assertEquals(88f, spec.heightDp)
        assertEquals(-40f, spec.tipOffsetDp.y, 1e-4f)
    }

    @Test
    fun `sadece FIXED hatasiz`() {
        assertTrue(MarkerMode.entries.filter { !it.isBuggy } == listOf(MarkerMode.FIXED))
    }
}
