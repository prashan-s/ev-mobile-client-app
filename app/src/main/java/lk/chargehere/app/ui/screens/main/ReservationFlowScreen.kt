package lk.chargehere.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import lk.chargehere.app.ui.viewmodel.ReservationViewModel
import lk.chargehere.app.ui.viewmodel.TimeSlot
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFlowScreen(
    stationId: String,
    onNavigateToConfirmation: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ReservationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(stationId) {
        viewModel.loadStation(stationId)
    }
    
    LaunchedEffect(uiState.isBookingSuccessful) {
        if (uiState.isBookingSuccessful && uiState.bookingResponse != null) {
            // Navigate to confirmation with booking ID
            val bookingId = uiState.bookingResponse!!.id
                ?: uiState.bookingResponse!!.id
                ?: "unknown"
            onNavigateToConfirmation(bookingId)
        }
    }
    
    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    Box(modifier = Modifier.fillMaxSize()) {
        ClarityBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                // Clean Header Bar
                ClarityDetailHeader(
                    title = "Create Booking",
                    onNavigateBack = onNavigateBack
                )

                when {
                    uiState.isLoading -> {
                        ModernLoadingState()
                    }
                    uiState.error != null -> {
                        ModernErrorState(
                            error = uiState.error!!,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                    uiState.station != null -> {
                        ModernBookingContent(
                            station = uiState.station!!,
                            availableTimeSlots = uiState.availableTimeSlots,
                            selectedTimeSlot = uiState.selectedTimeSlot,
                            onTimeSlotSelected = { viewModel.selectTimeSlot(it) },
                            onConfirmBooking = { viewModel.createBookingWithSelectedSlot(stationId) }
                        )
                    }
                }
            }
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
            .background(ClarityPureWhite)
            .padding(horizontal = ClaritySpacing.md, vertical = ClaritySpacing.md)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = ClarityDarkGray
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = ClarityDarkGray,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ModernLoadingState() {
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
                text = "Loading station details...",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
        }
    }
}

@Composable
private fun ModernErrorState(
    error: String,
    onDismiss: () -> Unit
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

                ClarityFormattedError(
                    errorMessage = error,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.lg))
                ClarityPrimaryButton(
                    text = "OK",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ModernBookingContent(
    station: lk.chargehere.app.domain.model.Station,
    availableTimeSlots: List<TimeSlot>,
    selectedTimeSlot: TimeSlot?,
    onTimeSlotSelected: (TimeSlot) -> Unit,
    onConfirmBooking: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.md)
    ) {
        // Station Info Card
        ClaritySectionHeader(text = "Station Details")
        ClarityCard {
            Column {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = ClarityDarkGray,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ClarityMediumGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(ClaritySpacing.xs))
                    Text(
                        text = station.address ?: "Address not available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClarityMediumGray
                    )
                }

                Spacer(modifier = Modifier.height(ClaritySpacing.xs))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = ClarityAccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(ClaritySpacing.xs))
                    Text(
                        text = "${station.maxPower} kW • ${station.chargerType}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClarityDarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.lg))

        // Time Slot Selection
        ClaritySectionHeader(text = "Select Time Slot")

        Text(
            text = "Choose when you'd like to start charging",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityMediumGray,
            modifier = Modifier.padding(bottom = ClaritySpacing.sm)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm),
            contentPadding = PaddingValues(vertical = ClaritySpacing.xs)
        ) {
            items(availableTimeSlots) { timeSlot ->
                ModernTimeSlotCard(
                    timeSlot = timeSlot,
                    isSelected = selectedTimeSlot?.id == timeSlot.id,
                    onClick = { onTimeSlotSelected(timeSlot) }
                )
            }
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.lg))

        // Selected Time Summary
        if (selectedTimeSlot != null) {
            ClarityCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = ClarityAccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Selected Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = ClarityMediumGray
                        )
                        Text(
                            text = if (selectedTimeSlot.id == "now") "Now" else selectedTimeSlot.displayTime,
                            style = MaterialTheme.typography.titleMedium,
                            color = ClarityDarkGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ClaritySuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(ClaritySpacing.lg))
        }

        // Confirm Button
        ClarityPrimaryButton(
            text = "Confirm Booking",
            onClick = onConfirmBooking,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedTimeSlot != null
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
    }
}

@Composable
private fun ModernTimeSlotCard(
    timeSlot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) ClarityAccentBlue else ClarityPureWhite
    val textColor = if (isSelected) ClarityPureWhite else ClarityDarkGray
    val borderColor = if (isSelected) ClarityAccentBlue else ClarityLightGray

    Box(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(ClaritySpacing.md))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(ClaritySpacing.md)
            )
            .clickable(onClick = onClick)
            .padding(ClaritySpacing.md),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (timeSlot.id == "now") Icons.Default.FlashOn else Icons.Default.Schedule,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(ClaritySpacing.xs))
            Text(
                text = timeSlot.displayTime,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (timeSlot.id != "now") {
                Text(
                    text = "in ${timeSlot.offsetMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}