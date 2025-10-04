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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.domain.model.Station
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import lk.chargehere.app.ui.viewmodel.StationDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    stationId: String,
    onNavigateToReservation: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: StationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(stationId) {
        viewModel.loadStationDetails(stationId)
    }

    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    Box(modifier = Modifier.fillMaxSize()) {
        ClarityBackground {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Clean Header Bar
                ClarityDetailHeader(
                    title = "Station Details",
                    onNavigateBack = onNavigateBack
                )

                when {
                    uiState.isLoading -> {
                        ClarityLoadingState()
                    }

                    uiState.error != null -> {
                        ClarityErrorState(
                            error = uiState.error!!,
                            onRetry = { viewModel.loadStationDetails(stationId) }
                        )
                    }

                    uiState.station != null -> {
                        ClarityStationDetailContent(
                            station = uiState.station!!,
                            onNavigateToReservation = onNavigateToReservation
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
                text = "Loading station details...",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
        }
    }
}

@Composable
private fun ClarityErrorState(
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

            ClarityFormattedError(
                errorMessage = error,
                modifier = Modifier.fillMaxWidth()
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
private fun ClarityStationDetailContent(
    station: Station,
    onNavigateToReservation: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ClaritySpacing.md)
    ) {
        // Hero Station Card
        ModernStationHeroCard(station = station)

        Spacer(modifier = Modifier.height(ClaritySpacing.lg))

        // Station Features Section
        ClaritySectionHeader(text = "Station Features")

        ClarityCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
            ) {
                ModernFeatureRow(
                    icon = Icons.Default.ElectricBolt,
                    label = "Max Power",
                    value = "${station.maxPower} kW",
                    highlighted = true
                )

                ModernFeatureRow(
                    icon = Icons.Default.BatteryChargingFull,
                    label = "Charger Type",
                    value = station.chargerType
                )

                ModernFeatureRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Availability",
                    value = if (station.isAvailable) "Available Now" else "Not Available",
                    valueColor = if (station.isAvailable) ClaritySuccessGreen else ClarityErrorRed
                )

                ModernFeatureRow(
                    icon = Icons.Default.CalendarMonth,
                    label = "Reservations",
                    value = if (station.isReservable) "Enabled" else "Disabled",
                    valueColor = if (station.isReservable) ClaritySuccessGreen else ClarityMediumGray
                )
            }
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.lg))

        // Location Section
        if (station.address != null) {
            ClaritySectionHeader(text = "Location")

            ClarityCard {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ClarityAccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Address",
                            style = MaterialTheme.typography.labelMedium,
                            color = ClarityMediumGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = station.address,
                            style = MaterialTheme.typography.bodyLarge,
                            color = ClarityDarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))
        }

        // Action Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
        ) {
            ClarityPrimaryButton(
                text = "Make Reservation",
                onClick = { onNavigateToReservation(station.id) },
                modifier = Modifier.fillMaxWidth(),
                enabled = station.isReservable && station.isAvailable,
                icon = Icons.Default.EventAvailable
            )

            if (!station.isAvailable || !station.isReservable) {
                Text(
                    text = when {
                        !station.isAvailable -> "This station is currently unavailable"
                        !station.isReservable -> "Reservations are not enabled for this station"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ClarityMediumGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Bottom spacing
        Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
    }
}

@Composable
private fun ModernStationHeroCard(station: Station) {
    ClarityCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Station Name
            Text(
                text = station.name,
                style = MaterialTheme.typography.headlineMedium,
                color = ClarityDarkGray,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.md))

            // Status Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
            ) {
                ClarityStatusChip(
                    text = if (station.isAvailable) "Available" else "Unavailable",
                    status = if (station.isAvailable) ClarityStatus.Success else ClarityStatus.Error
                )

                if (station.isReservable) {
                    ClarityStatusChip(
                        text = "Reservable",
                        status = ClarityStatus.Success
                    )
                }
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickStatItem(
                    icon = Icons.Default.ElectricBolt,
                    value = "${station.maxPower} kW",
                    label = "Max Power"
                )
                QuickStatItem(
                    icon = Icons.Default.BatteryChargingFull,
                    value = station.chargerType,
                    label = "Charger"
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ClarityAccentBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ClarityAccentBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = ClarityDarkGray,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ClarityMediumGray
            )
        }
    }
}

@Composable
private fun ModernFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    highlighted: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = ClarityDarkGray
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlighted) ClarityAccentBlue else ClarityMediumGray,
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
                fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
