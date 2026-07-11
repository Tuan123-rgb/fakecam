package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CameraPreset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CameraViewModel,
    onLaunchSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets by viewModel.presets.collectAsState()
    val capturedItems = viewModel.capturedPhotos

    var showAddPresetForm by remember { mutableStateOf(false) }
    
    // Custom Preset Form States
    var newPresetName by remember { mutableStateOf("") }
    var selectedSourceType by remember { mutableStateOf("SYNTHETIC") } // "TEMPLATE", "SYNTHETIC"
    var selectedVideoUri by remember { mutableStateOf("radar_scanning") }
    var textPrimary by remember { mutableStateOf("CCTV CAMERA") }
    var textSecondary by remember { mutableStateOf("SECURE FEED") }
    var filterType by remember { mutableStateOf("NONE") }
    var overlayPos by remember { mutableStateOf("TOP_LEFT") }

    val templateVideos = listOf(
        Pair("CCTV Siêu Thị", "https://assets.mixkit.co/videos/preview/mixkit-supermarket-security-camera-footage-40540-large.mp4"),
        Pair("Camera Giao Thông", "https://assets.mixkit.co/videos/preview/mixkit-highway-traffic-in-the-afternoon-41553-large.mp4"),
        Pair("Webcam Văn Phòng", "https://assets.mixkit.co/videos/preview/mixkit-young-woman-working-at-office-with-laptop-42302-large.mp4")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)) // Slate Dark Background
            .padding(bottom = 80.dp), // Space for FAB
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Image / Visual Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_dashboard_banner),
                    contentDescription = "Hệ thống Camera Ảo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0D1117)),
                                startY = 100f
                            )
                        )
                )
                // Title
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "VIRTUAL CAMERA SYSTEM",
                        color = Color(0xFF39FF14), // Cyber Green
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Camera Ảo Thông Minh",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // 2. Action Buttons Quick Access
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onLaunchSimulator,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39FF14)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("quick_launch_button")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Khởi Chạy Ngay", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showAddPresetForm = !showAddPresetForm },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("toggle_add_preset_button")
                ) {
                    Icon(
                        if (showAddPresetForm) Icons.Default.KeyboardArrowUp else Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tạo Preset", color = Color.White)
                }
            }
        }

        // 3. Add Preset Expandable Form
        item {
            AnimatedVisibility(
                visible = showAddPresetForm,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    border = BorderStroke(1.dp, Color(0xFF30363D)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Tạo Preset Mới",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        OutlinedTextField(
                            value = newPresetName,
                            onValueChange = { newPresetName = it },
                            label = { Text("Tên Preset (Ví dụ: CCTV Phòng Khách)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_preset_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF39FF14),
                                unfocusedBorderColor = Color(0xFF30363D)
                            )
                        )

                        // Source Select Type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedSourceType = "SYNTHETIC"
                                    selectedVideoUri = "radar_scanning"
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedSourceType == "SYNTHETIC") Color(0xFF39FF14) else Color(0xFF21262D)
                                )
                            ) {
                                Text(
                                    "Nguồn Radar",
                                    color = if (selectedSourceType == "SYNTHETIC") Color.Black else Color.White,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    selectedSourceType = "TEMPLATE"
                                    selectedVideoUri = templateVideos.first().second
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedSourceType == "TEMPLATE") Color(0xFF39FF14) else Color(0xFF21262D)
                                )
                            ) {
                                Text(
                                    "Video Mẫu",
                                    color = if (selectedSourceType == "TEMPLATE") Color.Black else Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Video Selection dropdown if Template
                        if (selectedSourceType == "TEMPLATE") {
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            var activeTemplateLabel by remember { mutableStateOf(templateVideos.first().first) }

                            ExposedDropdownMenuBox(
                                expanded = dropdownExpanded,
                                onExpandedChange = { dropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = activeTemplateLabel,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Chọn Video Mẫu") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF39FF14),
                                        unfocusedBorderColor = Color(0xFF30363D)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    templateVideos.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item.first) },
                                            onClick = {
                                                activeTemplateLabel = item.first
                                                selectedVideoUri = item.second
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Overlay input fields
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = textPrimary,
                                onValueChange = { textPrimary = it },
                                label = { Text("Dòng Chữ 1") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF39FF14),
                                    unfocusedBorderColor = Color(0xFF30363D)
                                )
                            )

                            OutlinedTextField(
                                value = textSecondary,
                                onValueChange = { textSecondary = it },
                                label = { Text("Dòng Chữ 2") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF39FF14),
                                    unfocusedBorderColor = Color(0xFF30363D)
                                )
                            )
                        }

                        // Filter drop down selection
                        var filterDropdownExpanded by remember { mutableStateOf(false) }
                        val filters = listOf(
                            Pair("NONE", "Không bộ lọc"),
                            Pair("NIGHT_VISION", "Bộ lọc hồng ngoại (Night Vision)"),
                            Pair("MONOCHROME", "Camera Đen Trắng"),
                            Pair("INFRARED", "Chế độ nhiệt (Thermal)"),
                            Pair("SEPIA", "Màu Hoài Cổ (Sepia)"),
                            Pair("VHS", "Nhiễu Sóng VHS")
                        )
                        var activeFilterLabel by remember { mutableStateOf("Không bộ lọc") }

                        ExposedDropdownMenuBox(
                            expanded = filterDropdownExpanded,
                            onExpandedChange = { filterDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = activeFilterLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Bộ Lọc Khởi Đầu") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF39FF14),
                                    unfocusedBorderColor = Color(0xFF30363D)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = filterDropdownExpanded,
                                onDismissRequest = { filterDropdownExpanded = false }
                            ) {
                                filters.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.second) },
                                        onClick = {
                                            filterType = item.first
                                            activeFilterLabel = item.second
                                            filterDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Save Button
                        Button(
                            onClick = {
                                if (newPresetName.isNotBlank()) {
                                    viewModel.saveCurrentAsPreset(newPresetName)
                                    // Reset states
                                    newPresetName = ""
                                    showAddPresetForm = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("save_preset_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39FF14)),
                            enabled = newPresetName.isNotBlank()
                        ) {
                            Text("Lưu Preset Vào Danh Sách", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Presets Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = Color(0xFF39FF14),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cấu Hình Camera Sẵn Có (${presets.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // 5. Presets List Items
        if (presets.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đang tải cấu hình mặc định...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(presets) { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("preset_card_${preset.id}")
                        .clickable {
                            viewModel.loadPreset(preset)
                            onLaunchSimulator()
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (viewModel.activePreset.value?.id == preset.id) Color(0xFF39FF14) else Color(0xFF30363D)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Visual properties row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Source type badge
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF21262D), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when(preset.videoSourceType) {
                                            "SYNTHETIC" -> "Radar Radar"
                                            "TEMPLATE" -> "Video Mẫu"
                                            else -> "Thư Viện"
                                        },
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }

                                // Filter Badge
                                if (preset.filterType != "NONE") {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0x3339FF14), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = preset.filterType,
                                            color = Color(0xFF39FF14),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Overlays Indicator
                                if (preset.textOverlayPrimary.isNotBlank()) {
                                    Icon(
                                        Icons.Default.GridOn,
                                        contentDescription = "Có chữ đè",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    viewModel.loadPreset(preset)
                                    onLaunchSimulator()
                                },
                                modifier = Modifier.testTag("launch_preset_button_${preset.id}")
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Chạy",
                                    tint = Color(0xFF39FF14)
                                )
                            }

                            if (preset.isCustom) {
                                IconButton(
                                    onClick = { viewModel.deletePreset(preset) },
                                    modifier = Modifier.testTag("delete_preset_button_${preset.id}")
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Xóa",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. In-App Reel / Capture Log Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = Color(0xFF39FF14),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lịch Sử Chụp & Ghi Hình (${capturedItems.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // 7. Capture items
        if (capturedItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    border = BorderStroke(1.dp, Color(0xFF21262D))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có ảnh chụp hay video giả lập nào.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Nhấn 'Chạy Camera' để chụp ảnh hoặc quay thử!",
                            color = Color.DarkGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(capturedItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    border = BorderStroke(1.dp, Color(0xFF21262D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Icon based on type
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (item.type == "PHOTO") Color(0x3339FF14) else Color(0x33FF3914),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.type == "PHOTO") Icons.Default.CameraAlt else Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = if (item.type == "PHOTO") Color(0xFF39FF14) else Color(0xFFFF3914),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Nguồn: ${item.presetName} | Lọc: ${item.filterApplied}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Date(item.timestamp)),
                                    color = Color.DarkGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Right side - show length if video, or file format
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (item.type == "PHOTO") "JPG" else "MP4",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (item.type == "VIDEO") {
                                Text(
                                    text = "${item.durationSeconds}s",
                                    color = Color(0xFFFF3914),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
