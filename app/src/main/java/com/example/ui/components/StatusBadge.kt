package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportStatus
import com.example.data.model.ReviewPriority
import com.example.data.model.VerificationStatus
import com.example.ui.theme.WaypointGreen
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointOrange
import com.example.ui.theme.WaypointRose
import com.example.ui.theme.WaypointAmber

@Composable
fun VerificationBadge(status: VerificationStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, dotColor, label) = when (status) {
        VerificationStatus.VERIFIED -> Quadruple(
            WaypointGreen.copy(alpha = 0.18f),
            WaypointGreen,
            WaypointGreen,
            "Field Survey Verified"
        )
        VerificationStatus.COMMUNITY_REPORTED -> Quadruple(
            WaypointAmber.copy(alpha = 0.18f),
            WaypointAmber,
            WaypointAmber,
            "Community Reported"
        )
        VerificationStatus.EXTERNALLY_SOURCED -> Quadruple(
            Color(0xFF38BDF8).copy(alpha = 0.18f),
            Color(0xFF38BDF8),
            Color(0xFF38BDF8),
            "OpenStreetMap (External)"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
fun ReportStatusBadge(status: ReportStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        ReportStatus.COMMUNITY_REPORTED -> Triple(WaypointAmber.copy(alpha = 0.15f), WaypointAmber, "Community Reported")
        ReportStatus.UNDER_REVIEW -> Triple(Color(0xFF818CF8).copy(alpha = 0.15f), Color(0xFF818CF8), "Under Review")
        ReportStatus.VERIFIED -> Triple(WaypointRose.copy(alpha = 0.15f), WaypointRose, "Confirmed Hazard")
        ReportStatus.RESOLVED -> Triple(WaypointGreen.copy(alpha = 0.15f), WaypointGreen, "Resolved / Cleared")
        ReportStatus.REJECTED_OR_DUPLICATE -> Triple(Color(0xFF94A3B8).copy(alpha = 0.15f), Color(0xFF94A3B8), "Rejected / Duplicate")
    }

    Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun PriorityBadge(priority: ReviewPriority, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (priority) {
        ReviewPriority.LOW -> Triple(Color(0xFF64748B).copy(alpha = 0.2f), Color(0xFF94A3B8), "Priority: Low")
        ReviewPriority.MEDIUM -> Triple(WaypointAmber.copy(alpha = 0.2f), WaypointAmber, "Priority: Medium")
        ReviewPriority.HIGH -> Triple(WaypointOrange.copy(alpha = 0.2f), WaypointOrange, "Priority: High")
        ReviewPriority.URGENT -> Triple(WaypointRose.copy(alpha = 0.25f), WaypointRose, "Priority: URGENT")
    }

    Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
