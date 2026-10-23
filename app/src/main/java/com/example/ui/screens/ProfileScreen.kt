package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.WaypointDarkBg
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointLimeBorder
import com.example.ui.theme.WaypointLimeContainer
import com.example.ui.theme.WaypointMint
import com.example.ui.theme.WaypointMintContainer
import com.example.ui.viewmodel.WaypointViewModel

@Composable
fun ProfileScreen(
    viewModel: WaypointViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val userProfile by viewModel.userProfile.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showPolicyDialog by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlassBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "User Profile",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Navigator Account & Walking Settings",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WaypointLimeContainer)
                        .border(1.dp, WaypointLimeBorder, CircleShape)
                        .clickable { showEditDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = WaypointLime,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // User Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(WaypointLimeContainer)
                                .border(2.dp, WaypointLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = WaypointLime,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.name.ifBlank { "" },
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(WaypointLimeContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Verified",
                                        color = WaypointLime,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = userProfile.email.ifBlank { "" },
                                color = TextSecondary,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Doha Pilot Member · Active Contributor",
                                color = WaypointMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(GlassBorder)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProfileStatItem(title = "Walks", value = "18", subtitle = "14.2 km")
                        ProfileStatItem(title = "Shaded", value = "88%", subtitle = "Cool paths")
                        ProfileStatItem(title = "Reports", value = "4", subtitle = "3 resolved")
                        ProfileStatItem(title = "Rating", value = "★ 5.0", subtitle = "Top curator")
                    }
                }
            }

            // Onboarding & Survey Discovery Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Onboarding & Discovery",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WaypointMintContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Saved Profile",
                                color = WaypointMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Suggested By", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = userProfile.referralSource.ifBlank { "Doha Mobility Initiative" },
                            color = WaypointLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Walking Priority", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = userProfile.primaryWalkingGoal.ifBlank { "Maximum Shade & Heat Avoidance" },
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    OutlinedButton(
                        onClick = { viewModel.triggerOnboarding() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("rerun_onboarding_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WaypointMint.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WaypointMint)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = WaypointMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Re-run Onboarding & Survey",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Walking Preferences Section
            Text(
                text = "Walking Preferences",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PreferenceToggleRow(
                        icon = Icons.Default.WbSunny,
                        title = "Prioritize Shaded Corridors",
                        description = "Route through Sikkat Al Wadi & covered colonnades",
                        checked = userProfile.preferShade,
                        onCheckedChange = { viewModel.updateUserProfile(userProfile.copy(preferShade = it)) }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorder))

                    PreferenceToggleRow(
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        title = "Step-Free & Wheelchair Ramps",
                        description = "Avoid curbs, construction steps and steep inclines",
                        checked = userProfile.stepFreeRequired,
                        onCheckedChange = { viewModel.updateUserProfile(userProfile.copy(stepFreeRequired = it)) }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorder))

                    PreferenceToggleRow(
                        icon = Icons.Default.Shield,
                        title = "Auto-avoid Reported Obstacles",
                        description = "Instantly re-route around live barriers reported by users",
                        checked = userProfile.autoAvoidObstacles,
                        onCheckedChange = { viewModel.updateUserProfile(userProfile.copy(autoAvoidObstacles = it)) }
                    )
                }
            }

            // Legal & Terms Section (Requested by user)
            Text(
                text = "Legal & Policies",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceTranslucent),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GlassBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Links and policy placeholders ready for custom URLs:",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    // Privacy Policy Button (Placeholder Link Button)
                    OutlinedButton(
                        onClick = {
                            showPolicyDialog = "Privacy Policy\n\nPlaceholder Link:\nhttps://waypointapp.qa/privacy-policy\n\nYour location and walking telemetry are processed on-device for real-time shaded routing."
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("privacy_policy_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = WaypointLime,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Privacy Policy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open link",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Terms and Conditions Button (Placeholder Link Button)
                    OutlinedButton(
                        onClick = {
                            showPolicyDialog = "Terms and Conditions\n\nPlaceholder Link:\nhttps://waypointapp.qa/terms-and-conditions\n\nWaypoint provides community-driven walking paths and barrier verification for Doha pedestrians."
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("terms_and_conditions_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = WaypointMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Terms and Conditions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open link",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Version & Brand footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_waypoint_emblem_trans),
                    contentDescription = "WAYPOINT Official Logo",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "WAYPOINT",
                    color = WaypointLime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Waypoint Doha · Climate-Conscious Navigation v1.2",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        var tempName by remember { mutableStateOf(userProfile.name) }
        var tempEmail by remember { mutableStateOf(userProfile.email) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text("Edit Profile Details", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Navigator Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WaypointLime,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("Gmail / Email Address") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WaypointLime,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = tempName.trim().ifEmpty { userProfile.name }
                        val finalEmail = tempEmail.trim().ifEmpty { userProfile.email }
                        viewModel.updateUserProfile(userProfile.copy(name = finalName, email = finalEmail))
                        showEditDialog = false
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaypointLime)
                ) {
                    Text("Save", color = GlassBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = WaypointDarkBg
        )
    }

    // Policy & Terms Placeholder Modal
    showPolicyDialog?.let { dialogText ->
        AlertDialog(
            onDismissRequest = { showPolicyDialog = null },
            title = {
                Text("Policy Placeholder", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(dialogText, color = TextSecondary, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Ready for custom URL link", Toast.LENGTH_SHORT).show()
                        showPolicyDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaypointLime)
                ) {
                    Text("OK", color = GlassBackground, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = WaypointDarkBg
        )
    }
}

@Composable
private fun ProfileStatItem(
    title: String,
    value: String,
    subtitle: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = WaypointLime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(text = subtitle, color = TextMuted, fontSize = 9.sp)
    }
}

@Composable
private fun PreferenceToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(WaypointLimeContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WaypointLime,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WaypointLime,
                checkedTrackColor = WaypointLimeContainer,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = GlassSurfaceElevated
            )
        )
    }
}
