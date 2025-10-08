package lk.chargehere.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import lk.chargehere.app.ui.utils.keyboardAwareScrollPadding
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
            val bookingId = uiState.bookingResponse!!.id ?: "unknown"
            onNavigateToConfirmation(bookingId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ClarityBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .keyboardAwareScrollPadding()
            ) {
                // Header
                ClarityDetailHeader(
                    title = "Create Booking",
                    subtitle = "Step ${uiState.currentStep} of ${uiState.totalSteps}",
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
                        Column(modifier = Modifier.weight(1f)) {
                            // Step indicator
                            ClarityStepIndicator(
                                currentStep = uiState.currentStep,
                                totalSteps = uiState.totalSteps,
                                modifier = Modifier.padding(vertical = ClaritySpacing.lg)
                            )

                            // Step content
                            when (uiState.currentStep) {
                                1 -> DateSelectionStep(
                                    selectedDateMillis = uiState.selectedDateMillis,
                                    onDateSelected = { viewModel.selectDate(it) },
                                    validationError = uiState.validationError
                                )
                                2 -> TimeSelectionStep(
                                    selectedHour = uiState.selectedTimeHour,
                                    selectedMinute = uiState.selectedTimeMinute,
                                    onTimeSelected = { hour, minute -> viewModel.selectTime(hour, minute) },
                                    operatingHours = uiState.station!!.operatingHours
                                )
                                3 -> DurationSelectionStep(
                                    selectedDurationMinutes = uiState.selectedDurationMinutes,
                                    onDurationSelected = { viewModel.selectDuration(it) },
                                    estimatedCost = uiState.estimatedCost,
                                    stationName = uiState.station!!.name
                                )
                                4 -> SlotSelectionStep(
                                    totalSlots = uiState.station!!.maxPower.toInt().coerceAtLeast(4),
                                    availableSlots = 6, // TODO: Get from API
                                    selectedSlot = uiState.selectedPhysicalSlot,
                                    onSlotSelected = { viewModel.selectPhysicalSlot(it) }
                                )
                            }
                        }

                        // Navigation buttons
                        BottomNavigationButtons(
                            currentStep = uiState.currentStep,
                            totalSteps = uiState.totalSteps,
                            canProceed = viewModel.canProceedToNextStep(),
                            onPrevious = { viewModel.previousStep() },
                            onNext = {
                                if (uiState.currentStep < uiState.totalSteps) {
                                    viewModel.nextStep()
                                } else {
                                    viewModel.createBookingWithAllDetails(stationId)
                                }
                            }
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
    subtitle: String,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClarityPureWhite)
            .padding(horizontal = ClaritySpacing.md, vertical = ClaritySpacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ClarityDarkGray
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ClarityDarkGray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClarityMediumGray
                )
            }
        }
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

// STEP 1: Date Selection
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectionStep(
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    validationError: String?
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.md)
    ) {
        ClaritySectionHeader(text = "Select Date")

        Text(
            text = "Choose a date for your charging session (up to 7 days in advance)",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityMediumGray,
            modifier = Modifier.padding(bottom = ClaritySpacing.md)
        )

        // Selected date display
        ClarityCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Selected Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = ClarityMediumGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedDateMillis != null) {
                            Instant.ofEpochMilli(selectedDateMillis)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
                        } else {
                            "Tap to select date"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = ClarityDarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = ClarityAccentBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Validation error
        if (validationError != null) {
            Spacer(modifier = Modifier.height(ClaritySpacing.sm))
            ClarityFormattedError(
                errorMessage = validationError,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.md))

        // Quick date selection chips
        Text(
            text = "Quick Select",
            style = MaterialTheme.typography.labelLarge,
            color = ClarityDarkGray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(ClaritySpacing.sm))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickDateOption("Today", 0, selectedDateMillis, onDateSelected)
            QuickDateOption("Tomorrow", 1, selectedDateMillis, onDateSelected)
            QuickDateOption("In 2 days", 2, selectedDateMillis, onDateSelected)
            QuickDateOption("In 3 days", 3, selectedDateMillis, onDateSelected)
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun QuickDateOption(
    label: String,
    daysOffset: Int,
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit
) {
    val dateMillis = System.currentTimeMillis() + (daysOffset * 24 * 60 * 60 * 1000L)
    val isSelected = selectedDateMillis?.let {
        val selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        val optionDate = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        selectedDate == optionDate
    } ?: false

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ClarityAccentBlue else ClarityPureWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) ClarityAccentBlue else ClarityLightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onDateSelected(dateMillis) }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) ClarityPureWhite else ClarityDarkGray,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = Instant.ofEpochMilli(dateMillis)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM dd")),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) ClarityPureWhite.copy(alpha = 0.9f) else ClarityMediumGray
            )
        }
    }
}

// STEP 2: Time Selection
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSelectionStep(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    operatingHours: List<lk.chargehere.app.domain.model.OperatingHour>?
) {
    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute,
        is24Hour = true
    )
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.md)
    ) {
        ClaritySectionHeader(text = "Select Time")

        Text(
            text = "Choose a time for your charging session",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityMediumGray,
            modifier = Modifier.padding(bottom = ClaritySpacing.md)
        )

        // Selected time display
        ClarityCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Selected Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = ClarityMediumGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.headlineMedium,
                        color = ClarityDarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = ClarityAccentBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.md))

        // Operating hours info
        if (operatingHours != null && operatingHours.isNotEmpty()) {
            ClarityCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ClarityAccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Station Operating Hours",
                            style = MaterialTheme.typography.titleSmall,
                            color = ClarityDarkGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    operatingHours.take(3).forEach { hours ->
                        Text(
                            text = "${hours.dayOfWeek.capitalize()}: ${hours.startTime} - ${hours.endTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hours.isOpen) ClaritySuccessGreen else ClarityErrorRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.md))

        // Quick time selection
        Text(
            text = "Quick Select",
            style = MaterialTheme.typography.labelLarge,
            color = ClarityDarkGray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(ClaritySpacing.sm))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "Morning (09:00)" to Pair(9, 0),
                "Noon (12:00)" to Pair(12, 0),
                "Afternoon (15:00)" to Pair(15, 0),
                "Evening (18:00)" to Pair(18, 0)
            ).forEach { (label, time) ->
                QuickTimeOption(label, time, selectedHour, selectedMinute, onTimeSelected)
            }
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuickTimeOption(
    label: String,
    time: Pair<Int, Int>,
    selectedHour: Int,
    selectedMinute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    val isSelected = selectedHour == time.first && selectedMinute == time.second

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ClarityAccentBlue else ClarityPureWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) ClarityAccentBlue else ClarityLightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onTimeSelected(time.first, time.second) }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) ClarityPureWhite else ClarityDarkGray,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// STEP 3: Duration Selection
@Composable
private fun DurationSelectionStep(
    selectedDurationMinutes: Int,
    onDurationSelected: (Int) -> Unit,
    estimatedCost: Double,
    stationName: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.md)
    ) {
        ClaritySectionHeader(text = "Select Duration")

        Text(
            text = "How long do you need to charge?",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityMediumGray,
            modifier = Modifier.padding(bottom = ClaritySpacing.md)
        )

        ClarityDurationSelector(
            selectedDurationMinutes = selectedDurationMinutes,
            onDurationSelected = onDurationSelected,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.lg))

        // Estimated cost card
        ClarityCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estimated Cost",
                            style = MaterialTheme.typography.labelMedium,
                            color = ClarityMediumGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rs. ${String.format("%.2f", estimatedCost)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ClarityAccentBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = ClarityAccentBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ClarityLightGray)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Duration:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClarityMediumGray
                    )
                    Text(
                        text = "$selectedDurationMinutes minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClarityDarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Station:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClarityMediumGray
                    )
                    Text(
                        text = stationName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClarityDarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// STEP 4: Slot Selection
@Composable
private fun SlotSelectionStep(
    totalSlots: Int,
    availableSlots: Int,
    selectedSlot: Int,
    onSlotSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.md)
    ) {
        ClaritySectionHeader(text = "Select Charging Slot")

        Text(
            text = "Choose which charging slot you'd like to use",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityMediumGray,
            modifier = Modifier.padding(bottom = ClaritySpacing.md)
        )

        ClaritySlotSelector(
            totalSlots = totalSlots,
            availableSlots = availableSlots,
            selectedSlot = selectedSlot,
            onSlotSelected = onSlotSelected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Bottom Navigation Buttons
@Composable
private fun BottomNavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClarityPureWhite)
            .padding(ClaritySpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
        ) {
            if (currentStep > 1) {
                ClaritySecondaryButton(
                    text = "Previous",
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f)
                )
            }

            ClarityPrimaryButton(
                text = if (currentStep < totalSteps) "Next" else "Confirm Booking",
                onClick = onNext,
                modifier = Modifier.weight(if (currentStep > 1) 1f else 2f),
                enabled = canProceed,
                icon = if (currentStep < totalSteps) Icons.Default.ArrowForward else Icons.Default.CheckCircle
            )
        }
    }
}
