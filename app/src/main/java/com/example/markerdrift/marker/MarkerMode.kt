package com.example.markerdrift.marker

/** Ekran ofseti, dp. +x sağa, +y aşağı. */
data class DpOffset(val x: Float, val y: Float)

/**
 * Pin görselinin ölçüleri. Ucu gövdenin alt-ortasındadır.
 * [paddingBottomDp] yalnızca şeffaf boşluklu hatalı varyantta görsele eklenir.
 */
data class PinGeometry(
    val widthDp: Float = 36f,
    val bodyHeightDp: Float = 48f,
    val paddingBottomDp: Float = 40f,
)

/** Bir işaretçinin görsel boyutu, çapası ve görseldeki uç konumu (0..1). */
data class MarkerSpec(
    val widthDp: Float,
    val heightDp: Float,
    val paddingBottomDp: Float,
    val anchorU: Float,
    val anchorV: Float,
    val tipU: Float,
    val tipV: Float,
) {
    /**
     * Çapa noktası gerçek konuma oturtulur; uç ondan bu kadar uzakta çizilir.
     * Değer yalnızca görsel geometrisine bağlıdır, zoom'a bağlı değildir.
     */
    val tipOffsetDp: DpOffset
        get() = DpOffset((tipU - anchorU) * widthDp, (tipV - anchorV) * heightDp)
}

enum class MarkerMode(val isBuggy: Boolean, val label: String) {
    /** Boşluksuz pin, çapa alt-orta: uç = gerçek konum. */
    FIXED(false, "Düzeltilmiş"),

    /** Boşluksuz pin ama çapa merkezde: uç, yüksekliğin yarısı kadar aşağıda. */
    BUGGY_ANCHOR(true, "Hatalı: çapa"),

    /** Altında şeffaf boşluk olan pin, varsayılan alt-orta çapa: uç, boşluk kadar yukarıda. */
    BUGGY_PADDING(true, "Hatalı: boşluk");

    fun spec(pin: PinGeometry): MarkerSpec = when (this) {
        FIXED -> MarkerSpec(pin.widthDp, pin.bodyHeightDp, 0f, 0.5f, 1.0f, 0.5f, 1.0f)
        BUGGY_ANCHOR -> MarkerSpec(pin.widthDp, pin.bodyHeightDp, 0f, 0.5f, 0.5f, 0.5f, 1.0f)
        BUGGY_PADDING -> {
            val h = pin.bodyHeightDp + pin.paddingBottomDp
            MarkerSpec(pin.widthDp, h, pin.paddingBottomDp, 0.5f, 1.0f, 0.5f, pin.bodyHeightDp / h)
        }
    }
}
