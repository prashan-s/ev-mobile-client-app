package lk.chargehere.app.ui.screens.main

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import lk.chargehere.app.ui.components.ClarityBackground
import lk.chargehere.app.ui.components.ClarityCard
import lk.chargehere.app.ui.theme.*
import lk.chargehere.app.ui.components.ClarityPrimaryButton
import lk.chargehere.app.ui.theme.ClarityAccentBlue
import lk.chargehere.app.ui.theme.ClarityDarkGray
import lk.chargehere.app.ui.theme.ClarityMediumGray
import lk.chargehere.app.ui.theme.ClarityOverlay
import lk.chargehere.app.ui.theme.ClaritySpacing
import lk.chargehere.app.ui.viewmodel.HomeViewModel as AppHomeViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import lk.chargehere.app.domain.model.Station
import lk.chargehere.app.domain.model.Reservation
import lk.chargehere.app.ui.viewmodel.HomeViewModel
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ModernHomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToStationDetail: (String) -> Unit,
    onNavigateToReservationDetail: (String) -> Unit,
    viewModel: AppHomeViewModel = hiltViewModel()
) {
    android.util.Log.d("ModernHomeScreen", "ModernHomeScreen composing - ViewModel: ${viewModel.hashCode()}")
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    android.util.Log.d("ModernHomeScreen", "UI State - Loading: ${uiState.isLoading}, Stations: ${uiState.nearbyStations.size}, Error: ${uiState.error}")
    
    // State for selected station and bubble position
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var showInfoWindow by remember { mutableStateOf(false) }
    var markerScreenPosition by remember { mutableStateOf<Offset?>(null) }
    
    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Check if we have location permissions
    val hasLocationPermission = locationPermissions.permissions.all { it.status.isGranted }
    
    // Default location (Colombo, Sri Lanka)
    val defaultLocation = com.google.android.gms.maps.model.LatLng(6.9271, 79.8612)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // Load all stations and upcoming reservations on screen appearance
    // Note: ViewModel init already calls these, but we call again to ensure fresh data
    LaunchedEffect(Unit) {
        android.util.Log.d("ModernHomeScreen", "Screen launched - refreshing data")
        viewModel.refreshData()
    }

    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    ClarityBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-screen Google Map with bubble overlay
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = hasLocationPermission,
                        mapStyleOptions = null
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = hasLocationPermission,
                        mapToolbarEnabled = false
                    )
                ) {
                    // Add custom markers for charging stations
                    uiState.nearbyStations.forEach { station ->
                        // Create custom marker icon based on station type and availability
                        val markerIcon = remember(station.id, station.isAvailable, station.maxPower) {
                            createChargingStationMarker(
                                context = context,
                                stationType = if (station.maxPower > 100) "DC" else "AC",
                                isAvailable = station.isAvailable
                            )
                        }

                        Marker(
                            state = MarkerState(
                                position = com.google.android.gms.maps.model.LatLng(
                                    station.latitude,
                                    station.longitude
                                )
                            ),
                            icon = markerIcon,
                            title = station.name,
                            onClick = { marker ->
                                selectedStation = station

                                // Store marker for screen position calculation
                                // The bubble will be positioned relative to the marker
                                markerScreenPosition = Offset(
                                    x = 0.5f, // Center horizontally (will be adjusted in bubble)
                                    y = 0.4f  // Position slightly above center
                                )

                                showInfoWindow = true
                                true // Return true to consume the event and show our custom bubble
                            }
                        )
                    }
                }

                // Bubble popup rendered INSIDE the map view
                if (showInfoWindow && selectedStation != null && markerScreenPosition != null) {
                    StationDetailsBubble(
                        station = selectedStation!!,
                        markerPosition = markerScreenPosition!!,
                        onDismiss = {
                            showInfoWindow = false
                            selectedStation = null
                            markerScreenPosition = null
                        },
                        onReserveClick = {
                            onNavigateToStationDetail(selectedStation!!.id)
                        }
                    )
                }
            }

            // Search Bar at the top - Clarity style
            ClaritySearchBarOverlay(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        horizontal = ClaritySpacing.md,
                        vertical = ClaritySpacing.xxxl + ClaritySpacing.md // Status bar + margin
                    ),
                onSearchClick = onNavigateToSearch
            )

            // Floating Station/Reservation Card - Clarity style
            ClarityFloatingCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        horizontal = ClaritySpacing.md,
                        vertical = ClaritySpacing.xxxl + ClaritySpacing.xl // Increased bottom padding for tab bar spacing
                    ),
                upcomingReservation = uiState.upcomingReservation,
                nearestStation = uiState.nearbyStations.firstOrNull(),
                onReservationClick = { reservation ->
                    val destinationId = reservation.id.ifBlank { reservation.bookingNumber ?: "" }
                    if (destinationId.isNotBlank()) {
                        onNavigateToReservationDetail(destinationId)
                    } else {
                        android.util.Log.w("ModernHomeScreen", "Unable to navigate: reservation id missing")
                    }
                },
                onStationClick = { station ->
                    onNavigateToStationDetail(station.id)
                }
            )

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ClarityOverlay),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ClarityAccentBlue)
                }
            }
            
            // Location permission request UI
            if (!hasLocationPermission) {
                LocationPermissionCard(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(ClaritySpacing.md),
                    onRequestPermission = {
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun ClaritySearchBarOverlay(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSearchClick() }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ClarityShadow,
                spotColor = ClarityShadow
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClaritySurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = ClarityMediumGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(ClaritySpacing.sm))
            Text(
                text = "Search charging stations...",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ClarityFloatingCard(
    modifier: Modifier = Modifier,
    upcomingReservation: Reservation?,
    nearestStation: Station?,
    onReservationClick: (Reservation) -> Unit,
    onStationClick: (Station) -> Unit
) {
    // Determine if there's an active booking (APPROVED status means actively in progress)
    val hasActiveBooking = upcomingReservation?.status?.uppercase() == "APPROVED"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ClarityShadow,
                spotColor = ClarityShadow
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClaritySurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(ClaritySpacing.md)
        ) {
            if (hasActiveBooking && upcomingReservation != null) {
                // Active booking section - takes priority
                ClarityReservationSection(
                    reservation = upcomingReservation,
                    onClick = { onReservationClick(upcomingReservation) }
                )
            } else if (nearestStation != null) {
                // Nearest station section - only show when no active booking
                ClarityNearestStationSection(
                    station = nearestStation,
                    onClick = { onStationClick(nearestStation) }
                )
            }
        }
    }
}

@Composable
private fun ClarityReservationSection(
    reservation: Reservation,
    onClick: () -> Unit
) {
    var showQRCode by remember { mutableStateOf(false) }

    // Determine if this is an active booking
    val isActiveBooking = reservation.status.uppercase() == "APPROVED"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isActiveBooking) "Active Booking" else "Upcoming Reservation",
                style = MaterialTheme.typography.labelMedium,
                color = ClarityMediumGray
            )
            ClarityStatusChip(
                text = when (reservation.status.uppercase()) {
                    "APPROVED" -> "Active"
                    "PENDING" -> "Pending"
                    else -> reservation.status
                },
                status = when (reservation.status.uppercase()) {
                    "APPROVED" -> ClarityStatus.Success
                    "PENDING" -> ClarityStatus.Warning
                    else -> ClarityStatus.Neutral
                }
            )
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.sm))

        Text(
            text = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(reservation.startTime),
                ZoneId.systemDefault()
            ).format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
            style = MaterialTheme.typography.headlineSmall,
            color = ClarityDarkGray
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.xs))

        Text(
            text = "${reservation.durationMinutes} minutes at ${reservation.stationName}",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityMediumGray
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.md))

        // QR Code Section (expandable)
        if (showQRCode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = ClaritySpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QRCodeGenerator(
                    content = reservation.qrPayload ?: "reservation:${reservation.id}",
                    modifier = Modifier.size(180.dp)
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                Text(
                    text = "Show this QR code to the station operator",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClarityMediumGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                ClarityTextButton(
                    text = "Hide QR Code",
                    onClick = { showQRCode = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Show QR and View Details buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
            ) {
                ClaritySecondaryButton(
                    text = "Show QR",
                    onClick = { showQRCode = true },
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
}

@Composable
private fun ClarityNearestStationSection(
    station: Station,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nearest Station",
                style = MaterialTheme.typography.labelMedium,
                color = ClarityMediumGray
            )
            ClarityStatusChip(
                text = if (station.isAvailable) "Available" else "Occupied",
                status = if (station.isAvailable) ClarityStatus.Success else ClarityStatus.Error
            )
        }
        
        Spacer(modifier = Modifier.height(ClaritySpacing.sm))
        
        Text(
            text = station.name,
            style = MaterialTheme.typography.headlineSmall,
            color = ClarityDarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(ClaritySpacing.xs))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = ClarityMediumGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(ClaritySpacing.xs))
            Text(
                text = "${station.maxPower}kW",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
            
            Spacer(modifier = Modifier.width(ClaritySpacing.md))
            
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = ClarityMediumGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(ClaritySpacing.xs))
            Text(
                text = "2.1 km away",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
        }
        
        Spacer(modifier = Modifier.height(ClaritySpacing.md))
        
        ClarityPrimaryButton(
            text = "Reserve Now",
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LocationPermissionCard(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    ClarityCard(
        modifier = modifier
    ) {
        // Content is already in a Column scope
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.lg),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = null,
                tint = ClarityAccentBlue,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(ClaritySpacing.md))
        
        Text(
            text = "Location Access Required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ClarityDarkGray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(ClaritySpacing.sm))
        
        Text(
            text = "To show your current location and find nearby charging stations, please grant location permission.",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityMediumGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(ClaritySpacing.lg))
        
        ClarityPrimaryButton(
            text = "Grant Permission",
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Bubble popup that appears near the tapped marker
 * Shows detailed information about the charging station
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationDetailsBubble(
    station: Station,
    markerPosition: Offset, // Normalized position (0.0-1.0) of marker on screen
    onDismiss: () -> Unit,
    onReserveClick: () -> Unit
) {
    // Semi-transparent overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClarityOverlay)
            .clickable(onClick = onDismiss)
    )

    // Bubble positioned alongside the marker
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Calculate bubble position - place it slightly above and to the side of the marker
        val bubbleWidth = 320.dp
        val bubbleOffset = 24.dp // Distance from marker

        // Position bubble above the marker (marker is at markerPosition.y)
        val bubbleY = (screenHeight * markerPosition.y) - bubbleOffset - 280.dp

        // Center horizontally
        val bubbleX = (screenWidth - bubbleWidth) / 2

        Box(
            modifier = Modifier
                .width(bubbleWidth)
                .offset(x = bubbleX, y = bubbleY.coerceAtLeast(ClaritySpacing.xxxl + 80.dp)) // Keep below top bar
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Main bubble card with speech bubble shape
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) { } // Prevent clicks from dismissing
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = ClarityShadow,
                        spotColor = ClarityShadow
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ClaritySurfaceWhite
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ClaritySpacing.md)
                ) {
                    // Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = ClarityMediumGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Header with station name and type badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = station.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ClarityDarkGray,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(ClaritySpacing.xs))

                        // Station type badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (station.maxPower > 100) ClarityAccentBlue else ClaritySuccessGreen
                        ) {
                            Text(
                                text = if (station.maxPower > 100) "DC" else "AC",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(
                                    horizontal = ClaritySpacing.sm,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                    // Address
                    if (!station.address.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ClarityMediumGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(ClaritySpacing.xs))
                            Text(
                                text = station.address ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClarityMediumGray,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(ClaritySpacing.md))
                    }

                    // Details in a compact grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
                    ) {
                        // Power capacity
                        BubbleDetailChip(
                            icon = Icons.Default.Bolt,
                            text = "${station.maxPower.toInt()}kW",
                            modifier = Modifier.weight(1f)
                        )

                        // Availability status
                        BubbleDetailChip(
                            icon = if (station.isAvailable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            text = if (station.isAvailable) "Available" else "Occupied",
                            backgroundColor = if (station.isAvailable)
                                ClaritySuccessGreen.copy(alpha = 0.1f)
                            else
                                ClarityErrorRed.copy(alpha = 0.1f),
                            textColor = if (station.isAvailable) ClaritySuccessGreen else ClarityErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(ClaritySpacing.md))

                    // Action button
                    ClarityPrimaryButton(
                        text = if (station.isReservable) "Reserve Now" else "View Details",
                        onClick = onReserveClick,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.ArrowForward
                    )
                }
            }

                // Triangular pointer pointing down to the marker
                Canvas(
                    modifier = Modifier
                        .size(24.dp, 12.dp)
                        .offset(y = (-1).dp)
                ) {
                    val trianglePath = Path().apply {
                        // Pointer pointing down to marker
                        moveTo(size.width / 2f, size.height)
                        lineTo(size.width / 2f - 12.dp.toPx(), 0f)
                        lineTo(size.width / 2f + 12.dp.toPx(), 0f)
                        close()
                    }
                    drawPath(
                        path = trianglePath,
                        color = Color.White,
                        style = Fill
                    )
                }
            }
        }
    }
}

/**
 * Compact detail chip for bubble popup
 */
@Composable
private fun BubbleDetailChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ClarityLightGray,
    textColor: Color = ClarityDarkGray
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ClaritySpacing.sm,
                vertical = ClaritySpacing.xs
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

private fun formatDistance(meters: Int): String {
    return when {
        meters < 1000 -> "${meters}m"
        else -> String.format("%.1f km", meters / 1000.0)
    }
}