package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Navy700
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointLimeContainer

@Composable
fun FilterChipItem(
    label: String,
    icon: String? = null,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) WaypointLimeContainer else Navy700
    val borderColor = if (isSelected) WaypointLime else NavyBorder
    val textColor = if (isSelected) WaypointLime else TextSecondaryDark

    Row(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!icon.isNullOrEmpty()) {
            Text(
                text = icon,
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
