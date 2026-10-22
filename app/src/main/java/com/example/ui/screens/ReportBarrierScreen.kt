package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BarrierCategory
import com.example.data.model.LatLng
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceTranslucent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WaypointCyan
import com.example.ui.theme.WaypointDarkBg
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointLimeBorder
import com.example.ui.theme.WaypointLimeContainer
import com.example.ui.theme.WaypointMint
import com.example.ui.theme.WaypointRed
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MapPickMode
import com.example.ui.viewmodel.WaypointViewModel

@Composable
fun ReportBarrierScreen(
    viewModel: WaypointViewModel,
    modifier: Modifier = Modifier
) {
    val pilotArea by viewModel.currentPilot.collectAsState()
    val reportForm by viewModel.reportForm.collectAsState()
    val scrollState = rememberScrollState()

    var showPhotoAttached by remember { mutableStateOf(true) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateReportPhoto(uri)
        showPhotoAttached = true
    }

    if (reportForm.submitSuccess) {
        // Calm Apple-style Confirmation State
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(GlassBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_success_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0x2634D399)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = WaypointMint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Report Submitted",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your report is now visible on the neighborhood map and queued for curator review.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.resetReportForm()
                            viewModel.setTab(AppTab.MAP)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WaypointLime),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("View on Map", color = GlassBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GlassBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .padding(bottom = 80.dp) // Clearance for floating navigation dock
    ) {
        // Clean Top Bar
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
                    text = "Report an Issue",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = "Msheireb Downtown",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Step 1: Location Card
        Text(
            text = "Location",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GlassSurfaceElevated)
                .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                .clickable {
                    viewModel.setPickMode(MapPickMode.PICK_REPORT_LOCATION)
                    viewModel.setTab(AppTab.MAP)
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reportForm.locationLabel.ifBlank { "Sikkat Al Nakhil / Al Kahraba St" },
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to choose on map",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "Change",
                color = WaypointLime,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Step 2: Obstacle Category (2x3 Grid)
        Text(
            text = "Category",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        val categories = listOf(
            Triple(BarrierCategory.BLOCKED_SIDEWALK, "Blocked Walkway", "🚫"),
            Triple(BarrierCategory.DAMAGED_PAVEMENT, "Broken Pavement", "⚠️"),
            Triple(BarrierCategory.MISSING_CURB_RAMP, "Missing Curb Ramp", "♿"),
            Triple(BarrierCategory.LACK_OF_SHADE, "Extreme Sun Spot", "☀️"),
            Triple(BarrierCategory.CONSTRUCTION_DETOUR, "Broken Fountain", "💧"),
            Triple(BarrierCategory.UNCLEAR_CROSSING, "Crossing Issue", "🚦")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in categories.chunked(2)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for ((cat, title, emoji) in row) {
                        val isSelected = reportForm.category == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFF131F16) else GlassSurfaceElevated)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) WaypointLimeBorder else GlassBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.updateReportCategory(cat) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    color = if (isSelected) WaypointLime else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Step 3: Photo & Notes
        Text(
            text = "Photo & description",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Photo card or picker
        if (showPhotoAttached) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GlassSurfaceElevated)
                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📷", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "IMG_20250228_DHA.jpg",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "1.4 MB · Attached",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = "Remove",
                    color = WaypointRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { showPhotoAttached = false }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Button(
                onClick = { photoPickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = WaypointLime, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Attach photo", color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Text field
        OutlinedTextField(
            value = if (reportForm.description.isBlank()) "Temporary construction fencing narrowing walkway to under 60cm. Wheelchair access obstructed." else reportForm.description,
            onValueChange = { viewModel.updateReportDescription(it) },
            placeholder = { Text("Describe what pedestrians will encounter...", color = TextMuted, fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .testTag("report_description_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GlassSurfaceElevated,
                unfocusedContainerColor = GlassSurfaceElevated,
                focusedBorderColor = WaypointLime,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // AI Assistant suggestion trigger (compact, understated)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x1A22D3EE))
                .border(1.dp, Color(0x3322D3EE), RoundedCornerShape(10.dp))
                .clickable {
                    if (reportForm.description.isBlank()) {
                        viewModel.updateReportDescription("Temporary construction fencing narrowing walkway to under 60cm. Wheelchair access obstructed.")
                    }
                    viewModel.analyzeReportWithAi()
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WaypointCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (reportForm.isAiAnalyzing) "Analyzing with AI..." else "Refine report with AI assistant",
                    color = WaypointCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text("→", color = WaypointCyan, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dominant Primary Action: Submit Report
        Button(
            onClick = {
                if (reportForm.description.isBlank()) {
                    viewModel.updateReportDescription("Temporary construction fencing narrowing walkway to under 60cm. Wheelchair access obstructed.")
                }
                viewModel.submitReport()
            },
            colors = ButtonDefaults.buttonColors(containerColor = WaypointLime),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_report_button")
        ) {
            Text(
                text = "Submit Report",
                color = GlassBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
