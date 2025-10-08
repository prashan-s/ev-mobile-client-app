package lk.chargehere.app.ui.components

import android.Manifest
import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import lk.chargehere.app.ui.theme.*
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerComponent(
    onQRCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    showInstructions: Boolean = true
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Box(modifier = modifier.fillMaxSize()) {
        when {
            cameraPermissionState.status.isGranted -> {
                CameraPreviewWithScanner(
                    onQRCodeScanned = onQRCodeScanned,
                    showInstructions = showInstructions
                )
            }
            else -> {
                CameraPermissionRequest(
                    permissionState = cameraPermissionState
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CameraPermissionRequest(
    permissionState: PermissionState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClarityDarkGray)
            .padding(ClaritySpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = ClarityPureWhite,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.lg))

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            color = ClarityPureWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.sm))

        Text(
            text = "We need camera access to scan QR codes",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityPureWhite.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.xl))

        ClarityPrimaryButton(
            text = "Grant Permission",
            onClick = { permissionState.launchPermissionRequest() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CameraPreviewWithScanner(
    onQRCodeScanned: (String) -> Unit,
    showInstructions: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var flashEnabled by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    camera = bindCameraUseCases(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        cameraProvider = cameraProvider,
                        previewView = previewView,
                        onQRCodeDetected = { qrCode ->
                            if (isScanning && qrCode != lastScannedCode) {
                                lastScannedCode = qrCode
                                isScanning = false
                                onQRCodeScanned(qrCode)
                                // Reset after delay to allow rescanning
                                kotlinx.coroutines.MainScope().launch {
                                    kotlinx.coroutines.delay(2000)
                                    isScanning = true
                                }
                            }
                        }
                    )
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Scanning overlay with animated reticle
        AnimatedScanningOverlay(isScanning = isScanning)

        // Flash toggle button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = ClaritySpacing.md, end = ClaritySpacing.md, bottom = ClaritySpacing.md)
                .padding(end = 60.dp)
        ) {
            IconButton(
                onClick = {
                    flashEnabled = !flashEnabled
                    camera?.cameraControl?.enableTorch(flashEnabled)
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Toggle Flash",
                    tint = ClarityPureWhite
                )
            }
        }

        // Instructions
        if (showInstructions) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(ClaritySpacing.xl)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        MaterialTheme.shapes.medium
                    )
                    .padding(ClaritySpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Position QR code within frame",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClarityPureWhite,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                if (!isScanning) {
                    Spacer(modifier = Modifier.height(ClaritySpacing.xs))
                    Text(
                        text = "✓ Code Detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClaritySuccessGreen,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedScanningOverlay(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")

    // Animated scanning line
    val scanLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    // Pulsing corner brackets
    val cornerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cornerScale"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Scanning frame dimensions (70% of screen)
        val frameSize = minOf(canvasWidth, canvasHeight) * 0.7f
        val frameLeft = (canvasWidth - frameSize) / 2
        val frameTop = (canvasHeight - frameSize) / 2

        // Semi-transparent overlay outside the frame
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = Size(canvasWidth, frameTop)
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(0f, frameTop + frameSize),
            size = Size(canvasWidth, canvasHeight - frameTop - frameSize)
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(0f, frameTop),
            size = Size(frameLeft, frameSize)
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(frameLeft + frameSize, frameTop),
            size = Size(canvasWidth - frameLeft - frameSize, frameSize)
        )

        // Corner brackets
        val cornerLength = 40f * cornerScale
        val cornerWidth = 4f
        val cornerColor = if (isScanning) Color(0xFF2196F3) else Color(0xFF4CAF50)

        // Top-left corner
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft + cornerLength, frameTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft, frameTop + cornerLength),
            strokeWidth = cornerWidth
        )

        // Top-right corner
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft + frameSize, frameTop),
            end = Offset(frameLeft + frameSize - cornerLength, frameTop),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft + frameSize, frameTop),
            end = Offset(frameLeft + frameSize, frameTop + cornerLength),
            strokeWidth = cornerWidth
        )

        // Bottom-left corner
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop + frameSize),
            end = Offset(frameLeft + cornerLength, frameTop + frameSize),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop + frameSize),
            end = Offset(frameLeft, frameTop + frameSize - cornerLength),
            strokeWidth = cornerWidth
        )

        // Bottom-right corner
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft + frameSize, frameTop + frameSize),
            end = Offset(frameLeft + frameSize - cornerLength, frameTop + frameSize),
            strokeWidth = cornerWidth
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft + frameSize, frameTop + frameSize),
            end = Offset(frameLeft + frameSize, frameTop + frameSize - cornerLength),
            strokeWidth = cornerWidth
        )

        // Animated scanning line
        if (isScanning) {
            val scanY = frameTop + (frameSize * scanLinePosition)
            drawLine(
                color = cornerColor.copy(alpha = 0.7f),
                start = Offset(frameLeft, scanY),
                end = Offset(frameLeft + frameSize, scanY),
                strokeWidth = 2f
            )
        }

        // Frame border
        drawRoundRect(
            color = cornerColor.copy(alpha = 0.3f),
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameSize, frameSize),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 2f)
        )
    }
}

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    onQRCodeDetected: (String) -> Unit
): Camera {
    // Preview use case
    val preview = Preview.Builder()
        .build()
        .also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

    // Image analysis for QR code detection
    val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
            it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processImageProxy(imageProxy, onQRCodeDetected)
            }
        }

    // Select back camera
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        // Unbind all use cases before rebinding
        cameraProvider.unbindAll()

        // Bind use cases to camera
        return cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalyzer
        )
    } catch (exc: Exception) {
        throw exc
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onQRCodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    when (barcode.valueType) {
                        Barcode.TYPE_TEXT,
                        Barcode.TYPE_URL -> {
                            barcode.rawValue?.let { value ->
                                onQRCodeDetected(value)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Handle error silently
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
