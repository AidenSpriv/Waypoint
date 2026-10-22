package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.ui.components.WaypointWalkingLoader
import kotlinx.coroutines.delay
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import com.example.ui.components.WaypointAiChatModal
import com.example.ui.screens.OnboardingScreen
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChooseRouteScreen
import com.example.ui.screens.ExploreMapScreen
import com.example.ui.screens.ModeratorScreen
import com.example.ui.screens.PausePointsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReportBarrierScreen
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceTranslucent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WaypointLime
import com.example.ui.theme.WaypointLimeBorder
import com.example.ui.theme.WaypointLimeContainer
import com.example.ui.theme.WaypointMint
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.WaypointViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WaypointApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaypointApp(viewModel: WaypointViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val pilotArea by viewModel.currentPilot.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCuratorModal by remember { mutableStateOf(false) }

    BackHandler(enabled = currentTab != AppTab.MAP) {
        viewModel.setTab(AppTab.MAP)
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val userProfile by viewModel.userProfile.collectAsState()
    val isReplayingOnboarding by viewModel.isReplayingOnboarding.collectAsState()
    val shouldShowOnboarding = !userProfile.isOnboardingCompleted || isReplayingOnboarding

    var showAiChatModal by remember { mutableStateOf(false) }

    var isAppStarting by remember { mutableStateOf(true) }
    var isStartupActive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1300)
        isStartupActive = false
    }

    if (isAppStarting) {
        WaypointWalkingLoader(
            isLoading = isStartupActive,
            isDarkTheme = true,
            headlineMessage = "Mapping your walk…",
            onFinished = { isAppStarting = false },
            modifier = Modifier.fillMaxSize()
        )
    } else if (shouldShowOnboarding) {
        OnboardingScreen(
            initialName = userProfile.name,
            initialEmail = userProfile.email,
            initialReferral = userProfile.referralSource,
            initialGoal = userProfile.primaryWalkingGoal,
            isReplaying = isReplayingOnboarding,
            onComplete = { completedProfile ->
                viewModel.completeOnboarding(completedProfile)
            },
            onDismiss = if (isReplayingOnboarding) {
                { viewModel.dismissReplayOnboarding() }
            } else null,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassBackground)
        ) {
        // Main Screen View with smooth fluid crossfade (220ms Apple-style)
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "screen_transition",
            modifier = Modifier.fillMaxSize()
        ) { tab ->
            when (tab) {
                AppTab.MAP -> ExploreMapScreen(
                    viewModel = viewModel,
                    onOpenAiChat = { showAiChatModal = true }
                )
                AppTab.ROUTES -> ChooseRouteScreen(viewModel = viewModel)
                AppTab.PAUSE_POINTS -> PausePointsScreen(viewModel = viewModel)
                AppTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                AppTab.REPORT -> ReportBarrierScreen(viewModel = viewModel)
                AppTab.REVIEW -> ModeratorScreen(viewModel = viewModel)
            }
        }

        // Floating Liquid Glass Top Header (visible on Explore & Facilities)
        if (currentTab == AppTab.MAP || currentTab == AppTab.PAUSE_POINTS) {
            FloatingTopHeader(
                onOpenCurator = { showCuratorModal = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            )
        }

        // Floating Liquid Glass Navigation Dock at bottom
        FloatingGlassNavigationDock(
            currentTab = currentTab,
            onTabSelected = { viewModel.setTab(it) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 70.dp)
        )

        // AI Chat Assistant Modal
        if (showAiChatModal) {
            WaypointAiChatModal(
                viewModel = viewModel,
                onDismissRequest = { showAiChatModal = false }
            )
        }


        // Curator & Pilot Modal Sheet
        if (showCuratorModal) {
            ModalBottomSheet(
                onDismissRequest = { showCuratorModal = false },
                containerColor = GlassBackground,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0x33FFFFFF))
                    )
                }
            ) {
                ModeratorScreen(viewModel = viewModel)
            }
        }
    }
}
}

/**
 * Apple-inspired floating top bar with translucent liquid glass material
 */
@Composable
private fun FloatingTopHeader(
    onOpenCurator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(12.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(GlassSurfaceTranslucent)
            .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand & Pilot Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_waypoint_emblem_trans),
                contentDescription = "WAYPOINT Official Logo",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "WAYPOINT",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DOHA",
                        color = WaypointLime,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(WaypointLimeContainer)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(WaypointMint)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "37° · 25° shaded",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Curator / Settings icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0x1AFFFFFF))
                .clickable { onOpenCurator() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Pilot Settings",
                tint = TextPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Apple-inspired floating liquid glass navigation dock
 */
@Composable
private fun FloatingGlassNavigationDock(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(GlassSurfaceTranslucent)
            .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DockItem(
            label = "Explore",
            icon = Icons.Default.Explore,
            isSelected = currentTab == AppTab.MAP,
            testTag = "nav_tab_explore",
            onClick = { onTabSelected(AppTab.MAP) }
        )

        DockItem(
            label = "Routes",
            icon = Icons.AutoMirrored.Filled.AltRoute,
            isSelected = currentTab == AppTab.ROUTES,
            testTag = "nav_tab_routes",
            onClick = { onTabSelected(AppTab.ROUTES) }
        )

        DockItem(
            label = "Facilities",
            icon = Icons.Default.WaterDrop,
            isSelected = currentTab == AppTab.PAUSE_POINTS,
            testTag = "nav_tab_facilities",
            onClick = { onTabSelected(AppTab.PAUSE_POINTS) }
        )

        DockItem(
            label = "Profile",
            icon = Icons.Default.Person,
            isSelected = currentTab == AppTab.PROFILE,
            testTag = "nav_tab_profile",
            onClick = { onTabSelected(AppTab.PROFILE) }
        )
    }
}

@Composable
private fun DockItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) WaypointLimeContainer else Color.Transparent
    val borderColor = if (isSelected) WaypointLimeBorder else Color.Transparent
    val contentColor = if (isSelected) WaypointLime else TextMuted

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(if (isSelected) 1.dp else 0.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
