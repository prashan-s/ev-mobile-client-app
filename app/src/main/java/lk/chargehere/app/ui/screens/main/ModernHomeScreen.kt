package lk.chargehere.app.ui.screens.main

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // State for selected station (for info window)
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var showInfoWindow by remember { mutableStateOf(false) }
    
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

    // Request location permission and load nearby stations
    LaunchedEffect(Unit) {
        viewModel.loadNearbyStations(defaultLocation.latitude, defaultLocation.longitude)
        viewModel.loadUpcomingReservations()
    }

    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    ClarityBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-screen Google Map
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
                    
                    MarkerInfoWindowContent(
                        state = MarkerState(
                            position = com.google.android.gms.maps.model.LatLng(
                                station.latitude,
                                station.longitude
                            )
                        ),
                        icon = markerIcon,
                        onClick = {
                            selectedStation = station
                            showInfoWindow = true
                            false // Return false to show info window
                        },
                        onInfoWindowClick = {
                            onNavigateToStationDetail(station.id)
                        }
                    ) {
                        StationInfoWindowContent(station = station)
                    }
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
                        vertical = ClaritySpacing.xxxl + ClaritySpacing.lg // Bottom nav + margin
                    ),
                upcomingReservation = uiState.upcomingReservation,
                nearestStation = uiState.nearbyStations.firstOrNull(),
                onReservationClick = { reservation ->
                    onNavigateToReservationDetail(reservation.id)
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
            if (upcomingReservation != null) {
                // Upcoming reservation section
                ClarityReservationSection(
                    reservation = upcomingReservation,
                    onClick = { onReservationClick(upcomingReservation) }
                )
                
                if (nearestStation != null) {
                    Spacer(modifier = Modifier.height(ClaritySpacing.md))
                    HorizontalDivider(
                        color = ClarityLightGray,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(ClaritySpacing.md))
                }
            }
            
            if (nearestStation != null) {
                // Nearest station section
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
                text = "Upcoming Reservation",
                style = MaterialTheme.typography.labelMedium,
                color = ClarityMediumGray
            )
            ClarityStatusChip(
                text = "Active",
                status = ClarityStatus.Success
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
            text = "${reservation.durationMinutes} minutes • Tap to view QR code",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityMediumGray
        )
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