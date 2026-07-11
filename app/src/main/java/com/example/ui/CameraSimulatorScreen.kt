package com.example.ui

import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SyntheticFeedView
import com.example.ui.components.VideoPlayerView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraSimulatorScreen(
    viewModel: CameraViewModel,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Viewfinder Core States
    val videoSourceType by viewModel.videoSourceType.collectAsState()
    val videoUri by viewModel.videoUri.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val digitalZoom by viewModel.digitalZoom.collectAsState()
    val exposureCompensation by viewModel.exposureCompensation.collectAsState()

    // Overlay content states
    val textOverlayPrimary by viewModel.textOverlayPrimary.collectAsState()
    val textOverlaySecondary by viewModel.textOverlaySecondary.collectAsState()
    val overlayPosition by viewModel.overlayPosition.collectAsState()
    val textOverlayColor by viewModel.textOverlayColor.collectAsState()
    val textBgColor by viewModel.textBgColor.collectAsState()
    val textSize by viewModel.textSize.collectAsState()

    // HUD settings states
    val showTimestamp by viewModel.showTimestamp.collectAsState()
    val showRecIndicator by viewModel.showRecIndicator.collectAsState()
    val showBattery by viewModel.showBattery.collectAsState()
    val showGrid by viewModel.showGrid.collectAsState()

    // Filters and tuning states
    val filterType by viewModel.filterType.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val contrast by viewModel.contrast.collectAsState()
    val saturation by viewModel.saturation.collectAsState()

    // Recording states
    val isRecording by viewModel.isRecording.collectAsState()
    val recordDurationSeconds by viewModel.recordDurationSeconds.collectAsState()

    // Drag-up control panel state
    var controlPanelExpanded by remember { mutableStateOf(true) }
    var activeSettingsTab by remember { mutableStateOf(0) } // 0: Source, 1: Overlays, 2: Filters, 3: HUD, 4: Save Preset

    // Gallery File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setVideoSource("GALLERY", it.toString())
        }
    }

    // Dynamic ticking clock for surveillance timestamp
    var liveTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            liveTimeString = format.format(Date())
            delay(1000)
        }
    }

    // Interactive autofocus ring target click coordinates
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            delay(1500)
            focusPoint = null
        }
    }

    // Simulated Photo Capture screen flash animation trigger
    var isFlashActive by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.flashTriggerFlow.collect {
            isFlashActive = true
            delay(150)
            isFlashActive = false
        }
    }

    // Color matrices for real-time viewfinder graphicsLayer
    val colorMatrix = remember(filterType, exposureCompensation, brightness, contrast) {
        val baseMatrix = when (filterType) {
            "NIGHT_VISION" -> floatArrayOf(
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.8f, 0.0f, 0.0f, 15f/255f,
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
            "MONOCHROME" -> floatArrayOf(
                0.213f, 0.715f, 0.072f, 0.0f, 0.0f,
                0.213f, 0.715f, 0.072f, 0.0f, 0.0f,
                0.213f, 0.715f, 0.072f, 0.0f, 0.0f,
                0.0f,   0.0f,   0.0f,   1.0f, 0.0f
            )
            "INFRARED" -> floatArrayOf(
                -0.9f,  0.0f,  0.3f, 0.0f, 220f/255f,
                 0.0f, -0.9f,  0.0f, 0.0f, 180f/255f,
                 0.2f,  0.0f, -0.9f, 0.0f, 240f/255f,
                 0.0f,  0.0f,  0.0f, 1.0f, 0.0f
            )
            "SEPIA" -> floatArrayOf(
                0.393f, 0.769f, 0.189f, 0.0f, 0.0f,
                0.349f, 0.686f, 0.168f, 0.0f, 0.0f,
                0.272f, 0.534f, 0.131f, 0.0f, 0.0f,
                0.0f,   0.0f,   0.0f,   1.0f, 0.0f
            )
            "VHS" -> floatArrayOf(
                0.95f, 0.0f,  0.15f, 0.0f, 10f/255f,
                0.0f,  1.05f, 0.0f,  0.0f, 0.0f,
                0.05f, 0.0f,  1.15f, 0.0f, 5f/255f,
                0.0f,  0.0f,  0.0f,  1.0f, 0.0f
            )
            else -> floatArrayOf(
                1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
        }

        // Apply Exposure compensation & custom brightness/contrast slider math directly on the Matrix!
        val brightnessFactor = brightness + exposureCompensation * 0.25f
        val contrastFactor = contrast
        
        // Simple manual color scaling to emulate exposure/brightness/contrast changes
        if (brightnessFactor != 1.0f || contrastFactor != 1.0f) {
            val scale = contrastFactor * brightnessFactor
            val translate = (1.0f - contrastFactor) * 0.5f * brightnessFactor
            for (row in 0..2) {
                val offset = row * 5
                baseMatrix[offset] = baseMatrix[offset] * scale
                baseMatrix[offset + 1] = baseMatrix[offset + 1] * scale
                baseMatrix[offset + 2] = baseMatrix[offset + 2] * scale
                baseMatrix[offset + 4] = baseMatrix[offset + 4] * scale + translate
            }
        }
        ColorMatrix(baseMatrix)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Core Camera Viewport Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            focusPoint = offset
                        }
                    )
                }
                .testTag("camera_viewport")
        ) {
            // Render Selected Video source
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1.0f)
            ) {
                if (videoSourceType == "SYNTHETIC") {
                    SyntheticFeedView(
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    VideoPlayerView(
                        videoUri = videoUri,
                        isPlaying = isPlaying,
                        zoomScale = digitalZoom,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Apply global screen filter overlays (like scanlines, VHS grid, green CCTV overlay)
            if (filterType == "NIGHT_VISION" || filterType == "VHS") {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineSpacing = 16f
                    val lineCount = (size.height / lineSpacing).toInt()
                    // Draw horizontal retro scan lines
                    for (i in 0..lineCount) {
                        drawLine(
                            color = Color(0x1A000000),
                            start = Offset(0f, i * lineSpacing),
                            end = Offset(size.width, i * lineSpacing),
                            strokeWidth = 2f
                        )
                    }
                }
            }

            // 2. 3x3 Composition Grid Lines
            if (showGrid) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Vertical grid lines
                    drawLine(Color(0x66FFFFFF), Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1f)
                    drawLine(Color(0x66FFFFFF), Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), strokeWidth = 1f)
                    
                    // Horizontal grid lines
                    drawLine(Color(0x66FFFFFF), Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1f)
                    drawLine(Color(0x66FFFFFF), Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), strokeWidth = 1f)
                }
            }

            // 3. Dynamic Text Overlays (Position-Dependent rendering)
            if (textOverlayPrimary.isNotBlank() || textOverlaySecondary.isNotBlank() || (showTimestamp && liveTimeString.isNotBlank())) {
                val overlayAlignment = when (overlayPosition) {
                    "TOP_LEFT" -> Alignment.TopStart
                    "TOP_RIGHT" -> Alignment.TopEnd
                    "BOTTOM_LEFT" -> Alignment.BottomStart
                    "BOTTOM_RIGHT" -> Alignment.BottomEnd
                    else -> Alignment.Center
                }

                val parsedTextColor = try { Color(android.graphics.Color.parseColor(textOverlayColor)) } catch (e: Exception) { Color.White }
                val parsedBgColor = try { Color(android.graphics.Color.parseColor(textBgColor)) } catch (e: Exception) { Color(0x80000000) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 80.dp), // Safe paddings for status bar and header overlays
                    contentAlignment = overlayAlignment
                ) {
                    Column(
                        modifier = Modifier
                            .background(parsedBgColor, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (textOverlayPrimary.isNotBlank()) {
                            Text(
                                text = textOverlayPrimary.uppercase(),
                                color = parsedTextColor,
                                fontSize = textSize.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Start
                            )
                        }

                        if (textOverlaySecondary.isNotBlank()) {
                            Text(
                                text = textOverlaySecondary,
                                color = parsedTextColor.copy(alpha = 0.85f),
                                fontSize = (textSize - 2).coerceAtLeast(10).sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Start
                            )
                        }

                        // Surveillance dynamic timestamp embedded inside overlay box
                        if (showTimestamp) {
                            Text(
                                text = liveTimeString,
                                color = parsedTextColor,
                                fontSize = (textSize - 2).coerceAtLeast(10).sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // 4. System Camera Indicators overlay (REC blinking, Battery, Live banner)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top controls overlay row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back arrow icon
                    IconButton(
                        onClick = onBackToDashboard,
                        modifier = Modifier
                            .background(Color(0x66000000), CircleShape)
                            .testTag("back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }

                    // Blinking REC dot
                    if (showRecIndicator) {
                        val recBlinkTransition = rememberInfiniteTransition(label = "rec_dot")
                        val blinkAlpha by recBlinkTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = 0.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "blink"
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0x66000000), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .alpha(blinkAlpha)
                                    .background(Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REC",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Battery HUD
                    if (showBattery) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0x66000000), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LNK 94% 🔋",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // 5. Autofocus Ring target placement on tap
            focusPoint?.let { point ->
                Box(
                    modifier = Modifier
                        .offset { IntOffset(point.x.toInt() - 50, point.y.toInt() - 50) }
                        .size(100.dp)
                        .border(1.5.dp, Color(0xFF39FF14), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Small blinking focus point inside
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF39FF14), CircleShape)
                    )
                }
            }

            // 6. Camera Screen Capture white flash card overlay
            if (isFlashActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
            }
        }

        // 7. Right-side Quick Zoom indicators overlay
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .background(Color(0xAA111827), RoundedCornerShape(24.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ZOOM", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            
            listOf(1.0f, 2.0f, 4.0f).forEach { zoomVal ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (digitalZoom == zoomVal) Color(0xFF39FF14) else Color(0xFF1F2937))
                        .clickable { viewModel.setDigitalZoom(zoomVal) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${zoomVal.toInt()}x",
                        color = if (digitalZoom == zoomVal) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 8. Exposure Compensation adjustment slider (bottom-right above control panel)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .background(Color(0xAA111827), RoundedCornerShape(16.dp))
                .padding(10.dp)
                .width(44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("EV", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${if (exposureCompensation >= 0) "+" else ""}${exposureCompensation.toInt()}",
                color = Color(0xFF39FF14),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Increment / Decrement EV buttons
            IconButton(
                onClick = { if (exposureCompensation < 2.0f) viewModel.setExposureCompensation(exposureCompensation + 1.0f) },
                modifier = Modifier.size(28.dp).background(Color(0xFF374151), CircleShape)
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            IconButton(
                onClick = { if (exposureCompensation > -2.0f) viewModel.setExposureCompensation(exposureCompensation - 1.0f) },
                modifier = Modifier.size(28.dp).background(Color(0xFF374151), CircleShape)
            ) {
                Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // 9. Floating Shutter Action Overlay (Right center / center-bottom above control panel)
        // We will put it beautifully integrated in the bottom bar, but we can also place a quick float here!

        // 10. REAL-TIME EDITING CONTROL PANEL (Bottom Sliding Sheet layout)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF0D1117), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .border(1.dp, Color(0xFF21262D), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(16.dp)
        ) {
            // Drag handle/toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { controlPanelExpanded = !controlPanelExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (controlPanelExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = "Mở rộng bảng cấu hình",
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (controlPanelExpanded) "ẨN BẢNG CẤU HÌNH CAMERA" else "CẤU HÌNH NỘI DUNG THỜI GIAN THỰC",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            AnimatedVisibility(visible = controlPanelExpanded) {
                Column {
                    // Shutter Buttons & Capture Actions Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause stream toggle
                        IconButton(
                            onClick = { viewModel.togglePlaying() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF21262D), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.Refresh,
                                contentDescription = if (isPlaying) "Tạm dừng" else "Tiếp tục",
                                tint = Color.White
                            )
                        }

                        // Huge white circular Shutter capture photo button
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { viewModel.capturePhoto() }
                                .testTag("shutter_photo_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .border(2.dp, Color.Black, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Chụp hình",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Red Record video button
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) Color.Red else Color(0x33FF3914))
                                .border(
                                    width = 2.dp,
                                    color = if (isRecording) Color.White else Color.Red,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.toggleRecording() }
                                .testTag("shutter_record_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRecording) {
                                // Stop square icon or live timing text
                                Text(
                                    text = "${recordDurationSeconds}s",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    Icons.Default.RadioButtonChecked,
                                    contentDescription = "Quay",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Tabs selection bar (Source, Overlay, Filter, HUD, Presets)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161B22), RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val tabs = listOf(
                            Pair(Icons.Default.VideoLibrary, "Nguồn"),
                            Pair(Icons.Default.TextFields, "Chữ"),
                            Pair(Icons.Default.Tune, "Bộ Lọc"),
                            Pair(Icons.Default.GridOn, "HUD"),
                            Pair(Icons.Default.Save, "Lưu")
                        )
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = activeSettingsTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF21262D) else Color.Transparent)
                                    .clickable { activeSettingsTab = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = tab.first,
                                        contentDescription = tab.second,
                                        tint = if (isSelected) Color(0xFF39FF14) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tab.second,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab Contents Container (Scrollable)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (activeSettingsTab) {
                                // 0: Media Source
                                0 -> {
                                    Text(
                                        "Chọn Nguồn Video Thay Thế:",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )

                                    // Local Gallery selection button
                                    Button(
                                        onClick = { filePickerLauncher.launch("video/*") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .testTag("gallery_video_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFF39FF14))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Tải Video Từ Thư Viện Máy", color = Color.White, fontSize = 13.sp)
                                    }

                                    // Preloaded templates
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = { viewModel.setVideoSource("SYNTHETIC", "radar_scanning") },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (videoSourceType == "SYNTHETIC") Color(0x3339FF14) else Color(0xFF21262D)
                                            )
                                        ) {
                                            Text("Radar Quét", color = Color.White, fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { viewModel.setVideoSource("TEMPLATE", "https://assets.mixkit.co/videos/preview/mixkit-supermarket-security-camera-footage-40540-large.mp4") },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (videoUri.contains("supermarket")) Color(0x3339FF14) else Color(0xFF21262D)
                                            )
                                        ) {
                                            Text("Siêu Thị", color = Color.White, fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { viewModel.setVideoSource("TEMPLATE", "https://assets.mixkit.co/videos/preview/mixkit-highway-traffic-in-the-afternoon-41553-large.mp4") },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (videoUri.contains("highway")) Color(0x3339FF14) else Color(0xFF21262D)
                                            )
                                        ) {
                                            Text("Giao Thông", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }

                                // 1: Text Overlays (Changing text contents in real-time!)
                                1 -> {
                                    Text(
                                        "Thay đổi văn bản thời gian thực:",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )

                                    TextField(
                                        value = textOverlayPrimary,
                                        onValueChange = { viewModel.setOverlayPrimary(it) },
                                        label = { Text("Dòng Chữ 1 (CCTV Name)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("realtime_text_primary"),
                                        colors = TextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF161B22),
                                            unfocusedContainerColor = Color(0xFF161B22)
                                        )
                                    )

                                    TextField(
                                        value = textOverlaySecondary,
                                        onValueChange = { viewModel.setOverlaySecondary(it) },
                                        label = { Text("Dòng Chữ 2 (Status description)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("realtime_text_secondary"),
                                        colors = TextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF161B22),
                                            unfocusedContainerColor = Color(0xFF161B22)
                                        )
                                    )

                                    // Position select grid
                                    Text("Vị Trí Hiển Thị:", color = Color.Gray, fontSize = 11.sp)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val positions = listOf("TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT", "CENTER")
                                        positions.forEach { pos ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (overlayPosition == pos) Color(0xFF39FF14) else Color(0xFF21262D))
                                                    .clickable { viewModel.setOverlayPosition(pos) }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = when(pos) {
                                                        "TOP_LEFT" -> "Tr.Trái"
                                                        "TOP_RIGHT" -> "Tr.Phải"
                                                        "BOTTOM_LEFT" -> "D.Trái"
                                                        "BOTTOM_RIGHT" -> "D.Phải"
                                                        else -> "Giữa"
                                                    },
                                                    color = if (overlayPosition == pos) Color.Black else Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Text size slider
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Cỡ chữ: ${textSize}sp", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(90.dp))
                                        Slider(
                                            value = textSize.toFloat(),
                                            onValueChange = { viewModel.setTextSize(it.toInt()) },
                                            valueRange = 10f..30f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(thumbColor = Color(0xFF39FF14), activeTrackColor = Color(0xFF39FF14))
                                        )
                                    }
                                }

                                // 2: Filters and Tuning
                                2 -> {
                                    Text(
                                        "Lựa Chọn Bộ Lọc Camera:",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )

                                    // Grid of filter options
                                    val filters = listOf(
                                        Pair("NONE", "Mặc Định"),
                                        Pair("NIGHT_VISION", "CCTV Xanh"),
                                        Pair("MONOCHROME", "Trắng Đen"),
                                        Pair("INFRARED", "Hồng Ngoại"),
                                        Pair("SEPIA", "Hoài Cổ"),
                                        Pair("VHS", "Nhiễu VHS")
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // First row of filters
                                        filters.take(3).forEach { f ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (filterType == f.first) Color(0xFF39FF14) else Color(0xFF21262D))
                                                    .clickable { viewModel.setFilterType(f.first) }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    f.second,
                                                    color = if (filterType == f.first) Color.Black else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Second row of filters
                                        filters.drop(3).forEach { f ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (filterType == f.first) Color(0xFF39FF14) else Color(0xFF21262D))
                                                    .clickable { viewModel.setFilterType(f.first) }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    f.second,
                                                    color = if (filterType == f.first) Color.Black else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Brightness Adjustment Slider
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Sáng tối: ${String.format("%.1f", brightness)}x", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(90.dp))
                                        Slider(
                                            value = brightness,
                                            onValueChange = { viewModel.setBrightness(it) },
                                            valueRange = 0.5f..1.5f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(thumbColor = Color(0xFF39FF14), activeTrackColor = Color(0xFF39FF14))
                                        )
                                    }
                                }

                                // 3: HUD Viewfinder settings
                                3 -> {
                                    Text(
                                        "Hiển Thị Viewfinder (HUD):",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )

                                    // Horizontal toggle rows
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Timestamp toggle
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (showTimestamp) Color(0x2239FF14) else Color(0xFF21262D))
                                                .clickable { viewModel.setShowTimestamp(!showTimestamp) }
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Settings,
                                                contentDescription = null,
                                                tint = if (showTimestamp) Color(0xFF39FF14) else Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Giờ Tích Tắc", color = Color.White, fontSize = 11.sp)
                                        }

                                        // Grid toggle
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (showGrid) Color(0x2239FF14) else Color(0xFF21262D))
                                                .clickable { viewModel.setShowGrid(!showGrid) }
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.GridOn,
                                                contentDescription = null,
                                                tint = if (showGrid) Color(0xFF39FF14) else Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Khung Lưới 3x3", color = Color.White, fontSize = 11.sp)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Blink REC toggle
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (showRecIndicator) Color(0x2239FF14) else Color(0xFF21262D))
                                                .clickable { viewModel.setShowRecIndicator(!showRecIndicator) }
                                                .padding(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(if (showRecIndicator) Color.Red else Color.Gray, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Chữ REC Nháy", color = Color.White, fontSize = 11.sp)
                                        }

                                        // Battery toggle
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (showBattery) Color(0x2239FF14) else Color(0xFF21262D))
                                                .clickable { viewModel.setShowBattery(!showBattery) }
                                                .padding(8.dp)
                                        ) {
                                            Text("🔋", fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Chỉ Số Pin", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }

                                // 4: Save Preset configuration
                                4 -> {
                                    Text(
                                        "Lưu Cấu Hình Này Thành Preset Mới:",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )

                                    var saveName by remember { mutableStateOf("") }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextField(
                                            value = saveName,
                                            onValueChange = { saveName = it },
                                            placeholder = { Text("Tên Preset (Ví dụ: Cam Trộm)") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("save_current_preset_name"),
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor = Color(0xFF161B22),
                                                unfocusedContainerColor = Color(0xFF161B22)
                                            )
                                        )

                                        Button(
                                            onClick = {
                                                if (saveName.isNotBlank()) {
                                                    viewModel.saveCurrentAsPreset(saveName)
                                                    saveName = ""
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39FF14)),
                                            enabled = saveName.isNotBlank(),
                                            modifier = Modifier.testTag("save_current_preset_action")
                                        ) {
                                            Text("Lưu", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
