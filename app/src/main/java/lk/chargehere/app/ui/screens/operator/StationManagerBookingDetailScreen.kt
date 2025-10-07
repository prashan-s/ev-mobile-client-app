package lk.chargehere.app.ui.screens.operator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.domain.model.Reservation
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun StationManagerBookingDetailScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    viewModel: StationManagerBookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCompleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        viewModel.loadBooking(bookingId)
    }

    LaunchedEffect(uiState.completionSuccess) {
        if (uiState.completionSuccess) {
            showCompleteDialog = false
            viewModel.onCompletionHandled()
            onNavigateBack()
        }
    }

    var swipeOffset by remember { mutableStateOf(0f) }
    val swipeThreshold = 300f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClarityBackgroundGray)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset > swipeThreshold) {
                            onNavigateBack()
                        }
                        swipeOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (dragAmount > 0) { // Only allow right swipe
                            swipeOffset += dragAmount
                        }
                    }
                )
            }
    ) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ClaritySpacing.md)
                ) {
                    // Header shimmer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = ClaritySpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerEffect(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                        ShimmerEffect(
                            modifier = Modifier
                                .width(150.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(ClaritySpacing.md))

                    // Booking card shimmer
                    BookingCardShimmer(modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(ClaritySpacing.lg))

                    // Additional details shimmer
                    ClarityCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)) {
                            repeat(5) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ShimmerEffect(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                                    Column(modifier = Modifier.weight(1f)) {
                                        ShimmerEffect(
                                            modifier = Modifier
                                                .fillMaxWidth(0.3f)
                                                .height(14.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        ShimmerEffect(
                                            modifier = Modifier
                                                .fillMaxWidth(0.6f)
                                                .height(18.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(ClaritySpacing.lg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ClarityErrorRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(ClaritySpacing.md))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ClarityMediumGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(ClaritySpacing.lg))
                        ClarityPrimaryButton(
                            text = "Try Again",
                            onClick = { viewModel.loadBooking(bookingId) }
                        )
                    }
                }
            }

            uiState.booking != null -> {
                StationManagerBookingContent(
                    booking = uiState.booking!!,
                    onComplete = { showCompleteDialog = true },
                    onNavigateBack = onNavigateBack
                )
            }
        }

        // Complete booking dialog
        if (showCompleteDialog) {
            CompleteBookingDialog(
                isLoading = uiState.isCompleting,
                error = uiState.completionError,
                onDismiss = {
                    showCompleteDialog = false
                    viewModel.clearError()
                },
                onConfirm = {
                    viewModel.completeBooking(bookingId)
                }
            )
        }
    }
}

@Composable
private fun StationManagerBookingContent(
    booking: Reservation,
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val startDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(booking.startTime),
        ZoneId.systemDefault()
    )
    val endDateTime = booking.endTime?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
    } ?: startDateTime.plusMinutes(booking.durationMinutes.toLong())

    // Entry animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val animatedOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 30.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "offset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .alpha(animatedAlpha)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ClarityDarkGray
                )
            }
            Spacer(modifier = Modifier.width(ClaritySpacing.sm))
            Text(
                text = "Booking Details",
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = ClaritySpacing.md)
                .offset(y = animatedOffset)
        ) {
            // Highlighted information card
            ClarityCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Booking Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ClarityDarkGray
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.lg))

                    // Customer name - highlighted
                    HighlightedInfoRow(
                        icon = Icons.Default.Person,
                        label = "Customer",
                        value = booking.evOwnerName ?: "N/A",
                        iconColor = ClarityAccentBlue
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.md))

                    // Duration - highlighted
                    HighlightedInfoRow(
                        icon = Icons.Default.Timer,
                        label = "Duration",
                        value = "${booking.durationMinutes} minutes",
                        iconColor = ClarityWarningOrange
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.md))

                    // Amount - highlighted
                    booking.pricePerHour?.let { price ->
                        val totalAmount = (price * booking.durationMinutes / 60.0)
                        val formattedAmount = if (totalAmount % 1.0 == 0.0) {
                            totalAmount.toInt().toString()
                        } else {
                            String.format("%.2f", totalAmount)
                        }
                        HighlightedInfoRow(
                            icon = Icons.Default.Payments,
                            label = "Amount",
                            value = "Rs $formattedAmount",
                            iconColor = ClaritySuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            // Additional booking details
            ClarityCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
                ) {
                    Text(
                        text = "Additional Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = ClarityDarkGray
                    )

                    InfoRow(
                        icon = Icons.Default.EvStation,
                        label = "Station",
                        value = booking.stationName
                    )

                    InfoRow(
                        icon = Icons.Default.CalendarMonth,
                        label = "Date",
                        value = startDateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
                    )

                    InfoRow(
                        icon = Icons.Default.Schedule,
                        label = "Time Slot",
                        value = "${startDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${endDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                    )

                    booking.physicalSlot?.let { slot ->
                        InfoRow(
                            icon = Icons.Default.EventSeat,
                            label = "Slot",
                            value = "Slot $slot"
                        )
                    }

                    InfoRow(
                        icon = Icons.Default.Tag,
                        label = "Booking ID",
                        value = booking.bookingNumber ?: "#${booking.id.takeLast(8).uppercase()}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            // Complete button
            ClarityPrimaryButton(
                text = "Complete Booking",
                onClick = onComplete,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
        }
    }
}

@Composable
private fun HighlightedInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClaritySpacing.sm),
        colors = CardDefaults.cardColors(
            containerColor = iconColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(ClaritySpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = ClarityMediumGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = ClarityDarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ClarityAccentBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(ClaritySpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ClarityMediumGray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = ClarityDarkGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CompleteBookingDialog(
    isLoading: Boolean = false,
    error: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Success icon pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "successPulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    ClarityModal(
        onDismiss = if (isLoading) { {} } else onDismiss,
        modifier = Modifier.padding(ClaritySpacing.lg)
    ) {
        Column(
            modifier = Modifier.padding(ClaritySpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(if (isLoading) 1f else iconScale)
                    .background(ClaritySuccessGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ClaritySuccessGreen,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.md))

            Text(
                text = "Complete Booking?",
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.sm))

            Text(
                text = "Are you sure you want to mark this booking as complete?",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                textAlign = TextAlign.Center
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(ClaritySpacing.sm))
                ClarityFormattedError(
                    errorMessage = error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
            ) {
                ClarityTextButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )

                ClarityPrimaryButton(
                    text = if (isLoading) "Completing..." else "Complete",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
            }
        }
    }
}
