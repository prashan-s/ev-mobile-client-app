package lk.chargehere.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
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
    var qrBitmap by remember(content) { mutableStateOf<Bitmap?>(null) }

    // Generate QR code bitmap in a background coroutine to avoid blocking UI
    LaunchedEffect(content, foregroundColor, backgroundColor) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            try {
                val writer = QRCodeWriter()
                val hints = hashMapOf<EncodeHintType, Any>().apply {
                    put(EncodeHintType.MARGIN, 1)
                }

                // Generate QR code with smaller size for better performance
                val size = 256
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

                // Convert BitMatrix to Bitmap in background thread
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

                val foregroundArgb = foregroundColor.toArgb()
                val backgroundArgb = backgroundColor.toArgb()

                // Create pixel array
                val pixels = IntArray(width * height)
                for (y in 0 until height) {
                    val offset = y * width
                    for (x in 0 until width) {
                        pixels[offset + x] = if (bitMatrix[x, y]) foregroundArgb else backgroundArgb
                    }
                }

                bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                qrBitmap = bitmap
            } catch (e: Exception) {
                android.util.Log.e("QRCodeGenerator", "Error generating QR code", e)
                qrBitmap = null
            }
        }
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .aspectRatio(1f)
    ) {
        qrBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}