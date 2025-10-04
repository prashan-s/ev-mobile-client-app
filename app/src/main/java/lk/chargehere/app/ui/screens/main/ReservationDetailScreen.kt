package lk.chargehere.app.ui.screens.main

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.domain.model.Reservation
import lk.chargehere.app.ui.viewmodel.ReservationDetailViewModel
import lk.chargehere.app.ui.components.QRCodeGenerator
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservationId: String,
    onNavigateBack: () -> Unit,
    viewModel: ReservationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(reservationId) {
        viewModel.loadReservationDetail(reservationId)
    }

    // Auto-dismiss dialog on cancellation success
    LaunchedEffect(uiState.cancellationSuccess) {
        if (uiState.cancellationSuccess) {
            showCancelDialog = false
            viewModel.clearCancellationSuccess()
        }
    }

    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    Box(modifier = Modifier.fillMaxSize()) {
        ClarityBackground {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Clean Header Bar
                ClarityDetailHeader(
                    title = "Booking Details",
                    onNavigateBack = onNavigateBack
                )

                when {
                    uiState.isLoading -> {
                        ClarityDetailLoadingState()
                    }

                    uiState.error != null -> {
                        ClarityDetailErrorState(
                            error = uiState.error!!,
                            onRetry = { viewModel.loadReservationDetail(reservationId) }
                        )
                    }

                    uiState.reservation != null -> {
                        ClarityReservationDetailContent(
                            reservation = uiState.reservation!!,
                            onCancelReservation = { showCancelDialog = true }
                        )
                    }
                }
            }
        }

        // Cancel confirmation dialog
        if (showCancelDialog) {
            ClarityCancelReservationDialog(
                isLoading = uiState.isCancelling,
                error = uiState.cancellationError,
                onDismiss = {
                    showCancelDialog = false
                    viewModel.clearError()
                },
                onConfirm = {
                    viewModel.cancelReservation(reservationId)
                }
            )
        }
    }
}

@Composable
private fun ClarityDetailHeader(
    title: String,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ClaritySpacing.md,
                vertical = ClaritySpacing.xl
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = ClarityDarkGray,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(ClaritySpacing.sm))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = ClarityDarkGray
        )
    }
}

@Composable
private fun ClarityDetailLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = ClarityAccentBlue,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            
            Text(
                text = "Loading reservation details...",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
        }
    }
}

@Composable
private fun ClarityDetailErrorState(
    error: String,
    onRetry: () -> Unit
) {
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
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.sm))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.lg))
            
            ClarityPrimaryButton(
                text = "Try Again",
                onClick = onRetry
            )
        }
    }
}

@Composable
private fun ClarityReservationDetailContent(
    reservation: Reservation,
    onCancelReservation: () -> Unit
) {
    val startDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(reservation.startTime),
        ZoneId.systemDefault()
    )
    val endDateTime = startDateTime.plusMinutes(reservation.durationMinutes.toLong())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.md)
    ) {
        // Hero Status Card with modern gradient
        ModernStatusHeroCard(
            reservation = reservation,
            startDateTime = startDateTime,
            endDateTime = endDateTime
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.lg))

        // Time Slot Visualization
        ModernTimeSlotCard(
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            durationMinutes = reservation.durationMinutes,
            status = reservation.status
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.lg))

        // Booking Information Section
        ClaritySectionHeader(text = "Booking Information")

        ClarityCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
            ) {
                ModernInfoRow(
                    icon = Icons.Default.EvStation,
                    label = "Station",
                    value = reservation.stationName
                )

                ModernInfoRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Date",
                    value = startDateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
                )

                ModernInfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Time Slot",
                    value = "${startDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${endDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                )

                ModernInfoRow(
                    icon = Icons.Default.Timer,
                    label = "Duration",
                    value = "${reservation.durationMinutes} minutes"
                )

                ModernInfoRow(
                    icon = Icons.Default.Tag,
                    label = "Booking ID",
                    value = "#${reservation.id.takeLast(8).uppercase()}"
                )
            }
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.lg))

        // QR Code Section (only for active bookings)
        if (reservation.status.lowercase() == "confirmed" || reservation.status.lowercase() == "in_progress") {
            ModernQRCodeCard(reservationId = reservation.id)
            Spacer(modifier = Modifier.height(ClaritySpacing.lg))
        }

        // Action Buttons
        if (reservation.status.lowercase() == "confirmed") {
            ModernActionButtons(
                onGetDirections = { /* Open maps */ },
                onCancelReservation = onCancelReservation
            )
        }

        // Bottom spacing
        Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
    }
}

@Composable
private fun ModernStatusHeroCard(
    reservation: Reservation,
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime
) {
    ClarityCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelLarge,
                    color = ClarityMediumGray
                )

                ClarityStatusChip(
                    text = when (reservation.status.lowercase()) {
                        "confirmed" -> "Confirmed"
                        "in_progress" -> "Active"
                        "completed" -> "Completed"
                        "cancelled" -> "Cancelled"
                        else -> "Pending"
                    },
                    status = when (reservation.status.lowercase()) {
                        "confirmed" -> ClarityStatus.Success
                        "in_progress" -> ClarityStatus.Warning
                        "completed" -> ClarityStatus.Success
                        "cancelled" -> ClarityStatus.Error
                        else -> ClarityStatus.Neutral
                    }
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            // Large Station Name
            Text(
                text = reservation.stationName,
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.xs))

            // Date display
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = ClarityAccentBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(ClaritySpacing.xs))
                Text(
                    text = startDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClarityDarkGray
                )
            }
        }
    }
}

@Composable
private fun ModernTimeSlotCard(
    startDateTime: LocalDateTime,
    endDateTime: LocalDateTime,
    durationMinutes: Int,
    status: String
) {
    val isActive = status.lowercase() == "confirmed" || status.lowercase() == "in_progress"

    ClarityCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = if (isActive) ClarityAccentBlue else ClarityMediumGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                Text(
                    text = "Charging Time Slot",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClarityDarkGray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            // Time Range Display with visual timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start Time
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = startDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isActive) ClarityAccentBlue else ClarityDarkGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClarityMediumGray
                    )
                }

                // Duration indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(4.dp)
                            .background(
                                color = if (isActive) ClarityAccentBlue else ClarityLightGray,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(ClaritySpacing.xs))
                    Text(
                        text = "$durationMinutes min",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) ClarityAccentBlue else ClarityMediumGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // End Time
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = endDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isActive) ClarityAccentBlue else ClarityDarkGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "End",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClarityMediumGray
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernInfoRow(
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
private fun ModernQRCodeCard(reservationId: String) {
    ClarityCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = ClarityAccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                Text(
                    text = "Station Check-in",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClarityDarkGray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            // QR Code
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(ClaritySpacing.md))
                    .background(ClarityPureWhite)
                    .padding(ClaritySpacing.sm)
            ) {
                QRCodeGenerator(
                    content = "reservation:$reservationId",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.md))

            Text(
                text = "Present this QR code to the station operator",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.xs))

            Text(
                text = "to start your charging session",
                style = MaterialTheme.typography.bodySmall,
                color = ClarityMediumGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernActionButtons(
    onGetDirections: () -> Unit,
    onCancelReservation: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
    ) {
        ClarityPrimaryButton(
            text = "Get Directions",
            onClick = onGetDirections,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Directions
        )

        ClaritySecondaryButton(
            text = "Cancel Booking",
            onClick = onCancelReservation,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ClarityCancelReservationDialog(
    isLoading: Boolean = false,
    error: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ClarityModal(
        onDismiss = if (isLoading) { {} } else onDismiss,
        modifier = Modifier.padding(ClaritySpacing.lg)
    ) {
        Column(
            modifier = Modifier.padding(ClaritySpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Warning Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ClarityErrorRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ClarityErrorRed,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.md))

            Text(
                text = "Cancel Booking?",
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.sm))

            Text(
                text = "Are you sure you want to cancel this booking? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                textAlign = TextAlign.Center
            )

            // Show error if present
            if (error != null) {
                Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(ClaritySpacing.xs))
                        .background(ClarityErrorRed.copy(alpha = 0.1f))
                        .padding(ClaritySpacing.sm)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ClarityErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(ClaritySpacing.xs))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = ClarityErrorRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
            ) {
                ClarityTextButton(
                    text = "Keep Booking",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )

                ClarityPrimaryButton(
                    text = if (isLoading) "Cancelling..." else "Yes, Cancel",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
            }
        }
    }
}