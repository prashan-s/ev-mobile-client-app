package lk.chargehere.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import lk.chargehere.app.domain.model.Station
import lk.chargehere.app.ui.theme.*

/**
 * Custom marker icon for charging stations
 * Shows different colors for AC/DC and availability status
 */
@Composable
fun ChargingStationMarkerIcon(
    stationType: String,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        !isAvailable -> ClarityMediumGray
        stationType.uppercase() == "DC" -> ClarityAccentBlue
        else -> ClaritySuccessGreen
    }
    
    val icon = when {
        stationType.uppercase() == "DC" -> Icons.Default.Bolt
        else -> Icons.Default.EvStation
    }
    
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = ClarityShadow,
                spotColor = ClarityShadow
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .border(3.dp, ClaritySurfaceWhite, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Charging Station",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Smaller variant for selected/focused stations
 */
@Composable
fun ChargingStationMarkerIconSmall(
    stationType: String,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        !isAvailable -> ClarityMediumGray
        stationType.uppercase() == "DC" -> ClarityAccentBlue
        else -> ClaritySuccessGreen
    }
    
    val icon = when {
        stationType.uppercase() == "DC" -> Icons.Default.Bolt
        else -> Icons.Default.EvStation
    }
    
    Box(
        modifier = modifier
            .size(36.dp)
            .shadow(
                elevation = 3.dp,
                shape = CircleShape,
                ambientColor = ClarityShadow,
                spotColor = ClarityShadow
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .border(2.dp, ClaritySurfaceWhite, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Charging Station",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Custom info window content for map markers
 * Shows compact station details
 */
@Composable
fun StationInfoWindowContent(
    station: Station,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ClarityShadow,
                spotColor = ClarityShadow
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClaritySurfaceWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.md)
        ) {
            // Header with station name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClarityDarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Station type badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (station.maxPower > 100) ClarityAccentBlue else ClaritySuccessGreen,
                    modifier = Modifier.padding(start = ClaritySpacing.xs)
                ) {
                    Text(
                        text = if (station.maxPower > 100) "DC" else "AC",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(
                            horizontal = ClaritySpacing.xs,
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
                    modifier = Modifier.padding(bottom = ClaritySpacing.xs)
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(ClaritySpacing.xs))
            
            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Power
                InfoChip(
                    icon = Icons.Default.Bolt,
                    text = "${station.maxPower.toInt()}kW",
                    color = ClarityAccentBlue
                )
                
                // Status
                InfoChip(
                    icon = if (station.isAvailable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    text = if (station.isAvailable) "Available" else "Occupied",
                    color = if (station.isAvailable) ClaritySuccessGreen else ClarityErrorRed
                )
                
                // Distance (if available)
                station.distanceMeters?.let { distance ->
                    InfoChip(
                        icon = Icons.Default.NearMe,
                        text = formatDistance(distance.toInt()),
                        color = ClarityMediumGray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(ClaritySpacing.sm))
            
            // Tap to view details hint
            Text(
                text = "Tap to view details",
                style = MaterialTheme.typography.labelSmall,
                color = ClarityAccentBlue,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = ClarityMediumGray,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDistance(meters: Int): String {
    return when {
        meters < 1000 -> "${meters}m"
        else -> String.format("%.1fkm", meters / 1000.0)
    }
}

/**
 * Utility to convert a Composable to BitmapDescriptor for Google Maps
 * This creates bitmap markers programmatically to avoid ComposeView attachment issues
 */
fun createBitmapDescriptorFromComposable(
    context: Context,
    content: @Composable () -> Unit
): BitmapDescriptor {
    // Create a fallback bitmap marker programmatically
    // This avoids the ComposeView attachment issue
    return createDefaultMarkerBitmap(context)
}

/**
 * Creates a default marker bitmap programmatically
 */
private fun createDefaultMarkerBitmap(context: Context): BitmapDescriptor {
    val size = 48
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Draw circle background
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.parseColor("#4CAF50")
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint)
    
    // Draw white border
    paint.apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Creates a marker bitmap for charging stations
 * @param stationType "AC" or "DC"
 * @param isAvailable availability status
 */
fun createChargingStationMarker(
    context: Context,
    stationType: String,
    isAvailable: Boolean
): BitmapDescriptor {
    val size = 96 // Size in pixels (48dp * 2 for better quality)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Determine color based on type and availability
    val backgroundColor = when {
        !isAvailable -> android.graphics.Color.parseColor("#757575") // Gray
        stationType.uppercase() == "DC" -> android.graphics.Color.parseColor("#2196F3") // Blue
        else -> android.graphics.Color.parseColor("#4CAF50") // Green
    }
    
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
    }
    
    // Draw shadow
    paint.apply {
        color = android.graphics.Color.parseColor("#40000000")
        style = android.graphics.Paint.Style.FILL
        maskFilter = android.graphics.BlurMaskFilter(8f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(size / 2f, size / 2f + 4f, size / 2f - 6f, paint)
    
    // Draw main circle
    paint.apply {
        color = backgroundColor
        style = android.graphics.Paint.Style.FILL
        maskFilter = null
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, paint)
    
    // Draw white border
    paint.apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 6f
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, paint)
    
    // Draw icon (lightning bolt for DC, plug for AC)
    paint.apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.FILL
        strokeWidth = 4f
    }
    
    if (stationType.uppercase() == "DC") {
        // Draw lightning bolt
        val path = android.graphics.Path().apply {
            moveTo(size / 2f + 8f, size / 2f - 20f)
            lineTo(size / 2f - 8f, size / 2f - 2f)
            lineTo(size / 2f + 2f, size / 2f - 2f)
            lineTo(size / 2f - 8f, size / 2f + 20f)
            lineTo(size / 2f + 8f, size / 2f + 2f)
            lineTo(size / 2f - 2f, size / 2f + 2f)
            close()
        }
        canvas.drawPath(path, paint)
    } else {
        // Draw charging plug icon
        // Plug body
        canvas.drawRoundRect(
            size / 2f - 10f, size / 2f - 12f,
            size / 2f + 10f, size / 2f + 12f,
            4f, 4f, paint
        )
        
        // Plug prongs
        canvas.drawRect(
            size / 2f - 6f, size / 2f - 20f,
            size / 2f - 2f, size / 2f - 12f,
            paint
        )
        canvas.drawRect(
            size / 2f + 2f, size / 2f - 20f,
            size / 2f + 6f, size / 2f - 12f,
            paint
        )
    }
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
