package com.example.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BarrierReport
import com.example.data.model.Facility
import com.example.data.model.LatLng
import com.example.data.model.PilotArea
import com.example.data.model.WalkingRoute
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceTranslucent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WaypointBorder
import com.example.ui.theme.WaypointDarkBg
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointLimeContainer
import com.example.ui.theme.WaypointMint
import com.example.ui.theme.WaypointSurface
import com.example.ui.viewmodel.MapPickMode
import org.json.JSONArray
import org.json.JSONObject

class WaypointMapBridge(
    private val onMapClick: (Double, Double) -> Unit,
    private val onMarkerClick: (String, String) -> Unit,
    private val onRouteClick: (Int) -> Unit,
    private val onReady: () -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onMapClicked(lat: Double, lng: Double) {
        mainHandler.post { onMapClick(lat, lng) }
    }

    @JavascriptInterface
    fun onMarkerClicked(id: String, type: String) {
        mainHandler.post { onMarkerClick(id, type) }
    }

    @JavascriptInterface
    fun onRouteClicked(index: Int) {
        mainHandler.post { onRouteClick(index) }
    }

    @JavascriptInterface
    fun onMapReady() {
        mainHandler.post { onReady() }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WaypointMapView(
    pilotArea: PilotArea,
    facilities: List<Facility>,
    barriers: List<BarrierReport>,
    routes: List<WalkingRoute>,
    selectedRouteIndex: Int,
    pickMode: MapPickMode,
    droppedPin: LatLng? = null,
    mapStyle: String = "google_streets",
    onMapClick: (Double, Double) -> Unit,
    onMarkerClick: (String, String) -> Unit,
    onRouteClick: (Int) -> Unit,
    onCancelPickMode: () -> Unit,
    onStyleChange: (String) -> Unit = {},
    onSetDroppedPinAsOrigin: () -> Unit = {},
    onSetDroppedPinAsDestination: () -> Unit = {},
    onClearDroppedPin: () -> Unit = {},
    onReportOnSpot: () -> Unit = {},
    isIslandExpanded: Boolean = false,
    isWalkingActive: Boolean = false,
    onOpenAiChat: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    var isNavToolsExpanded by remember { mutableStateOf(false) }
    var showThemeFlyout by remember { mutableStateOf(false) }

    val bridge = remember {
        WaypointMapBridge(
            onMapClick = { lat, lng ->
                isNavToolsExpanded = false
                showThemeFlyout = false
                onMapClick(lat, lng)
            },
            onMarkerClick = onMarkerClick,
            onRouteClick = onRouteClick,
            onReady = {
                isMapReady = true
            }
        )
    }

    fun evalJs(script: String) {
        val webView = webViewRef
        if (webView != null) {
            webView.post {
                webView.evaluateJavascript(script) { result ->
                    Log.d("WaypointMapView", "evalJs: $script -> $result")
                }
            }
        } else {
            Log.w("WaypointMapView", "evalJs called but webViewRef is null! script: $script")
        }
    }

    // Sync map style changes
    LaunchedEffect(isMapReady, mapStyle) {
        if (!isMapReady) return@LaunchedEffect
        evalJs("setMapStyle('$mapStyle');")
    }

    // Sync dropped pin
    LaunchedEffect(isMapReady, droppedPin) {
        if (!isMapReady) return@LaunchedEffect
        if (droppedPin != null) {
            evalJs("setDroppedPin(${droppedPin.latitude}, ${droppedPin.longitude});")
        } else {
            evalJs("clearDroppedPin();")
        }
    }

    // Atomic data sync to map
    LaunchedEffect(isMapReady, facilities, barriers, routes, selectedRouteIndex, pilotArea) {
        if (!isMapReady) return@LaunchedEffect

        evalJs("setCenter(${pilotArea.centerLat}, ${pilotArea.centerLng}, ${pilotArea.defaultZoom});")

        val facJsonArr = JSONArray()
        facilities.forEach { fac ->
            val facObj = JSONObject()
            facObj.put("id", fac.id)
            facObj.put("name", fac.name)
            facObj.put("category", fac.category.name)
            facObj.put("lat", fac.latitude)
            facObj.put("lng", fac.longitude)
            facObj.put("isVerified", fac.verificationStatus.name == "VERIFIED")
            facJsonArr.put(facObj)
        }

        val barJsonArr = JSONArray()
        barriers.forEach { bar ->
            val barObj = JSONObject()
            barObj.put("id", bar.id)
            barObj.put("category", bar.category.name)
            barObj.put("lat", bar.latitude)
            barObj.put("lng", bar.longitude)
            barObj.put("status", bar.status.name)
            barJsonArr.put(barObj)
        }

        evalJs("setMarkers($facJsonArr, $barJsonArr);")

        if (routes.isNotEmpty()) {
            val jsonArray = JSONArray()
            routes.forEach { r ->
                val routeObj = JSONObject()
                routeObj.put("id", r.id)
                val pointsArr = JSONArray()
                r.points.forEach { pt ->
                    val coordArr = JSONArray().put(pt.latitude).put(pt.longitude)
                    pointsArr.put(coordArr)
                }
                routeObj.put("points", pointsArr)
                jsonArray.put(routeObj)
            }
            evalJs("displayRoutes($jsonArray, $selectedRouteIndex);")
        } else {
            evalJs("clearRoutes();")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interactive_map_view"),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true
                        loadsImagesAutomatically = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36"
                        setSupportZoom(false) // Leaflet handles touch gestures
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            Log.d("WaypointMapJS", "${consoleMessage?.message()} [line ${consoleMessage?.lineNumber()}]")
                            return true
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript(
                                "if (typeof initMap === 'function' && !map) { initMap(${pilotArea.centerLat}, ${pilotArea.centerLng}, ${pilotArea.defaultZoom}); } if (typeof invalidateMapSize === 'function') { invalidateMapSize(); }",
                                null
                            )
                            isMapReady = true
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            Log.w("WaypointMapView", "WebView error: $description at $failingUrl")
                        }
                    }
                    addJavascriptInterface(bridge, "WaypointBridge")
                    
                    val htmlContent = try {
                        context.assets.open("leaflet_map.html").bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        Log.e("WaypointMapView", "Error reading leaflet_map.html", e)
                        ""
                    }
                    if (htmlContent.isNotEmpty()) {
                        loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
                    } else {
                        loadUrl("file:///android_asset/leaflet_map.html")
                    }
                    webViewRef = this
                }
            },
            update = { view ->
                webViewRef = view
                view.evaluateJavascript("if (typeof invalidateMapSize === 'function') invalidateMapSize();", null)
            }
        )

        val islandBottomPadding by animateDpAsState(
            targetValue = if (isWalkingActive) 104.dp else if (isIslandExpanded) 445.dp else 272.dp,
            label = "nav_island_offset"
        )

        // Integrated Navigation Island (Expandable & Collapsible)
        // Positioned down near the bottom island bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 14.dp, bottom = islandBottomPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Round, small floating AI button on top of the already existing navigation (Wayfinder)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(WaypointDarkBg.copy(alpha = 0.95f))
                        .border(1.5.dp, WaypointLime, CircleShape)
                        .clickable { onOpenAiChat() }
                        .testTag("wayfinder_ai_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Wayfinder AI Assistant",
                        tint = WaypointLime,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                // Flyout Theme Selector (Google Maps / Satellite / Dark)
                AnimatedVisibility(
                    visible = showThemeFlyout && isNavToolsExpanded,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(180))
                ) {
                    Row(
                        modifier = Modifier
                            .shadow(12.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(WaypointDarkBg.copy(alpha = 0.96f))
                            .border(1.dp, WaypointBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeOptionButton(
                            label = "🗺️ Map",
                            isSelected = mapStyle == "google_streets" || mapStyle == "streets",
                            onClick = {
                                onStyleChange("google_streets")
                                showThemeFlyout = false
                            },
                            testTag = "theme_streets_button"
                        )
                        ThemeOptionButton(
                            label = "🛰️ Sat",
                            isSelected = mapStyle == "google_satellite" || mapStyle == "satellite",
                            onClick = {
                                onStyleChange("google_satellite")
                                showThemeFlyout = false
                            },
                            testTag = "theme_satellite_button"
                        )
                        ThemeOptionButton(
                            label = "🌙 Dark",
                            isSelected = mapStyle == "google_dark" || mapStyle == "dark",
                            onClick = {
                                onStyleChange("google_dark")
                                showThemeFlyout = false
                            },
                            testTag = "theme_dark_button"
                        )
                    }
                }

                // Main Expandable Tactical Navigation Island
                Column(
                    modifier = Modifier
                        .shadow(16.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(WaypointDarkBg.copy(alpha = 0.95f))
                        .border(1.dp, WaypointBorder, RoundedCornerShape(22.dp))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Expandable Navigation Tools Panel (Hidden when collapsed to save space)
                    AnimatedVisibility(
                        visible = isNavToolsExpanded,
                        enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                        exit = fadeOut(tween(180)) + shrinkVertically(tween(220))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Pinpoint Exact Location (MyLocation with GPS pulse beacon & recenter)
                            MapToolButton(
                                icon = Icons.Default.MyLocation,
                                contentDescription = "Pinpoint Exact Location",
                                tint = WaypointLime,
                                onClick = {
                                    evalJs("if (typeof pinpointExactLocation === 'function') { pinpointExactLocation(${pilotArea.centerLat}, ${pilotArea.centerLng}); } else if (typeof setCenter === 'function') { setCenter(${pilotArea.centerLat}, ${pilotArea.centerLng}, 17); }")
                                },
                                testTag = "recenter_map_button"
                            )

                            // Zoom In (Increase size of map)
                            MapToolButton(
                                icon = Icons.Default.Add,
                                contentDescription = "Zoom In (Increase Map Size)",
                                tint = TextPrimary,
                                onClick = {
                                    evalJs("if (typeof zoomIn === 'function') { zoomIn(); } else if (window.map) { window.map.zoomIn(1); }")
                                },
                                testTag = "zoom_in_button"
                            )

                            // Zoom Out / Minimize (Decrease size of map)
                            MapToolButton(
                                icon = Icons.Default.Remove,
                                contentDescription = "Zoom Out (Minimize Map)",
                                tint = TextPrimary,
                                onClick = {
                                    evalJs("if (typeof zoomOut === 'function') { zoomOut(); } else if (window.map) { window.map.zoomOut(1); }")
                                },
                                testTag = "zoom_out_button"
                            )

                            // Horizontal divider
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(1.dp)
                                    .background(WaypointBorder)
                            )

                            // Integrated Map Theme Selector
                            MapToolButton(
                                icon = Icons.Default.Layers,
                                contentDescription = "Map Theme (Google Maps / Satellite / Dark)",
                                tint = if (showThemeFlyout) WaypointLime else if (mapStyle.contains("satellite")) WaypointMint else if (mapStyle.contains("dark")) Color(0xFF93C5FD) else TextPrimary,
                                onClick = {
                                    showThemeFlyout = !showThemeFlyout
                                },
                                testTag = "map_layer_toggle_button"
                            )

                            // Horizontal divider
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(1.dp)
                                    .background(WaypointBorder)
                            )

                            // Report Obstacle on the Spot
                            MapToolButton(
                                icon = Icons.Default.Warning,
                                contentDescription = "Report Obstacle On Spot",
                                tint = Color(0xFFF59E0B), // Vibrant Amber Warning
                                onClick = onReportOnSpot,
                                testTag = "report_obstacle_on_spot_button"
                            )

                            // Horizontal divider above toggle
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(1.dp)
                                    .background(WaypointBorder)
                            )
                        }
                    }

                    // Master Expand / Collapse Toggle Button (always visible, saves map space when collapsed)
                    MapToolButton(
                        icon = if (isNavToolsExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.Tune,
                        contentDescription = if (isNavToolsExpanded) "Collapse Navigation Bar" else "Expand Navigation Bar",
                        tint = if (isNavToolsExpanded) WaypointLime else TextPrimary,
                        onClick = {
                            isNavToolsExpanded = !isNavToolsExpanded
                            if (!isNavToolsExpanded) {
                                showThemeFlyout = false
                            }
                        },
                        testTag = "toggle_map_nav_expansion_button"
                    )
                }
            }

                // Small search button right below the navigation UI when walking is active
                AnimatedVisibility(
                    visible = isWalkingActive,
                    enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                    exit = fadeOut(tween(180)) + shrinkVertically(tween(220))
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(12.dp, CircleShape)
                            .clip(CircleShape)
                            .background(WaypointDarkBg.copy(alpha = 0.95f))
                            .border(1.5.dp, WaypointLime, CircleShape)
                            .clickable { onOpenSearch() }
                            .testTag("small_search_walking_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search & Route Options",
                            tint = WaypointLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Banner when Pick Mode is active
        AnimatedVisibility(
            visible = pickMode != MapPickMode.NONE,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp, start = 16.dp, end = 16.dp)
        ) {
            val promptText = when (pickMode) {
                MapPickMode.PICK_ORIGIN -> "Tap real map to set Start Point"
                MapPickMode.PICK_DESTINATION -> "Tap real map to set Destination"
                MapPickMode.PICK_REPORT_LOCATION -> "Tap real map to set obstacle spot"
                MapPickMode.NONE -> ""
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(WaypointDarkBg.copy(alpha = 0.96f))
                    .border(1.dp, WaypointLime, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = promptText,
                    color = WaypointLime,
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onCancelPickMode,
                    modifier = Modifier.size(24.dp).testTag("cancel_pick_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Dropped Pin Action Banner (Floating interactive action bar)
        AnimatedVisibility(
            visible = droppedPin != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📍", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Dropped Pin",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                droppedPin?.let {
                                    Text(
                                        text = "${String.format("%.4f", it.latitude)}, ${String.format("%.4f", it.longitude)}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onClearDroppedPin,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSetDroppedPinAsDestination,
                            colors = ButtonDefaults.buttonColors(containerColor = WaypointLime),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = null,
                                tint = GlassBackground,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Walk Here", color = GlassBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onSetDroppedPinAsOrigin,
                            colors = ButtonDefaults.buttonColors(containerColor = GlassSurfaceTranslucent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Here", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) WaypointLimeContainer else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) WaypointLime.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = WaypointLime)
            ) { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) WaypointLime else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun MapToolButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = WaypointLime)
            ) { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
