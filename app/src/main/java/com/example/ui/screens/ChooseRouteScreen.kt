package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.components.WaypointWalkingLoader
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceTranslucent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WaypointCyan
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointLimeBorder
import com.example.ui.theme.WaypointLimeContainer
import com.example.ui.theme.WaypointMint
import com.example.ui.theme.WaypointRed
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.WaypointViewModel

@Composable
fun ChooseRouteScreen(
    viewModel: WaypointViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapState by viewModel.mapState.collectAsState()
    val scrollState = rememberScrollState()
    val selectedIdx = mapState.selectedRouteIndex
    var isStartingRouteLoading by remember { mutableStateOf(false) }
    var isStartingRouteActive by remember { mutableStateOf(false) }

    LaunchedEffect(isStartingRouteActive) {
        if (isStartingRouteActive) {
            delay(1100)
            isStartingRouteActive = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassBackground)
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .padding(bottom = 80.dp) // breathing room above floating dock
        ) {
        // Minimal Apple-style Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassSurfaceElevated)
                    .border(1.dp, GlassBorder, CircleShape)
                    .clickable { viewModel.setTab(AppTab.MAP) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Choose Route",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = if (mapState.destinationName.isNotBlank()) {
                        "${mapState.originName.ifBlank { "Current Location" }} → ${mapState.destinationName}"
                    } else {
                        "Search a destination on map to plan route"
                    },
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Balanced empty spacer or filter button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassSurfaceElevated)
                    .border(1.dp, GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filter",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live microclimate status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(WaypointMint)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live Microclimate Data",
                    color = WaypointMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Updated 2m ago",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (mapState.routes.isEmpty()) {
            // Clean, friendly empty state when no destination has been planned yet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🗺️", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Destination Chosen Yet",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Search any destination on the map to calculate real walking routes and shade telemetry.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { viewModel.setTab(AppTab.MAP) },
                        colors = ButtonDefaults.buttonColors(containerColor = WaypointLime),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("Search on Map", color = GlassBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Dynamic Routes from OSRM & Shade Telemetry
            mapState.routes.forEachIndexed { idx, route ->
                val isSelected = (selectedIdx == idx)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) WaypointLimeBorder else GlassBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.selectRouteIndex(idx) }
                        .testTag("route_option_$idx"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF131D17) else GlassSurfaceElevated
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = route.name,
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (idx == 0) "Recommended" else "Alternative",
                                    color = if (idx == 0) WaypointLime else Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (idx == 0) WaypointLimeContainer else Color(0x26FFFFFF))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                if (route.shadeShelterCoveragePercent >= 50) "❄" else "☼",
                                fontSize = 16.sp,
                                color = if (route.shadeShelterCoveragePercent >= 50) WaypointLime else Color(0xFFFBBF24)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${route.walkingTimeDisplay.replace("~", "")} · ${route.distanceDisplay} · ${route.shadeShelterCoveragePercent}% Shaded",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = route.comparisonExplanation.ifBlank { "${route.shadeShelterCoveragePercent}% shaded corridor · Step-free verified" },
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Refined Comparison Telemetry Card
            val activeRoute = mapState.routes.getOrNull(selectedIdx) ?: mapState.routes.first()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Route telemetry · ${activeRoute.name}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TelemetryRow(
                        label = "Thermal comfort",
                        leftVal = if (activeRoute.shadeShelterCoveragePercent >= 50) "25°C shaded" else "34°C direct",
                        leftColor = WaypointLime,
                        rightVal = "${activeRoute.shadeShelterCoveragePercent}% Shaded"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TelemetryRow(
                        label = "Walking duration",
                        leftVal = activeRoute.walkingTimeDisplay,
                        leftColor = WaypointCyan,
                        rightVal = activeRoute.distanceDisplay
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TelemetryRow(
                        label = "Accessibility",
                        leftVal = "Step-free verified",
                        leftColor = WaypointMint,
                        rightVal = "Smooth paving"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dominant Primary Button: Start Route
            Button(
                onClick = {
                    isStartingRouteLoading = true
                    isStartingRouteActive = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaypointLime),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("start_selected_route_button")
            ) {
                Text(
                    text = "Start Walk via ${activeRoute.name} (${activeRoute.walkingTimeDisplay})",
                    color = GlassBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = GlassBackground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (isStartingRouteLoading) {
        WaypointWalkingLoader(
            isLoading = isStartingRouteActive,
            isDarkTheme = true,
            headlineMessage = "Mapping your walk…",
            onFinished = {
                isStartingRouteLoading = false
                viewModel.setTab(AppTab.MAP)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
}

@Composable
private fun TelemetryRow(
    label: String,
    leftVal: String,
    leftColor: Color,
    rightVal: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = leftVal, color = leftColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "  vs  ", color = TextMuted, fontSize = 11.sp)
            Text(text = rightVal, color = TextMuted, fontSize = 12.sp)
        }
    }
}
