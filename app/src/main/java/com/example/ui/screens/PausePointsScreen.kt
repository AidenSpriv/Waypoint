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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.Facility
import com.example.data.model.FacilityCategory
import com.example.data.model.LatLng
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WaypointCyan
import com.example.ui.theme.WaypointDarkBg
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointMint
import com.example.ui.theme.WaypointMintContainer
import com.example.ui.theme.WaypointOrange
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.WaypointViewModel

@Composable
fun PausePointsScreen(
    viewModel: WaypointViewModel,
    modifier: Modifier = Modifier
) {
    val pilotArea by viewModel.currentPilot.collectAsState()
    val facilities by viewModel.filteredFacilities.collectAsState()
    val searchQuery by viewModel.facilitySearchQuery.collectAsState()
    var activeCategoryTab by remember { mutableStateOf("All") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GlassBackground)
            .statusBarsPadding()
            .padding(top = 66.dp) // Below floating top bar
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
            .padding(bottom = 80.dp) // Above floating bottom dock
    ) {
        // Section Title: "Useful places nearby"
        Text(
            text = "Useful places nearby",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Chilled water, shaded rest & relief in ${pilotArea.shortName}",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setFacilitySearchQuery(it) },
            placeholder = { Text("Search water, shade, restrooms...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("search_pausepoints_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GlassSurfaceElevated,
                unfocusedContainerColor = GlassSurfaceElevated,
                focusedBorderColor = WaypointLime,
                unfocusedBorderColor = GlassBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Pills
        val categories = listOf("All", "Chilled Water", "Shaded Rest", "Restrooms")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = (cat == activeCategoryTab)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) WaypointLime else GlassSurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) WaypointLime else GlassBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            activeCategoryTab = cat
                            when (cat) {
                                "All" -> viewModel.setFacilityFilter(null)
                                "Chilled Water" -> viewModel.setFacilityFilter(FacilityCategory.WATER_REFILL)
                                "Shaded Rest" -> viewModel.setFacilityFilter(FacilityCategory.SHELTERED_REST)
                                "Restrooms" -> viewModel.setFacilityFilter(FacilityCategory.PUBLIC_TOILET)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("filter_pill_$cat"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) WaypointDarkBg else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Facilities List Count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${facilities.size} locations found",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "120 m — 420 m",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Facilities Rows
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(facilities) { fac ->
                FacilityRowCard(
                    facility = fac,
                    onSelect = {
                        val dest = LatLng(fac.latitude, fac.longitude)
                        viewModel.setDestinationPreset(fac.name, dest)
                        viewModel.selectFacility(fac)
                        viewModel.setTab(AppTab.MAP)
                    }
                )
            }
        }
    }
}

@Composable
private fun FacilityRowCard(
    facility: Facility,
    onSelect: () -> Unit
) {
    var isBookmarked by remember { mutableStateOf(false) }

    val (iconBg, iconEmoji) = when (facility.category) {
        FacilityCategory.WATER_REFILL -> Pair(Color(0xFF14242A), "💧")
        FacilityCategory.SHELTERED_REST, FacilityCategory.AC_WALKWAY -> Pair(Color(0xFF152219), "🏠")
        FacilityCategory.PUBLIC_TOILET -> Pair(Color(0xFF161E2E), "🚻")
        FacilityCategory.BENCH_SEATING -> Pair(Color(0xFF261D16), "🪑")
        FacilityCategory.METRO_ENTRANCE -> Pair(Color(0xFF261820), "🚇")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("pausepoint_card_${facility.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Bead
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg)
                    .border(1.dp, GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = facility.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${facility.category.displayName} · ${if (facility.verificationStatus.name == "VERIFIED") "Verified" else "Community"}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action: Directions Button
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = WaypointLime),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(32.dp).testTag("directions_button_${facility.id}")
            ) {
                Text(
                    text = "Directions",
                    color = GlassBackground,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
