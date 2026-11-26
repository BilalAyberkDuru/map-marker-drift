package com.example.markerdrift

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.markerdrift.geo.MercatorMath
import com.example.markerdrift.marker.MarkerMode
import com.example.markerdrift.marker.PinGeometry
import com.example.markerdrift.ui.FrameInfo
import com.example.markerdrift.ui.MapScreen
import com.example.markerdrift.ui.SamplePlaces
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * Pin ucunu ekran görüntüsünün piksellerinden bulur ve gerçek koordinatın
 * harita projeksiyonuyla bulunan ekran konumuyla karşılaştırır.
 *
 * Gerçek harita çizimi gerektirir: Google Play hizmetli cihaz/emülatör ve geçerli API anahtarı.
 */
@RunWith(AndroidJUnit4::class)
class MarkerTipOnScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val place = SamplePlaces.home
    private val pin = PinGeometry()

    /** Kenar yumuşatma nedeniyle uç birkaç piksel yukarıda bulunabilir. */
    private val tolerancePx = 4

    @Test fun fixed_z5() = assertTip(MarkerMode.FIXED, 5f)
    @Test fun fixed_z12() = assertTip(MarkerMode.FIXED, 12f)
    @Test fun fixed_z18() = assertTip(MarkerMode.FIXED, 18f)

    @Test fun buggyPadding_z5() = assertTip(MarkerMode.BUGGY_PADDING, 5f)
    @Test fun buggyPadding_z12() = assertTip(MarkerMode.BUGGY_PADDING, 12f)
    @Test fun buggyPadding_z18() = assertTip(MarkerMode.BUGGY_PADDING, 18f)

    @Test fun buggyAnchor_z5() = assertTip(MarkerMode.BUGGY_ANCHOR, 5f)
    @Test fun buggyAnchor_z12() = assertTip(MarkerMode.BUGGY_ANCHOR, 12f)
    @Test fun buggyAnchor_z18() = assertTip(MarkerMode.BUGGY_ANCHOR, 18f)

    private fun assertTip(mode: MarkerMode, zoom: Float) {
        var frame: FrameInfo? = null
        compose.setContent {
            MapScreen(
                places = listOf(place),
                initialTarget = place.position,
                initialZoom = zoom,
                initialMode = mode,
                showOverlays = false,
                showReferenceMarkers = false,
                onFrame = { frame = it },
            )
        }
        compose.waitUntil(timeoutMillis = 20_000) { frame?.zoom == zoom }
        // İşaretçi bitmap'inin GL yüzeyine çizilmesi için kısa bekleme.
        Thread.sleep(2_000)
        compose.waitForIdle()

        val f = frame!!
        val real = f.realPositionsPx.getValue(place.name)
        val shot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val tip = findTip(shot, real.x, real.y, f.density)
        assertNotNull("Pin pikselleri bulunamadı (harita çizilmedi mi?)", tip)
        tip!!

        val expected = mode.spec(pin).tipOffsetDp
        val expDx = expected.x * f.density
        val expDy = expected.y * f.density
        val dx = tip.first - real.x
        val dy = tip.second - real.y
        val dyDp = dy / f.density
        val errM = MercatorMath.groundErrorMeters(abs(dyDp).toDouble(), place.position.latitude, zoom.toDouble())

        Log.i(
            "MarkerTipTest",
            "RESULT mode=$mode zoom=$zoom density=${f.density} real=(${real.x},${real.y}) " +
                "tip=(${tip.first},${tip.second}) dPx=($dx,$dy) dDp=${"%.1f".format(dyDp)} " +
                "expectedDyPx=${"%.1f".format(expDy)} groundErrM=${"%.1f".format(errM)}",
        )

        assertEquals("x sapması (px)", expDx.toDouble(), dx.toDouble(), tolerancePx.toDouble())
        assertEquals("y sapması (px)", expDy.toDouble(), dy.toDouble(), tolerancePx.toDouble())
    }

    /**
     * Gerçek konum etrafındaki pencerede pin rengine yakın en alttaki satırı bulur.
     * Dönüş: (ucun x'i = o satırdaki eşleşmelerin ortası, ucun y'si).
     */
    private fun findTip(bmp: Bitmap, cx: Int, cy: Int, density: Float): Pair<Int, Int>? {
        val halfW = (pin.widthDp * density).toInt()
        val reach = ((pin.bodyHeightDp + pin.paddingBottomDp + 10) * density).toInt()
        val x0 = (cx - halfW).coerceAtLeast(0)
        val x1 = (cx + halfW).coerceAtMost(bmp.width - 1)
        val y0 = (cy - reach).coerceAtLeast(0)
        val y1 = (cy + reach).coerceAtMost(bmp.height - 1)
        for (y in y1 downTo y0) {
            val xs = (x0..x1).filter { x -> isPinColor(bmp.getPixel(x, y)) }
            if (xs.isNotEmpty()) return Pair((xs.first() + xs.last()) / 2, y)
        }
        return null
    }

    private fun isPinColor(c: Int): Boolean {
        val t = place.color
        return abs(Color.red(c) - Color.red(t)) < 50 &&
            abs(Color.green(c) - Color.green(t)) < 50 &&
            abs(Color.blue(c) - Color.blue(t)) < 50
    }
}
