package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.remote.PlaceSearchService
import com.example.data.remote.PlaceSearchResult
import com.example.ui.components.WaypointWalkingLoader
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BarrierCategory
import com.example.data.model.LatLng
import com.example.ui.components.BarrierDetailSheet
import com.example.ui.components.FacilityDetailSheet
import com.example.ui.components.WaypointMapView
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceTranslucent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WaypointDarkBg
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointLimeBorder
import com.example.ui.theme.WaypointLimeContainer
import com.example.ui.theme.WaypointMint
import com.example.ui.theme.WaypointMintContainer
import com.example.ui.theme.WaypointRed
import com.example.ui.theme.WaypointRedBorder
import com.example.ui.theme.WaypointRedContainer
import com.example.ui.viewmodel.MapPickMode
import com.example.ui.viewmodel.WaypointViewModel

data class SearchPlaceItem(
    val name: String,
    val description: String,
    val latLng: LatLng,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreMapScreen(
    viewModel: WaypointViewModel,
    onOpenAiChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pilotArea by viewModel.currentPilot.collectAsState()
    val mapState by viewModel.mapState.collectAsState()
    val facilities by viewModel.filteredFacilities.collectAsState()
    val barriers by viewModel.filteredBarriers.collectAsState()

    var isCardExpanded by remember { mutableStateOf(false) }
    var isWalkingActive by remember { mutableStateOf(false) }
    var hasRouteSelected by remember { mutableStateOf(false) }

    var isStartingWalkLoading by remember { mutableStateOf(false) }
    var isStartingWalkActive by remember { mutableStateOf(false) }

    LaunchedEffect(isStartingWalkActive) {
        if (isStartingWalkActive) {
            delay(1200)
            isStartingWalkActive = false
        }
    }

    var showRouteSearchModal by remember { mutableStateOf(false) }
    var showQuickReportModal by remember { mutableStateOf(false) }

    val activeRoute = if (mapState.destination != null && mapState.routes.isNotEmpty()) {
        mapState.routes.getOrNull(mapState.selectedRouteIndex) ?: mapState.routes.firstOrNull()
    } else {
        null
    }

    Box(modifier = modifier.fillMaxSize().background(GlassBackground)) {
        // Full Interactive Real Map Canvas
        WaypointMapView(
            pilotArea = pilotArea,
            facilities = facilities,
            barriers = barriers,
            routes = if (mapState.destination != null) mapState.routes else emptyList(),
            selectedRouteIndex = mapState.selectedRouteIndex,
            pickMode = mapState.pickMode,
            droppedPin = mapState.droppedPin,
            mapStyle = mapState.mapStyle,
            onMapClick = { lat, lng -> viewModel.handleMapClick(lat, lng) },
            onMarkerClick = { id, type ->
                if (type == "FACILITY") {
                    val fac = facilities.firstOrNull { it.id == id }
                    viewModel.selectFacility(fac)
                } else if (type == "BARRIER") {
                    val bar = barriers.firstOrNull { it.id == id }
                    viewModel.selectBarrier(bar)
                }
            },
            onRouteClick = { index -> viewModel.selectRouteIndex(index) },
            onCancelPickMode = { viewModel.setPickMode(MapPickMode.NONE) },
            onStyleChange = { viewModel.setMapStyle(it) },
            onSetDroppedPinAsOrigin = {
                viewModel.setDroppedPinAsOrigin()
                hasRouteSelected = true
            },
            onSetDroppedPinAsDestination = {
                viewModel.setDroppedPinAsDestination()
                hasRouteSelected = true
            },
            onClearDroppedPin = { viewModel.clearDroppedPin() },
            onReportOnSpot = { showQuickReportModal = true },
            isIslandExpanded = isCardExpanded && activeRoute != null,
            isWalkingActive = isWalkingActive,
            onOpenAiChat = onOpenAiChat,
            onOpenSearch = { showRouteSearchModal = true },
            modifier = Modifier.fillMaxSize()
        )

        // Live Active Walking HUD Banner (at top when walking is active, cleared below top header)
        AnimatedVisibility(
            visible = isWalkingActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 68.dp, start = 14.dp, end = 14.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = WaypointDarkBg.copy(alpha = 0.96f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WaypointLimeBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(WaypointLimeContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = null,
                                tint = WaypointLime,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Navigation Active · Step 1 of 4",
                                color = WaypointLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Head north through Shaded Sikkat Corridor",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Single-line Red Rounded "End Walk" Button
                    Button(
                        onClick = { isWalkingActive = false },
                        colors = ButtonDefaults.buttonColors(containerColor = WaypointRedContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WaypointRedBorder),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("end_walk_button")
                    ) {
                        Text(
                            text = "End Walk",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Floating Route Control Sheet (Hidden while walking to reveal clean map, spaced above bottom navigation)
        AnimatedVisibility(
            visible = !isWalkingActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 96.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .testTag("explore_bottom_sheet"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (activeRoute == null) {
                        // NO DESTINATION SELECTED: Clean, inviting prompt + Big Search Button as the ONLY option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Where to walk?",
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.4).sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Search a destination in ${pilotArea.shortName}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WaypointMintContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Ready to Plan",
                                    color = WaypointMint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large, prominent Search Button as the ONLY option
                        Button(
                            onClick = { showRouteSearchModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("search_destination_large_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = WaypointLime),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = GlassBackground,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Search Destination",
                                color = GlassBackground,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // DESTINATION SELECTED: Real route time, distance, shade %, and Start Walk button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCardExpanded = !isCardExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeRoute.walkingTimeDisplay.replace("~", ""),
                                    color = TextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.4).sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "· ${activeRoute.distanceDisplay}",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WaypointMintContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🌿 ${activeRoute.shadeShelterCoveragePercent}% Shaded",
                                        color = WaypointMint,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isCardExpanded = !isCardExpanded },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCardExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = if (isCardExpanded) "Collapse" else "Expand",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Route endpoints
                        Text(
                            text = "${mapState.originName.ifBlank { "Current Location" }} → ${mapState.destinationName.ifBlank { "Destination" }}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )

                        // Expandable Details (Apple Maps style telemetry)
                        AnimatedVisibility(
                            visible = isCardExpanded,
                            enter = expandVertically(animationSpec = tween(180)) + fadeIn(),
                            exit = shrinkVertically(animationSpec = tween(180)) + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                // Metrics Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MetricPill(
                                        icon = "🌿",
                                        label = "Shaded",
                                        value = "${activeRoute.shadeShelterCoveragePercent}%",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricPill(
                                        icon = "💨",
                                        label = "Cooling",
                                        value = "Misting Active",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricPill(
                                        icon = "♿",
                                        label = "Surface",
                                        value = "Step-Free",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Route comparison bar
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Direct Unshaded Route", color = TextMuted, fontSize = 11.sp)
                                            Text("+6°C hotter (Extreme)", color = WaypointRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = activeRoute.comparisonExplanation.ifBlank {
                                                "Waypoint routes through covered arcades and Sikkat Al Wadi, saving direct sunlight exposure."
                                            },
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons Row: Start Walk + Change Search
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Primary Start Walking Button
                            Button(
                                onClick = {
                                    if (!isWalkingActive) {
                                        isStartingWalkLoading = true
                                        isStartingWalkActive = true
                                    } else {
                                        isWalkingActive = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WaypointLime),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("main_route_action_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                    contentDescription = null,
                                    tint = GlassBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isWalkingActive) "Walking Active" else "Start Walk",
                                    color = GlassBackground,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Change / Re-search Destination Button
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GlassSurfaceElevated)
                                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                                    .clickable { showRouteSearchModal = true }
                                    .testTag("search_route_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search locations & routes",
                                    tint = WaypointLime,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sheets for Facility or Barrier Details
        mapState.selectedFacility?.let { fac ->
            FacilityDetailSheet(
                facility = fac,
                walkOrigin = mapState.origin,
                onClose = { viewModel.selectFacility(null) },
                onWalkHere = { selectedFac ->
                    val dest = LatLng(selectedFac.latitude, selectedFac.longitude)
                    viewModel.setDestinationPreset(selectedFac.name, dest)
                    hasRouteSelected = true
                    viewModel.selectFacility(null)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        mapState.selectedBarrier?.let { bar ->
            BarrierDetailSheet(
                barrier = bar,
                onClose = { viewModel.selectBarrier(null) },
                onStatusChange = { newStatus ->
                    viewModel.updateBarrierReportStatus(bar.id, newStatus, "Updated via Map workbench.")
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // 1. Loading experience when user starts a walk ("Checking the way ahead…")
        if (isStartingWalkLoading) {
            WaypointWalkingLoader(
                isLoading = isStartingWalkActive,
                isDarkTheme = true,
                headlineMessage = "Checking the way ahead…",
                onFinished = {
                    isStartingWalkLoading = false
                    isWalkingActive = true
                    Toast.makeText(context, "Walking navigation started!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Loading experience when calculating routes or handling route error
        if (mapState.isCalculatingRoutes || mapState.routeError != null) {
            WaypointWalkingLoader(
                isLoading = mapState.isCalculatingRoutes,
                isDarkTheme = true,
                headlineMessage = if (mapState.isCalculatingRoutes) "Finding a better way…" else null,
                errorMessage = mapState.routeError,
                onRetry = {
                    val orig = mapState.origin
                    val dest = mapState.destination
                    if (orig != null && dest != null) {
                        viewModel.calculateWalkingRoute(orig, dest)
                    } else {
                        viewModel.clearRouteError()
                    }
                },
                onCancel = {
                    viewModel.clearRouteError()
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Route Search & Destination Modal Sheet (Requested by user)
    if (showRouteSearchModal) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showRouteSearchModal = false },
            sheetState = sheetState,
            containerColor = WaypointDarkBg
        ) {
            RouteSearchContent(
                currentPilot = pilotArea,
                initialOrigin = mapState.originName.ifBlank { "Current Location (Msheireb Downtown)" },
                initialDest = mapState.destinationName,
                onRouteConfirmed = { originName, originCoord, destName, destCoord ->
                    viewModel.setCustomRoute(originName, originCoord, destName, destCoord)
                    hasRouteSelected = true
                    showRouteSearchModal = false
                    Toast.makeText(context, "Route calculated: $originName → $destName", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showRouteSearchModal = false }
            )
        }
    }

    // Quick On-The-Spot Barrier Report Modal (Triggered directly from Map Navigation Bar)
    if (showQuickReportModal) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showQuickReportModal = false },
            sheetState = sheetState,
            containerColor = WaypointDarkBg
        ) {
            QuickSpotReportContent(
                currentLat = pilotArea.centerLat,
                currentLng = pilotArea.centerLng,
                onSubmit = { category, description ->
                    val loc = mapState.droppedPin ?: LatLng(pilotArea.centerLat, pilotArea.centerLng)
                    viewModel.reportBarrierOnSpot(category, description, loc)
                    showQuickReportModal = false
                    Toast.makeText(context, "⚠️ Obstacle reported at your exact spot!", Toast.LENGTH_LONG).show()
                },
                onDismiss = { showQuickReportModal = false }
            )
        }
    }
}

private enum class SearchTargetField {
    DESTINATION,
    ORIGIN
}

/**
 * Route Search Sheet: Allows searching ANY real place in the world or local district
 * Connects directly to PlaceSearchService for live geocoding & route calculation.
 */
@Composable
private fun RouteSearchContent(
    currentPilot: com.example.data.model.PilotArea,
    initialOrigin: String,
    initialDest: String,
    onRouteConfirmed: (String, LatLng, String, LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    val placeSearchService = remember { PlaceSearchService() }
    val coroutineScope = rememberCoroutineScope()

    var originText by remember { mutableStateOf(initialOrigin) }
    var originCoord by remember { mutableStateOf(LatLng(currentPilot.centerLat, currentPilot.centerLng)) }
    var destText by remember { mutableStateOf(initialDest) }
    var destCoord by remember { mutableStateOf<LatLng?>(null) }

    var activeField by remember { mutableStateOf(SearchTargetField.DESTINATION) }
    var liveResults by remember { mutableStateOf<List<PlaceSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    // Popular, high-accuracy pedestrian destinations in Doha
    val curatedPlaces = remember {
        listOf(
            SearchPlaceItem(
                name = "Souq Waqif Metro Plaza",
                description = "Gold Line · Shaded pedestrian plaza & heritage arcade",
                latLng = LatLng(25.2879, 51.5358),
                icon = "🕌"
            ),
            SearchPlaceItem(
                name = "Msheireb Metro Interchange",
                description = "Red/Gold/Green hub · Exit 1 Sikkat Al Wadi",
                latLng = LatLng(25.2868, 51.5315),
                icon = "🚇"
            ),
            SearchPlaceItem(
                name = "Sikkat Al Wadi Shaded Arcade",
                description = "100% cooled & misted spine corridor",
                latLng = LatLng(25.2872, 51.5332),
                icon = "🌿"
            ),
            SearchPlaceItem(
                name = "Doha Corniche Dhow Harbour",
                description = "Waterfront shaded promenade & rest gazebos",
                latLng = LatLng(25.2915, 51.5385),
                icon = "⛵"
            ),
            SearchPlaceItem(
                name = "Museum of Islamic Art (MIA)",
                description = "MIA Park & waterfront shaded concourse",
                latLng = LatLng(25.2953, 51.5392),
                icon = "🏛️"
            ),
            SearchPlaceItem(
                name = "Al Bidda Park Gateway",
                description = "Covered trees corridor & open walking promenade",
                latLng = LatLng(25.2950, 51.5160),
                icon = "🌳"
            ),
            SearchPlaceItem(
                name = "Place Vendôme Shaded Connector",
                description = "Climate-controlled luxury walkway connector",
                latLng = LatLng(25.3680, 51.5280),
                icon = "🛍️"
            ),
            SearchPlaceItem(
                name = "Hamad Hospital Pedestrian Link",
                description = "Covered healthcare accessible ramps",
                latLng = LatLng(25.2920, 51.4980),
                icon = "🏥"
            ),
            SearchPlaceItem(
                name = "Katara Cultural Village",
                description = "Esplanade & shaded heritage amphitheatre",
                latLng = LatLng(25.3582, 51.5248),
                icon = "🎭"
            ),
            SearchPlaceItem(
                name = "Villaggio Mall Spine Link",
                description = "Aspire Park pedestrian connection",
                latLng = LatLng(25.2604, 51.4428),
                icon = "🏬"
            )
        )
    }

    val currentQuery = if (activeField == SearchTargetField.DESTINATION) destText.trim() else originText.trim()

    // Real-time live online search with debouncing
    LaunchedEffect(currentQuery) {
        if (currentQuery.length < 2) {
            liveResults = emptyList()
            isSearching = false
            searchError = null
            return@LaunchedEffect
        }
        isSearching = true
        searchError = null
        delay(350)
        try {
            val results = placeSearchService.searchPlaces(
                query = currentQuery,
                biasLat = currentPilot.centerLat,
                biasLng = currentPilot.centerLng
            )
            liveResults = results
        } catch (e: Exception) {
            searchError = e.message
        } finally {
            isSearching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Search & Plan Route",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Type any place, shop, landmark, or street in Doha & beyond",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        // Origin and Destination Inputs with Swap button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Starting Point Field
                OutlinedTextField(
                    value = originText,
                    onValueChange = {
                        originText = it
                        activeField = SearchTargetField.ORIGIN
                    },
                    label = {
                        Text(
                            if (activeField == SearchTargetField.ORIGIN) "Starting Location (Searching...)" else "Starting Location",
                            color = if (activeField == SearchTargetField.ORIGIN) WaypointLime else TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = if (activeField == SearchTargetField.ORIGIN) WaypointLime else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (originText.isNotEmpty()) {
                            IconButton(onClick = {
                                originText = ""
                                activeField = SearchTargetField.ORIGIN
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaypointLime,
                        unfocusedBorderColor = if (activeField == SearchTargetField.ORIGIN) WaypointLime.copy(alpha = 0.6f) else GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Swap Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            val tempText = originText
                            val tempCoord = originCoord
                            originText = destText
                            destText = tempText
                            if (destCoord != null) {
                                originCoord = destCoord!!
                            }
                            destCoord = tempCoord
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Swap locations", tint = WaypointMint, modifier = Modifier.size(20.dp))
                    }
                }

                // Destination Field
                OutlinedTextField(
                    value = destText,
                    onValueChange = {
                        destText = it
                        destCoord = null // Clear locked coord on new input
                        activeField = SearchTargetField.DESTINATION
                    },
                    label = {
                        Text(
                            if (activeField == SearchTargetField.DESTINATION) "Destination (Type any place)" else "Destination",
                            color = if (activeField == SearchTargetField.DESTINATION) WaypointLime else TextSecondary
                        )
                    },
                    placeholder = { Text("Search any mall, souq, hospital, hotel, address...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (destText.isNotEmpty()) {
                            IconButton(onClick = {
                                destText = ""
                                destCoord = null
                                activeField = SearchTargetField.DESTINATION
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaypointLime,
                        unfocusedBorderColor = if (activeField == SearchTargetField.DESTINATION) WaypointLime.copy(alpha = 0.6f) else GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Live Geocoding Loading Indicator
        if (isSearching) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = WaypointLime,
                    trackColor = GlassBorder
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Searching live map locations in Qatar...",
                        color = WaypointLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Search Results / Suggested Landmarks List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentQuery.length >= 2) {
                    if (liveResults.isNotEmpty()) "Live Results for \"$currentQuery\" (${liveResults.size})" else "Search Results"
                } else {
                    if (activeField == SearchTargetField.ORIGIN) "Select Starting Point" else "Popular Destinations in ${currentPilot.shortName}"
                },
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (currentQuery.length >= 2 && !isSearching) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(WaypointMintContainer.copy(alpha = 0.35f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Live Geocoding", color = WaypointMint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // If searching ORIGIN and query is short, show 1-click "Current Location (GPS)"
            if (activeField == SearchTargetField.ORIGIN && currentQuery.length < 2) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                originText = "Current Location (${currentPilot.shortName})"
                                originCoord = LatLng(currentPilot.centerLat, currentPilot.centerLng)
                                activeField = SearchTargetField.DESTINATION
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WaypointMintContainer.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📍", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Use My Current Location",
                                    color = WaypointMint,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${currentPilot.name} · Live GPS Anchor",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Real Live Online Search Results
            if (currentQuery.length >= 2) {
                if (liveResults.isEmpty() && !isSearching) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔍", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "No live places found matching \"$currentQuery\"",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Try searching by landmark name, metro station, street, or hotel",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    items(liveResults) { result ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (activeField == SearchTargetField.DESTINATION) {
                                        destText = result.name
                                        destCoord = result.latLng
                                        onRouteConfirmed(
                                            originText.ifBlank { "Current Location" },
                                            originCoord,
                                            result.name,
                                            result.latLng
                                        )
                                    } else {
                                        originText = result.name
                                        originCoord = result.latLng
                                        activeField = SearchTargetField.DESTINATION
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(result.iconEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            result.name,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(GlassSurfaceElevated)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                result.category,
                                                color = WaypointLime,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Text(
                                        result.subtitle,
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                                result.distanceDisplay?.let { dist ->
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        dist,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Curated places list when query is short
                items(curatedPlaces) { place ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (activeField == SearchTargetField.DESTINATION) {
                                    destText = place.name
                                    destCoord = place.latLng
                                    onRouteConfirmed(
                                        originText.ifBlank { "Current Location" },
                                        originCoord,
                                        place.name,
                                        place.latLng
                                    )
                                } else {
                                    originText = place.name
                                    originCoord = place.latLng
                                    activeField = SearchTargetField.DESTINATION
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(place.icon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(place.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(place.description, color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Confirm & View Walking Route Button
        Button(
            onClick = {
                val targetDestCoord = destCoord
                    ?: liveResults.firstOrNull()?.latLng
                    ?: curatedPlaces.firstOrNull { it.name.equals(destText, ignoreCase = true) }?.latLng

                val targetDestName = destText.ifBlank {
                    curatedPlaces.first().name
                }

                if (targetDestCoord != null) {
                    onRouteConfirmed(
                        originText.ifBlank { "Current Location" },
                        originCoord,
                        targetDestName,
                        targetDestCoord
                    )
                } else if (destText.isNotBlank()) {
                    // Perform real geocode resolution for the user's custom query
                    coroutineScope.launch {
                        isSearching = true
                        try {
                            val results = placeSearchService.searchPlaces(
                                destText.trim(),
                                currentPilot.centerLat,
                                currentPilot.centerLng
                            )
                            if (results.isNotEmpty()) {
                                val top = results.first()
                                onRouteConfirmed(
                                    originText.ifBlank { "Current Location" },
                                    originCoord,
                                    top.name,
                                    top.latLng
                                )
                            }
                        } finally {
                            isSearching = false
                        }
                    }
                } else {
                    val defaultPlace = curatedPlaces.first()
                    onRouteConfirmed(
                        originText.ifBlank { "Current Location" },
                        originCoord,
                        defaultPlace.name,
                        defaultPlace.latLng
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = WaypointLime),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = GlassBackground)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Confirm & View Walking Route", color = GlassBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Quick Spot Report Content: Directly reports obstacles anchored on the spot
 */
@Composable
private fun QuickSpotReportContent(
    currentLat: Double,
    currentLng: Double,
    onSubmit: (BarrierCategory, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(BarrierCategory.BLOCKED_SIDEWALK) }
    var description by remember { mutableStateOf("") }

    val categories: List<Pair<BarrierCategory, String>> = remember {
        listOf(
            BarrierCategory.BLOCKED_SIDEWALK to "Blocked Sidewalk",
            BarrierCategory.CONSTRUCTION_DETOUR to "Construction Detour",
            BarrierCategory.DAMAGED_PAVEMENT to "Damaged Pavement",
            BarrierCategory.MISSING_CURB_RAMP to "Missing Curb Ramp",
            BarrierCategory.LACK_OF_SHADE to "Extreme Sun / Lack of Shade",
            BarrierCategory.UNCLEAR_CROSSING to "Unclear Crossing"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x33F59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Report Obstacle On Spot", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Anchored at your exact spot (%.4f, %.4f)".format(currentLat, currentLng), color = WaypointMint, fontSize = 11.sp)
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Text("Select Obstacle Category", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for ((cat, label) in categories) {
                val isSelected = selectedCategory == cat
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategory = cat },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0x33F59E0B) else GlassSurfaceElevated
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) Color(0xFFF59E0B) else GlassBorder)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat.iconEmoji,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFFF59E0B) else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Details (Optional)") },
            placeholder = { Text("e.g. Scaffolding blocking path near cafe...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF59E0B),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSubmit(selectedCategory, description)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = GlassBackground)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Submit Report & Re-route Walkers", color = GlassBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MetricPill(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(GlassSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = "$icon $label",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
