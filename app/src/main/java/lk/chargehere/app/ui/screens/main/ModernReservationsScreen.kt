package lk.chargehere.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.domain.model.Reservation
import lk.chargehere.app.ui.viewmodel.ReservationsViewModel
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernReservationsScreen(
    onNavigateToReservationDetail: (String) -> Unit,
    viewModel: ReservationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Past")

    LaunchedEffect(Unit) {
        viewModel.loadReservations()
    }

    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    ClarityBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top spacing for status bar
            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))

            // Header with clean typography
            Column(
                modifier = Modifier.padding(horizontal = ClaritySpacing.md)
            ) {
                Text(
                    text = "My Reservations",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ClarityDarkGray
                )
                
                Spacer(modifier = Modifier.height(ClaritySpacing.xs))
                
                Text(
                    text = "Manage your charging station bookings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClarityMediumGray
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            // Clean Tab Row
            ClarityTabRow(
                selectedTabIndex = selectedTab,
                tabs = tabs,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = ClaritySpacing.md)
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.md))

            // Content based on selected tab
            when (selectedTab) {
                0 -> ClarityUpcomingReservations(
                    reservations = uiState.upcomingReservations,
                    isLoading = uiState.isLoading,
                    onReservationClick = onNavigateToReservationDetail,
                    onCancelReservation = { viewModel.cancelReservation(it) }
                )
                1 -> ClarityPastReservations(
                    reservations = uiState.pastReservations,
                    isLoading = uiState.isLoading,
                    onReservationClick = onNavigateToReservationDetail
                )
            }
        }
    }
}

@Composable
private fun ClarityTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = ClarityPureWhite,
        contentColor = ClarityAccentBlue,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.wrapContentWidth(Alignment.Start),
                color = ClarityAccentBlue,
                height = 2.dp
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedTabIndex == index) ClarityAccentBlue else ClarityMediumGray
                    )
                }
            )
        }
    }
}

@Composable
private fun ClarityUpcomingReservations(
    reservations: List<Reservation>,
    isLoading: Boolean,
    onReservationClick: (String) -> Unit,
    onCancelReservation: (String) -> Unit
) {
    when {
        isLoading -> {
            ClarityLoadingState()
        }
        
        reservations.isEmpty() -> {
            ClarityEmptyReservationsState(
                title = "No Upcoming Reservations",
                message = "Your future charging station bookings will appear here"
            )
        }
        
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = ClaritySpacing.md,
                    vertical = ClaritySpacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
            ) {
                items(reservations) { reservation ->
                    ClarityUpcomingReservationItem(
                        reservation = reservation,
                        onClick = { onReservationClick(reservation.id) },
                        onCancel = { onCancelReservation(reservation.id) }
                    )
                }
                
                // Bottom padding for floating nav
                item {
                    Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
                }
            }
        }
    }
}

@Composable
private fun ClarityPastReservations(
    reservations: List<Reservation>,
    isLoading: Boolean,
    onReservationClick: (String) -> Unit
) {
    when {
        isLoading -> {
            ClarityLoadingState()
        }
        
        reservations.isEmpty() -> {
            ClarityEmptyReservationsState(
                title = "No Past Reservations",
                message = "Your charging history will appear here after completing sessions"
            )
        }
        
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = ClaritySpacing.md,
                    vertical = ClaritySpacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
            ) {
                items(reservations) { reservation ->
                    ClarityPastReservationItem(
                        reservation = reservation,
                        onClick = { onReservationClick(reservation.id) }
                    )
                }
                
                // Bottom padding for floating nav
                item {
                    Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
                }
            }
        }
    }
}

@Composable
private fun ClarityLoadingState() {
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
                text = "Loading reservations...",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
        }
    }
}

@Composable
private fun ClarityEmptyReservationsState(
    title: String,
    message: String
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
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                tint = ClarityMediumGray,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.sm))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ClarityUpcomingReservationItem(
    reservation: Reservation,
    onClick: () -> Unit,
    onCancel: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    
    ClarityCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reservation #${reservation.id.takeLast(6)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ClarityMediumGray
                )
                
                ClarityStatusChip(
                    text = when (reservation.status) {
                        "confirmed" -> "Confirmed"
                        "in_progress" -> "Active"
                        else -> "Pending"
                    },
                    status = when (reservation.status) {
                        "confirmed" -> ClarityStatus.Success
                        "in_progress" -> ClarityStatus.Warning
                        else -> ClarityStatus.Neutral
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(ClaritySpacing.sm))
            
            // Station name and time
            Text(
                text = reservation.stationName,
                style = MaterialTheme.typography.titleMedium,
                color = ClarityDarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.xs))
            
            Text(
                text = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(reservation.startTime), 
                    ZoneId.systemDefault()
                ).format(DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")),
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
            
            Text(
                text = "${reservation.durationMinutes} minutes",
                style = MaterialTheme.typography.bodySmall,
                color = ClarityMediumGray
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
            ) {
                ClaritySecondaryButton(
                    text = "Cancel",
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.weight(1f)
                )
                
                ClarityPrimaryButton(
                    text = "View Details",
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    
    // Cancel confirmation dialog
    if (showCancelDialog) {
        ClarityCancelReservationDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                onCancel()
                showCancelDialog = false
            }
        )
    }
}

@Composable
private fun ClarityPastReservationItem(
    reservation: Reservation,
    onClick: () -> Unit
) {
    ClarityCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (reservation.status) {
                            "completed" -> ClaritySuccessGreen
                            "cancelled" -> ClarityErrorRed
                            else -> ClarityMediumGray
                        },
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(ClaritySpacing.sm))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reservation.stationName,
                    style = MaterialTheme.typography.titleSmall,
                    color = ClarityDarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(reservation.startTime), 
                        ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = ClarityMediumGray
                )
            }
            
            Text(
                text = when (reservation.status) {
                    "completed" -> "Completed"
                    "cancelled" -> "Cancelled"
                    else -> "Ended"
                },
                style = MaterialTheme.typography.labelSmall,
                color = ClarityMediumGray
            )
            
            Spacer(modifier = Modifier.width(ClaritySpacing.sm))
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = ClarityMediumGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ClarityCancelReservationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ClarityModal(
        onDismiss = onDismiss,
        modifier = Modifier.padding(ClaritySpacing.lg)
    ) {
        Column(
            modifier = Modifier.padding(ClaritySpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cancel Reservation",
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            
            Text(
                text = "Are you sure you want to cancel this reservation? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.lg))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
            ) {
                ClarityTextButton(
                    text = "Keep",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                
                ClarityPrimaryButton(
                    text = "Cancel",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}