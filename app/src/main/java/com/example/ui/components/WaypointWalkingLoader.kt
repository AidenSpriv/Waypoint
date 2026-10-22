package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.WaypointForestGreen
import com.example.ui.theme.WaypointFreshLime
import com.example.ui.theme.WaypointNearBlack
import com.example.ui.theme.WaypointWarmIvory
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * WAYPOINT — Premium Animated Loading Experience
 * "A walk is about to begin"
 *
 * Implements a crafted custom vector scene with an illustrated minimalist character
 * walking along a progressive organic Bezier route toward an illuminated destination pin.
 */
@Composable
fun WaypointWalkingLoader(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    isDarkTheme: Boolean = false,
    headlineMessage: String? = null,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onFinished: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isReducedMotion = remember {
        try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }

    val bgColor = if (isDarkTheme) Color(0xFF090D14) else WaypointWarmIvory
    val primaryColor = if (isDarkTheme) Color(0xFFE2E8F0) else WaypointForestGreen
    val accentColor = WaypointFreshLime
    val subtleColor = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x2B123B2F)
    val textMutedColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF5A6B64)

    val statusMessages = remember {
        listOf(
            "Finding a better way…",
            "Mapping your walk…",
            "Looking for a little more shade…",
            "Checking the way ahead…"
        )
    }

    var currentMessageIndex by remember { mutableIntStateOf(0) }

    // Rotate messages calmly every 2.4s while loading
    LaunchedEffect(isLoading, errorMessage, headlineMessage) {
        if (isLoading && errorMessage == null && headlineMessage == null) {
            while (true) {
                delay(2400)
                currentMessageIndex = (currentMessageIndex + 1) % statusMessages.size
            }
        }
    }

    // Walking path progress [0f .. 1f]
    val routeProgress = remember { Animatable(if (isReducedMotion) 1f else 0f) }

    // Natural walk cadence animation
    val infiniteTransition = rememberInfiniteTransition(label = "walker_cycle")
    val walkPhase by if (isReducedMotion) {
        remember { mutableStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 680, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "walk_phase"
        )
    }

    // Destination subtle pulse animation as walker approaches
    val pulseScale by if (isReducedMotion) {
        remember { mutableStateOf(1.1f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse_scale"
        )
    }

    // Handle progress lifecycle with minimum display duration to avoid screen flashing
    LaunchedEffect(isLoading, errorMessage) {
        if (isLoading && errorMessage == null) {
            routeProgress.snapTo(0f)
            routeProgress.animateTo(
                targetValue = 0.92f,
                animationSpec = tween(durationMillis = 2200, easing = FastOutSlowInEasing)
            )
        } else if (!isLoading && errorMessage == null && routeProgress.value > 0f) {
            // Smoothly complete the last stride to destination
            routeProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
            delay(150)
            onFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .semantics { contentDescription = "Loading walking route, finding a better way" }
            .testTag("waypoint_animated_loader"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Discreet Official Waypoint Logo Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_waypoint_emblem_trans),
                    contentDescription = "WAYPOINT Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WAYPOINT",
                        color = primaryColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Climate-Conscious Navigation",
                        color = textMutedColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                }
            }

            // Central Hero Canvas: The living route & walking character
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Define organic curved Bezier route
                    val p0 = Offset(w * 0.12f, h * 0.65f)
                    val c1 = Offset(w * 0.36f, h * 0.26f)
                    val c2 = Offset(w * 0.62f, h * 0.82f)
                    val p3 = Offset(w * 0.88f, h * 0.38f)

                    // 1. Draw subtle background guide route
                    drawCurvedBezier(
                        p0 = p0, c1 = c1, c2 = c2, p3 = p3,
                        progress = 1f,
                        color = subtleColor,
                        strokeWidth = 2.dp.toPx(),
                        isDashed = true
                    )

                    // 2. Draw active living progressive route
                    val curProgress = routeProgress.value.coerceIn(0f, 1f)
                    if (curProgress > 0.01f) {
                        drawCurvedBezier(
                            p0 = p0, c1 = c1, c2 = c2, p3 = p3,
                            progress = curProgress,
                            color = primaryColor,
                            strokeWidth = 3.5.dp.toPx(),
                            isDashed = false
                        )

                        // Leading edge highlight segment (fresh lime glow)
                        val tipStart = (curProgress - 0.08f).coerceAtLeast(0f)
                        drawLeadingGlowSegment(
                            p0 = p0, c1 = c1, c2 = c2, p3 = p3,
                            fromProgress = tipStart,
                            toProgress = curProgress,
                            glowColor = accentColor,
                            strokeWidth = 4.2.dp.toPx()
                        )
                    }

                    // 3. Start Waypoint Origin Ring
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = p0
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.3f),
                        radius = 7.dp.toPx(),
                        center = p0,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // 4. Intermediate Waypoint Marker at t = 0.48
                    val waypointT = 0.48f
                    val waypointPos = evalCubicBezier(p0, c1, c2, p3, waypointT)
                    val isWaypointPassed = curProgress >= waypointT
                    drawCircle(
                        color = if (isWaypointPassed) accentColor else subtleColor,
                        radius = if (isWaypointPassed) 5.dp.toPx() else 4.dp.toPx(),
                        center = waypointPos
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 7.dp.toPx(),
                        center = waypointPos,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // 5. Destination Marker at p3
                    val isNearDestination = curProgress > 0.78f
                    if (isNearDestination) {
                        val pulseAlpha = ((1.45f - pulseScale) / 0.45f).coerceIn(0f, 0.7f)
                        drawCircle(
                            color = accentColor.copy(alpha = pulseAlpha),
                            radius = 12.dp.toPx() * pulseScale,
                            center = p3,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Destination Outer Pin Ring & Inner Core
                    drawCircle(
                        color = if (curProgress >= 0.98f) accentColor else primaryColor,
                        radius = 8.5.dp.toPx(),
                        center = p3,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawCircle(
                        color = if (curProgress >= 0.98f) accentColor else primaryColor,
                        radius = 4.dp.toPx(),
                        center = p3
                    )

                    // Destination stem line
                    drawLine(
                        color = primaryColor,
                        start = Offset(p3.x, p3.y + 8.5.dp.toPx()),
                        end = Offset(p3.x, p3.y + 14.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // 6. The Walking Character
                    if (curProgress > 0.02f) {
                        val currentPos = evalCubicBezier(p0, c1, c2, p3, curProgress)
                        val tangent = evalCubicBezierTangent(p0, c1, c2, p3, curProgress)
                        val angleDeg = (atan2(tangent.y, tangent.x) * 180f / PI.toFloat()).coerceIn(-25f, 25f)

                        drawWalkingCharacter(
                            center = currentPos,
                            headingDeg = angleDeg,
                            walkPhase = walkPhase,
                            primaryColor = primaryColor,
                            accentColor = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error State vs Rotating Human Status Messages
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0x33EF4444) else Color(0x1AEF4444)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444).copy(alpha = 0.4f)))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            Text(
                                text = "Unable to complete walk route",
                                color = if (isDarkTheme) Color.White else WaypointNearBlack,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = errorMessage,
                            color = textMutedColor,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            if (onRetry != null) {
                                Button(
                                    onClick = onRetry,
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = if (isDarkTheme) Color.Black else WaypointWarmIvory, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry", color = if (isDarkTheme) Color.Black else WaypointWarmIvory, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (onCancel != null) {
                                OutlinedButton(
                                    onClick = onCancel,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Dismiss", color = primaryColor, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // Smooth rotating status copy or direct headline message
                AnimatedContent(
                    targetState = headlineMessage ?: statusMessages[currentMessageIndex],
                    transitionSpec = {
                        fadeIn(animationSpec = tween(380)) togetherWith fadeOut(animationSpec = tween(380))
                    },
                    label = "status_text_transition"
                ) { msg ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = msg,
                            color = primaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.2).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Optimizing shade corridors & misted paths",
                            color = textMutedColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draws the organic cubic Bezier route segment up to the given progress [0..1]
 */
private fun DrawScope.drawCurvedBezier(
    p0: Offset,
    c1: Offset,
    c2: Offset,
    p3: Offset,
    progress: Float,
    color: Color,
    strokeWidth: Float,
    isDashed: Boolean
) {
    val steps = (60 * progress).toInt().coerceAtLeast(1)
    val path = Path()
    path.moveTo(p0.x, p0.y)

    for (i in 1..steps) {
        val t = (i.toFloat() / 60f).coerceAtMost(progress)
        val pt = evalCubicBezier(p0, c1, c2, p3, t)
        path.lineTo(pt.x, pt.y)
    }

    val pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(12f, 12f)) else null

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = pathEffect
        )
    )
}

/**
 * Draws luminous leading edge tip in fresh lime ahead of the character
 */
private fun DrawScope.drawLeadingGlowSegment(
    p0: Offset,
    c1: Offset,
    c2: Offset,
    p3: Offset,
    fromProgress: Float,
    toProgress: Float,
    glowColor: Color,
    strokeWidth: Float
) {
    if (toProgress <= fromProgress) return
    val path = Path()
    val startPt = evalCubicBezier(p0, c1, c2, p3, fromProgress)
    path.moveTo(startPt.x, startPt.y)

    val steps = 12
    for (i in 1..steps) {
        val t = fromProgress + (toProgress - fromProgress) * (i.toFloat() / steps)
        val pt = evalCubicBezier(p0, c1, c2, p3, t)
        path.lineTo(pt.x, pt.y)
    }

    drawPath(
        path = path,
        color = glowColor,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Draws the minimalist walking figure with natural walking cadence and subtle body bounce
 */
private fun DrawScope.drawWalkingCharacter(
    center: Offset,
    headingDeg: Float,
    walkPhase: Float,
    primaryColor: Color,
    accentColor: Color
) {
    // Subtle bounce on cadence (two steps per cycle)
    val bounce = -abs(sin(walkPhase * 2f)) * 3.5.dp.toPx()

    // Leg angles for natural stride (alternating ±24 deg)
    val legSwing = sin(walkPhase) * 24f
    val legAngle1 = legSwing
    val legAngle2 = -legSwing

    // Arm swing counter-balances legs
    val armAngle1 = -legSwing * 0.75f
    val armAngle2 = legSwing * 0.75f

    // Center character standing baseline at (center.x, center.y)
    val cx = center.x
    val cy = center.y + bounce

    rotate(degrees = headingDeg * 0.6f, pivot = center) {
        val hipY = cy - 14.dp.toPx()
        val shoulderY = cy - 24.dp.toPx()
        val headY = cy - 32.dp.toPx()

        // 1. Far Leg (Back leg)
        drawLeg(
            hip = Offset(cx - 1.dp.toPx(), hipY),
            angleDeg = legAngle2,
            length = 14.dp.toPx(),
            color = primaryColor.copy(alpha = 0.75f),
            shoeColor = accentColor
        )

        // 2. Far Arm (Back arm)
        drawArm(
            shoulder = Offset(cx - 2.dp.toPx(), shoulderY),
            angleDeg = armAngle2,
            length = 10.dp.toPx(),
            color = primaryColor.copy(alpha = 0.75f)
        )

        // 3. Torso (Subtle forward lean for purposeful walking)
        drawLine(
            color = primaryColor,
            start = Offset(cx - 1.5.dp.toPx(), hipY),
            end = Offset(cx + 1.dp.toPx(), shoulderY),
            strokeWidth = 3.6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 4. Head
        drawCircle(
            color = primaryColor,
            radius = 4.2.dp.toPx(),
            center = Offset(cx + 2.dp.toPx(), headY)
        )

        // 5. Near Leg (Front leg)
        drawLeg(
            hip = Offset(cx + 1.dp.toPx(), hipY),
            angleDeg = legAngle1,
            length = 14.dp.toPx(),
            color = primaryColor,
            shoeColor = accentColor
        )

        // 6. Near Arm (Front arm)
        drawArm(
            shoulder = Offset(cx + 2.dp.toPx(), shoulderY),
            angleDeg = armAngle1,
            length = 10.dp.toPx(),
            color = primaryColor
        )
    }
}

private fun DrawScope.drawLeg(
    hip: Offset,
    angleDeg: Float,
    length: Float,
    color: Color,
    shoeColor: Color
) {
    val rad = (angleDeg + 90f) * PI.toFloat() / 180f
    val footX = hip.x + cos(rad) * length
    val footY = hip.y + sin(rad) * length

    // Thigh and calf
    drawLine(
        color = color,
        start = hip,
        end = Offset(footX, footY),
        strokeWidth = 2.4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Subtle lime shoe accent
    drawCircle(
        color = shoeColor,
        radius = 1.8.dp.toPx(),
        center = Offset(footX + 1.2.dp.toPx(), footY)
    )
}

private fun DrawScope.drawArm(
    shoulder: Offset,
    angleDeg: Float,
    length: Float,
    color: Color
) {
    val rad = (angleDeg + 90f) * PI.toFloat() / 180f
    val handX = shoulder.x + cos(rad) * length
    val handY = shoulder.y + sin(rad) * length

    drawLine(
        color = color,
        start = shoulder,
        end = Offset(handX, handY),
        strokeWidth = 2.0.dp.toPx(),
        cap = StrokeCap.Round
    )
}

/**
 * Cubic Bezier evaluation P(t) = (1-t)³P0 + 3(1-t)²tC1 + 3(1-t)t²C2 + t³P3
 */
private fun evalCubicBezier(p0: Offset, c1: Offset, c2: Offset, p3: Offset, t: Float): Offset {
    val u = 1f - t
    val tt = t * t
    val uu = u * u
    val uuu = uu * u
    val ttt = tt * t

    val x = uuu * p0.x + 3f * uu * t * c1.x + 3f * u * tt * c2.x + ttt * p3.x
    val y = uuu * p0.y + 3f * uu * t * c1.y + 3f * u * tt * c2.y + ttt * p3.y
    return Offset(x, y)
}

/**
 * First derivative tangent of Cubic Bezier
 */
private fun evalCubicBezierTangent(p0: Offset, c1: Offset, c2: Offset, p3: Offset, t: Float): Offset {
    val u = 1f - t
    val dx = 3f * u * u * (c1.x - p0.x) + 6f * u * t * (c2.x - c1.x) + 3f * t * t * (p3.x - c2.x)
    val dy = 3f * u * u * (c1.y - p0.y) + 6f * u * t * (c2.y - c1.y) + 3f * t * t * (p3.y - c2.y)
    return Offset(dx, dy)
}
