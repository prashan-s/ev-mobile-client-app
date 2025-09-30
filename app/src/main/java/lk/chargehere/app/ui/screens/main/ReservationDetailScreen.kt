package lk.chargehere.app.ui.screens.main

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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

    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    ClarityBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Clean Header Bar
            ClarityDetailHeader(
                title = "Reservation Details",
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
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                viewModel.cancelReservation(reservationId)
                showCancelDialog = false
            }
        )
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.md)
    ) {
        // Status and QR Code Section
        ClarityCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Booking Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = ClarityMediumGray
                    )
                    
                    ClarityStatusChip(
                        text = when (reservation.status) {
                            "confirmed" -> "Confirmed"
                            "in_progress" -> "Active"
                            "completed" -> "Completed"
                            "cancelled" -> "Cancelled"
                            else -> "Pending"
                        },
                        status = when (reservation.status) {
                            "confirmed" -> ClarityStatus.Success
                            "in_progress" -> ClarityStatus.Warning
                            "completed" -> ClarityStatus.Success
                            "cancelled" -> ClarityStatus.Error
                            else -> ClarityStatus.Neutral
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(ClaritySpacing.lg))
                
                // QR Code
                if (reservation.status == "confirmed" || reservation.status == "in_progress") {
                    QRCodeGenerator(
                        content = "reservation:${reservation.id}",
                        modifier = Modifier.size(200.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(ClaritySpacing.md))
                    
                    Text(
                        text = "Show this QR code to the station operator",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClarityMediumGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(ClaritySpacing.lg))
        
        // Reservation Details Section
        ClaritySectionHeader(text = "Reservation Details")
        
        ClarityCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
            ) {
                ClarityLabelDataPair(
                    label = "Reservation ID",
                    data = reservation.id
                )
                
                ClarityLabelDataPair(
                    label = "Station",
                    data = reservation.stationName
                )
                
                ClarityLabelDataPair(
                    label = "Date & Time",
                    data = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(reservation.startTime), 
                        ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm"))
                )
                
                ClarityLabelDataPair(
                    label = "Duration",
                    data = "${reservation.durationMinutes} minutes"
                )
                
                ClarityLabelDataPair(
                    label = "Created",
                    data = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(reservation.createdAt), 
                        ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(ClaritySpacing.lg))
        
        // Station Information Section
        ClaritySectionHeader(text = "Station Information")
        
        ClarityCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = ClarityMediumGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                    Column {
                        Text(
                            text = "Max Power",
                            style = MaterialTheme.typography.labelMedium,
                            color = ClarityMediumGray
                        )
                        Text(
                            text = "150 kW", // Get from station data
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClarityDarkGray
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ClarityMediumGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                    Column {
                        Text(
                            text = "Address",
                            style = MaterialTheme.typography.labelMedium,
                            color = ClarityMediumGray
                        )
                        Text(
                            text = "123 Main Street, Downtown", // Get from station data
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClarityDarkGray
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(ClaritySpacing.sectionSpacing))
        
        // Action Buttons
        if (reservation.status == "confirmed") {
            Column(
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
            ) {
                ClarityPrimaryButton(
                    text = "Get Directions",
                    onClick = { /* Open maps */ },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Directions
                )
                
                ClaritySecondaryButton(
                    text = "Cancel Reservation",
                    onClick = onCancelReservation,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
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
                textAlign = TextAlign.Center
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
                    text = "Cancel Reservation",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}