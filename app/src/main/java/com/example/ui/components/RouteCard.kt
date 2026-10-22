package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalkingRoute
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WaypointGreen
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointRose
import com.example.ui.theme.WaypointAmber

@Composable
fun RouteCard(
    route: WalkingRoute,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) WaypointLime else NavyBorder
    val bgColor = if (isSelected) Navy800 else Navy700

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title & Selection Radio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) WaypointLime else Color(0xFF64748B))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = route.name,
                        color = if (isSelected) WaypointLime else TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                if (isSelected) {
                    Text(
                        text = "ACTIVE ROUTE",
                        color = WaypointLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(WaypointLime.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row: Distance, Walking Time, Shade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DISTANCE",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = route.distanceDisplay,
                        color = TextPrimaryDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "WALK TIME (EST.)",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = route.walkingTimeDisplay,
                        color = TextPrimaryDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "SURVEYED SHADE",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "${route.shadeShelterCoveragePercent}%",
                        color = if (route.shadeShelterCoveragePercent >= 50) WaypointGreen else TextSecondaryDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Proximity Amenities & Barriers summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Facilities along route
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Navy800)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💧", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${route.facilitiesNearby.size} PausePoints",
                        color = TextPrimaryDark,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Barriers along route
                val barCount = route.barriersNearby.size
                val barColor = if (barCount > 0) WaypointRose else WaypointGreen
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Navy800)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (barCount > 0) "⚠️" else "✓", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (barCount > 0) "$barCount Obstacle(s)" else "0 Obstacles",
                        color = barColor,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Transparent comparison explanation (if present)
            if (route.comparisonExplanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = route.comparisonExplanation,
                    color = WaypointLime,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (route.limitationsExplanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${route.limitationsExplanation}",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
