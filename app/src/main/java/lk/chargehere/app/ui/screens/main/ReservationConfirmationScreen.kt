package lk.chargehere.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import lk.chargehere.app.ui.viewmodel.ReservationDetailViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationConfirmationScreen(
    reservationId: String,
    onNavigateToHome: () -> Unit,
    onNavigateToReservations: () -> Unit,
    viewModel: ReservationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(reservationId) {
        viewModel.loadReservationDetail(reservationId)
    }

    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    Box(modifier = Modifier.fillMaxSize()) {
        ClarityBackground {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadReservationDetail(reservationId) },
                        onGoBack = onNavigateToHome
                    )
                }
                uiState.reservation != null -> {
                    SuccessContent(
                        reservation = uiState.reservation!!,
                        onNavigateToHome = onNavigateToHome,
                        onNavigateToReservations = onNavigateToReservations
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = ClarityAccentBlue,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            Text(
                text = "Loading booking details...",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onGoBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(ClaritySpacing.lg),
        contentAlignment = Alignment.Center
    ) {
        ClarityCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = ClarityErrorRed,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(ClaritySpacing.md))

                Text(
                    text = "Failed to Load Booking",
                    style = MaterialTheme.typography.titleLarge,
                    color = ClarityDarkGray,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                ClarityFormattedError(
                    errorMessage = error,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
                ) {
                    ClaritySecondaryButton(
                        text = "Go Back",
                        onClick = onGoBack,
                        modifier = Modifier.weight(1f)
                    )
                    ClarityPrimaryButton(
                        text = "Retry",
                        onClick = onRetry,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    reservation: lk.chargehere.app.domain.model.Reservation,
    onNavigateToHome: () -> Unit,
    onNavigateToReservations: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))

        // Success Icon with animated background
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(ClaritySuccessGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = ClaritySuccessGreen,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.xl))

        // Success Message
        Text(
            text = "Booking Confirmed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ClarityDarkGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.sm))

        Text(
            text = "Your charging session has been successfully reserved.",
            style = MaterialTheme.typography.bodyLarge,
            color = ClarityMediumGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.xl))

        // Booking Details Card
        ClarityCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
            ) {
                Text(
                    text = "Booking Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ClarityDarkGray
                )

                HorizontalDivider(
                    color = ClarityLightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = ClaritySpacing.xs)
                )

                // Booking Number
                ConfirmationDetailRow(
                    icon = Icons.Default.ConfirmationNumber,
                    label = "Booking Number",
                    value = reservation.bookingNumber ?: "#${reservation.id.takeLast(8).uppercase()}"
                )

                // Station Name
                ConfirmationDetailRow(
                    icon = Icons.Default.ElectricCar,
                    label = "Charging Station",
                    value = reservation.stationName.ifEmpty { "Station ${reservation.stationId.takeLast(6)}" }
                )

                // Reservation Date & Time
                ConfirmationDetailRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Reservation Date & Time",
                    value = formatDateTime(reservation.startTime)
                )

                // Duration
                ConfirmationDetailRow(
                    icon = Icons.Default.Schedule,
                    label = "Duration",
                    value = "${reservation.durationMinutes} minutes"
                )

                // Status
                ConfirmationDetailRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Status",
                    value = reservation.status.replaceFirstChar { it.uppercase() },
                    valueColor = when (reservation.status.lowercase()) {
                        "pending" -> ClarityWarningOrange
                        "confirmed", "approved" -> ClaritySuccessGreen
                        "cancelled" -> ClarityErrorRed
                        else -> ClarityMediumGray
                    }
                )

                // QR Code info (if available)
                if (!reservation.qrPayload.isNullOrEmpty()) {
                    HorizontalDivider(
                        color = ClarityLightGray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = ClaritySpacing.xs)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(ClaritySpacing.sm))
                            .background(ClarityAccentBlue.copy(alpha = 0.1f))
                            .padding(ClaritySpacing.md)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = ClarityAccentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "QR Code Available",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ClarityAccentBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "View in reservations to access your QR code",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClarityAccentBlue.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Success message box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(ClaritySpacing.sm))
                        .background(ClaritySuccessGreen.copy(alpha = 0.1f))
                        .padding(ClaritySpacing.md)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ClaritySuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Please arrive on time for your charging session",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClaritySuccessGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.xl))

        // Next Steps Section
        ClarityCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
            ) {
                Text(
                    text = "What's Next?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ClarityDarkGray
                )

                NextStepItem(
                    number = "1",
                    title = "Check Your Bookings",
                    description = "View your reservation details and QR code in My Reservations"
                )

                NextStepItem(
                    number = "2",
                    title = "Arrive on Time",
                    description = "Make sure to arrive at ${formatTime(reservation.startTime)}"
                )

                NextStepItem(
                    number = "3",
                    title = "Show QR Code",
                    description = "Present your QR code to the operator to start charging"
                )
            }
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.xl))

        // Action Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(ClaritySpacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            ClarityPrimaryButton(
                text = "View My Reservations",
                onClick = onNavigateToReservations,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.EventAvailable
            )

            ClaritySecondaryButton(
                text = "Back to Home",
                onClick = onNavigateToHome,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
    }
}

@Composable
private fun ConfirmationDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = ClarityDarkGray
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
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
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NextStepItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
    ) {
        // Number Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ClarityAccentBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                color = ClarityPureWhite,
                fontWeight = FontWeight.Bold
            )
        }

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = ClarityDarkGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ClarityMediumGray
            )
        }
    }
}

// Helper functions for date/time formatting
private fun formatDateTime(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a")
    return instant.atZone(ZoneId.systemDefault()).format(formatter)
}

private fun formatTime(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return instant.atZone(ZoneId.systemDefault()).format(formatter)
}
