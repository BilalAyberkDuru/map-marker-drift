package com.example.markerdrift.marker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.createBitmap

/**
 * Özgün pin görselleri. Kod ile çizilir, dış görsel dosyası kullanılmaz.
 */
object PinIcon {

    /** Damla biçimli pin; uç, gövdenin alt-ortasında. Altında [MarkerSpec.paddingBottomDp] kadar şeffaf boşluk. */
    fun pin(spec: MarkerSpec, color: Int, density: Float): Bitmap {
        val w = (spec.widthDp * density).toInt()
        val h = (spec.heightDp * density).toInt()
        val bodyH = h - (spec.paddingBottomDp * density).toInt()
        val bmp = createBitmap(w, h)
        val c = Canvas(bmp)

        val r = w / 2f
        val path = Path().apply {
            addArc(RectF(0f, 0f, w.toFloat(), w.toFloat()), 150f, 240f)
            lineTo(r, bodyH.toFloat())
            close()
        }
        // Dış çizgi yok: kontur sivri uçta taşar ve ucun ekranda ölçülmesini bozar.
        c.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
        c.drawCircle(r, r, r * 0.38f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = 0xFFFFFFFF.toInt() })
        return bmp
    }

    /** Gerçek koordinatı gösteren küçük artı işaretli halka; merkezli, çapa 0.5/0.5. */
    fun referenceDot(sizeDp: Float, color: Int, density: Float): Bitmap {
        val s = (sizeDp * density).toInt()
        val bmp = createBitmap(s, s)
        val c = Canvas(bmp)
        val m = s / 2f
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * density
        }
        c.drawCircle(m, m, m - stroke.strokeWidth, stroke)
        c.drawLine(m, 0f, m, s.toFloat(), stroke)
        c.drawLine(0f, m, s.toFloat(), m, stroke)
        return bmp
    }
}
