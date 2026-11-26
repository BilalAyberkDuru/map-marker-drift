package com.example.markerdrift.ui

import android.graphics.Point
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.markerdrift.R
import com.example.markerdrift.geo.MercatorMath
import com.example.markerdrift.marker.MarkerMode
import com.example.markerdrift.marker.PinGeometry
import com.example.markerdrift.marker.PinIcon
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.util.Locale
import kotlin.math.hypot

private val CENTER = LatLng(37.84515, 27.8403)
private const val REFERENCE_COLOR = 0xFF00A86B.toInt()

/** Kamera durduğunda ölçüm için yayınlanan bilgi. Ekran konumları pencere pikselidir. */
data class FrameInfo(
    val zoom: Float,
    val density: Float,
    val mode: MarkerMode,
    val realPositionsPx: Map<String, Point>,
)

@Composable
fun MapScreen(
    places: List<Place> = SamplePlaces.all,
    initialTarget: LatLng = CENTER,
    initialZoom: Float = 18f,
    initialMode: MarkerMode = MarkerMode.BUGGY_PADDING,
    showOverlays: Boolean = true,
    showReferenceMarkers: Boolean = true,
    fitToPlaces: Boolean = false,
    onFrame: ((FrameInfo) -> Unit)? = null,
) {
    val density = LocalDensity.current.density
    val pin = remember { PinGeometry() }
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    var lastBuggy by rememberSaveable { mutableStateOf(MarkerMode.BUGGY_PADDING) }
    var mapOffset by remember { mutableStateOf(Offset.Zero) }
    var mapLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // İşletme/POI etiketleri gizli: ekran görüntülerinde gerçek işletme veya kişi adı çıkmasın.
    val mapProperties = remember {
        MapProperties(mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialTarget, initialZoom)
    }
    val spec = mode.spec(pin)

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .testTag("map")
                .onGloballyPositioned { mapOffset = it.positionInWindow() },
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
            onMapLoaded = { mapLoaded = true },
        ) {
            places.forEach { place ->
                val icon = remember(place, mode, density) {
                    BitmapDescriptorFactory.fromBitmap(PinIcon.pin(spec, place.color, density))
                }
                val refIcon = remember(density) {
                    BitmapDescriptorFactory.fromBitmap(PinIcon.referenceDot(14f, REFERENCE_COLOR, density))
                }
                // Referans: gerçek koordinatta 3 m'lik daire (yakın zoom'da görünür) ...
                Circle(
                    center = place.position,
                    radius = 3.0,
                    strokeColor = Color(REFERENCE_COLOR),
                    strokeWidth = 2f * density,
                    zIndex = 0f,
                )
                // ... ve her zoom'da aynı boyutta kalan merkezli nokta (çapa 0.5/0.5).
                if (showReferenceMarkers) Marker(
                    state = rememberUpdatedMarkerState(place.position),
                    icon = refIcon,
                    anchor = Offset(0.5f, 0.5f),
                    zIndex = 2f,
                    title = "${place.name} (gerçek konum)",
                )
                Marker(
                    state = rememberUpdatedMarkerState(place.position),
                    icon = icon,
                    anchor = Offset(spec.anchorU, spec.anchorV),
                    zIndex = 1f,
                    title = place.name,
                )
            }
        }

        // Açılışta tüm yerleri ekrana sığdır (yerler birbirinden uzaksa).
        LaunchedEffect(mapLoaded) {
            if (!mapLoaded || !fitToPlaces || places.size < 2) return@LaunchedEffect
            val bounds = LatLngBounds.builder().apply { places.forEach { include(it.position) } }.build()
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, (120 * density).toInt()))
        }

        // Kamera durduğunda gerçek konumların ekran pikselini yayınla (test ve bilgi paneli için).
        LaunchedEffect(cameraPositionState.isMoving, mode, mapOffset, mapLoaded) {
            if (!mapLoaded || cameraPositionState.isMoving) return@LaunchedEffect
            val projection = cameraPositionState.projection ?: return@LaunchedEffect
            val positions = places.associate { p ->
                val pt = projection.toScreenLocation(p.position)
                p.name to Point(pt.x + mapOffset.x.toInt(), pt.y + mapOffset.y.toInt())
            }
            onFrame?.invoke(FrameInfo(cameraPositionState.position.zoom, density, mode, positions))
        }

        if (showOverlays) {
            Column(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                ModeCard(
                    mode = mode,
                    onBuggyChange = { buggy -> mode = if (buggy) lastBuggy else MarkerMode.FIXED },
                    onBuggyKind = { lastBuggy = it; mode = it },
                )
                InfoCard(
                    zoom = cameraPositionState.position.zoom,
                    latitude = cameraPositionState.position.target.latitude,
                    density = density,
                    tipOffsetDp = spec.tipOffsetDp.y,
                    tipOffsetDx = spec.tipOffsetDp.x,
                    // İlk yeri merkeze alır: yerler birbirinden uzaksa z18'de ekranda en az bir pin kalsın.
                    onZoom = { z ->
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(places.first().position, z))
                    },
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    mode: MarkerMode,
    onBuggyChange: (Boolean) -> Unit,
    onBuggyKind: (MarkerMode) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hatalı", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = !mode.isBuggy,
                    onCheckedChange = { fixed -> onBuggyChange(!fixed) },
                    modifier = Modifier.testTag("modeSwitch"),
                )
                Spacer(Modifier.width(8.dp))
                Text("Düzeltilmiş", style = MaterialTheme.typography.bodyLarge)
            }
            if (mode.isBuggy) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(MarkerMode.BUGGY_PADDING, MarkerMode.BUGGY_ANCHOR).forEach { m ->
                        FilterChip(
                            selected = mode == m,
                            onClick = { onBuggyKind(m) },
                            label = { Text(if (m == MarkerMode.BUGGY_PADDING) "Şeffaf boşluk" else "Yanlış çapa") },
                            modifier = Modifier.testTag("chip_${m.name}"),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    zoom: Float,
    latitude: Double,
    density: Float,
    tipOffsetDp: Float,
    tipOffsetDx: Float,
    onZoom: (Float) -> Unit,
) {
    val offsetDp = hypot(tipOffsetDx.toDouble(), tipOffsetDp.toDouble())
    val errorM = MercatorMath.groundErrorMeters(offsetDp, latitude, zoom.toDouble())
    val dir = when {
        tipOffsetDp > 0f -> "aşağı"
        tipOffsetDp < 0f -> "yukarı"
        else -> ""
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Zoom: ${fmt(zoom.toDouble(), 2)}", modifier = Modifier.testTag("zoomText"))
            Text("Yoğunluk: 1 dp = ${fmt(density.toDouble(), 3)} px")
            Text(
                "Ekran kayması: ${fmt(offsetDp, 1)} dp = " +
                    "${fmt(MercatorMath.dpToPx(offsetDp, density), 1)} px $dir",
            )
            Text("Haritadaki karşılığı: ${formatMeters(errorM)} (Mercator formülü)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5f, 12f, 18f).forEach { z ->
                    OutlinedButton(onClick = { onZoom(z) }, modifier = Modifier.testTag("zoom_${z.toInt()}")) {
                        Text("z${z.toInt()}")
                    }
                }
            }
        }
    }
}

private fun fmt(v: Double, digits: Int) = String.format(Locale.forLanguageTag("tr"), "%.${digits}f", v)

private fun formatMeters(m: Double) = when {
    m >= 1000 -> "${fmt(m / 1000, 1)} km"
    else -> "${fmt(m, 1)} m"
}
