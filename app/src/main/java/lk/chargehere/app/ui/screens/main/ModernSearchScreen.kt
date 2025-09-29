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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.domain.model.Station
import lk.chargehere.app.ui.viewmodel.SearchViewModel
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchScreen(
    onNavigateToStationDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.searchStations(searchQuery)
        } else {
            viewModel.clearSearch()
        }
    }

    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    ClarityBackground {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top spacing for status bar
            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))

            // Search Header
            ClaritySearchHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(horizontal = ClaritySpacing.md)
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.md))

            // Search Results Content
            when {
                uiState.isLoading -> {
                    ClarityLoadingState()
                }
                
                searchQuery.isBlank() -> {
                    ClarityEmptySearchState()
                }
                
                uiState.searchResults.isEmpty() && searchQuery.isNotBlank() -> {
                    ClarityNoResultsState(query = searchQuery)
                }
                
                else -> {
                    ClaritySearchResults(
                        stations = uiState.searchResults,
                        onStationClick = onNavigateToStationDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun ClaritySearchHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
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
        
        // Search field
        ClarityTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = "Search",
            placeholder = "Find charging stations...",
            modifier = Modifier.weight(1f)
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
                text = "Searching stations...",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
        }
    }
}

@Composable
private fun ClarityEmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(ClaritySpacing.lg)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = ClarityMediumGray,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            
            Text(
                text = "Search for Charging Stations",
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.sm))
            
            Text(
                text = "Type the name or location of a charging station to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ClarityNoResultsState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(ClaritySpacing.lg)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = ClarityMediumGray,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            
            Text(
                text = "No stations found",
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.sm))
            
            Text(
                text = "We couldn't find any charging stations matching \"$query\". Try a different search term.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ClaritySearchResults(
    stations: List<Station>,
    onStationClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = ClaritySpacing.md,
            vertical = ClaritySpacing.sm
        ),
        verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
    ) {
        // Results header
        item {
            Text(
                text = "${stations.size} stations found",
                style = MaterialTheme.typography.labelMedium,
                color = ClarityMediumGray,
                modifier = Modifier.padding(
                    horizontal = ClaritySpacing.xs,
                    vertical = ClaritySpacing.sm
                )
            )
        }
        
        // Station list - using proper spacing instead of dividers
        items(stations) { station ->
            ClarityStationSearchItem(
                station = station,
                onClick = { onStationClick(station.id) }
            )
        }
        
        // Bottom padding for floating nav
        item {
            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
        }
    }
}

@Composable
private fun ClarityStationSearchItem(
    station: Station,
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
            // Station icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (station.isAvailable) 
                            ClaritySuccessGreen.copy(alpha = 0.1f) 
                        else 
                            ClarityErrorRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricCar,
                    contentDescription = null,
                    tint = if (station.isAvailable) ClaritySuccessGreen else ClarityErrorRed,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(ClaritySpacing.md))
            
            // Station info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = ClarityDarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(ClaritySpacing.xs))
                
                Text(
                    text = station.address ?: "Address not available",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClarityMediumGray,
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
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(ClaritySpacing.xs))
                    Text(
                        text = "${station.maxPower}kW",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClarityMediumGray
                    )
                    
                    Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                    
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ClarityMediumGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(ClaritySpacing.xs))
                    Text(
                        text = "1.2 km", // Calculate actual distance
                        style = MaterialTheme.typography.labelSmall,
                        color = ClarityMediumGray
                    )
                }
            }
            
            // Status and arrow
            Column(
                horizontalAlignment = Alignment.End
            ) {
                ClarityStatusChip(
                    text = if (station.isAvailable) "Available" else "Occupied",
                    status = if (station.isAvailable) ClarityStatus.Success else ClarityStatus.Error
                )
                
                Spacer(modifier = Modifier.height(ClaritySpacing.sm))
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = ClarityMediumGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}