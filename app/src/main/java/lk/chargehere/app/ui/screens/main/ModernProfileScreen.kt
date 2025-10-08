package lk.chargehere.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.ui.viewmodel.ProfileViewModel
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import lk.chargehere.app.ui.utils.keyboardAwareScrollPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernProfileScreen(
    onNavigateToAuth: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showProfileEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Handle logout success
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onNavigateToAuth()
        }
    }

    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    ClarityBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .keyboardAwareScrollPadding()
                .padding(ClaritySpacing.md)
        ) {
            // Profile Header Section
            ClarityProfileHeader(
                user = uiState.user,
                onEditClick = { showProfileEditDialog = true }
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.sectionSpacing))
            
            // Stats Section
            ClarityStatsSection(
                totalReservations = uiState.totalReservations,
                totalChargingTime = uiState.totalChargingTime,
                favoriteStations = uiState.favoriteStations
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.sectionSpacing))
            
            // Logout Button
            ClaritySecondaryButton(
                text = "Sign Out",
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl)) // Bottom padding
        }
    }
    
    // Profile Edit Dialog
    if (showProfileEditDialog) {
        ClarityProfileEditDialog(
            user = uiState.user,
            onDismiss = { showProfileEditDialog = false },
            onSave = { name, email, phone ->
                viewModel.updateProfile(name, email, phone)
                showProfileEditDialog = false
            }
        )
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        ClarityLogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                viewModel.logout()
                showLogoutDialog = false
            }
        )
    }
}

@Composable
private fun ClarityProfileHeader(
    user: lk.chargehere.app.domain.model.User?,
    onEditClick: () -> Unit
) {
    ClarityCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = ClarityAccentBlue.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ClarityAccentBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(ClaritySpacing.md))
            
            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "Loading...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = ClarityDarkGray
                )
                Spacer(modifier = Modifier.height(ClaritySpacing.xs))
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClarityMediumGray
                )
                if (user?.phone != null) {
                    Text(
                        text = user.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = ClarityMediumGray
                    )
                }
            }
            
            // Edit Button
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = ClarityAccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ClarityStatsSection(
    totalReservations: Int,
    totalChargingTime: Int,
    favoriteStations: Int
) {
    ClaritySectionHeader(text = "Your Stats")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
    ) {
        ClarityStatCard(
            label = "Total\nReservations",
            value = totalReservations.toString(),
            modifier = Modifier.weight(1f)
        )
        
        ClarityStatCard(
            label = "Charging\nHours",
            value = totalChargingTime.toString(),
            modifier = Modifier.weight(1f)
        )
        
        ClarityStatCard(
            label = "Favorite\nStations",
            value = favoriteStations.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ClarityStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ClarityCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = ClarityAccentBlue
            )
            Spacer(modifier = Modifier.height(ClaritySpacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ClarityMediumGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ClarityProfileEditDialog(
    user: lk.chargehere.app.domain.model.User?,
    onDismiss: () -> Unit,
    onSave: (String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    
    ClarityModal(
        onDismiss = onDismiss,
        modifier = Modifier.padding(ClaritySpacing.lg)
    ) {
        Column(
            modifier = Modifier.padding(ClaritySpacing.lg)
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.lg))
            
            ClarityTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name"
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            
            ClarityTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            
            ClarityTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone (Optional)"
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.lg))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
            ) {
                ClarityTextButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                
                ClarityPrimaryButton(
                    text = "Save",
                    onClick = {
                        onSave(name, email, phone.takeIf { it.isNotBlank() })
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ClarityLogoutDialog(
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
                text = "Sign Out",
                style = MaterialTheme.typography.headlineSmall,
                color = ClarityDarkGray
            )
            
            Spacer(modifier = Modifier.height(ClaritySpacing.md))
            
            Text(
                text = "Are you sure you want to sign out of your account?",
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
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                
                ClarityPrimaryButton(
                    text = "Sign Out",
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}