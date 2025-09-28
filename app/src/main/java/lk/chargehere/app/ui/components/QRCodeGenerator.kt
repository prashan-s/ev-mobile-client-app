package lk.chargehere.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix

@Composable
fun QRCodeGenerator(
    content: String,
    modifier: Modifier = Modifier,
    foregroundColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val density = LocalDensity.current
    
    val qrCodeBitmap by remember(content) {
        derivedStateOf {
            try {
                val writer = QRCodeWriter()
                val hints = hashMapOf<EncodeHintType, Any>().apply {
                    put(EncodeHintType.MARGIN, 1)
                }
                
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)
                bitMatrix
            } catch (e: Exception) {
                null
            }
        }
    }

    Canvas(
        modifier = modifier
            .background(backgroundColor)
            .aspectRatio(1f)
    ) {
        qrCodeBitmap?.let { bitMatrix ->
            drawQRCode(
                bitMatrix = bitMatrix,
                foregroundColor = foregroundColor,
                backgroundColor = backgroundColor
            )
        }
    }
}

private fun DrawScope.drawQRCode(
    bitMatrix: BitMatrix,
    foregroundColor: Color,
    backgroundColor: Color
) {
    val width = bitMatrix.width
    val height = bitMatrix.height
    val cellSize = size.width / width.coerceAtLeast(height)

    // Draw background
    drawRect(
        color = backgroundColor,
        size = size
    )

    // Draw QR code pixels
    for (x in 0 until width) {
        for (y in 0 until height) {
            if (bitMatrix[x, y]) {
                drawRect(
                    color = foregroundColor,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = x * cellSize,
                        y = y * cellSize
                    ),
                    size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                )
            }
        }
    }
}