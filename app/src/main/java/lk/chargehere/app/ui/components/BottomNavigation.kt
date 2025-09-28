package lk.chargehere.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val iconSelected: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Outlined.Home, Icons.Filled.Home, "Home")
    object Search : BottomNavItem("search", Icons.Outlined.Search, Icons.Filled.Search, "Search")
    object Reservations : BottomNavItem("reservations", Icons.Outlined.DateRange, Icons.Filled.DateRange, "Bookings")
    object Profile : BottomNavItem("profile", Icons.Outlined.Person, Icons.Filled.Person, "Profile")
}

@Composable
fun ChargeHereBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Reservations,
        BottomNavItem.Profile
    )

    // Floating bottom navigation bar
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                FloatingNavItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.iconSelected else item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}