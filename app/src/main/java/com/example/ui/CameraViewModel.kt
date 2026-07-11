package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CameraPreset
import com.example.data.CameraPresetDao
import com.example.data.PresetDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PresetDatabase.getDatabase(application, viewModelScope)
    private val dao = database.presetDao()

    // Presets list from database
    private val _presets = MutableStateFlow<List<CameraPreset>>(emptyList())
    val presets: StateFlow<List<CameraPreset>> = _presets.asStateFlow()

    // Selected / Active Camera Preset (null if modified from original)
    private val _activePreset = MutableStateFlow<CameraPreset?>(null)
    val activePreset: StateFlow<CameraPreset?> = _activePreset.asStateFlow()

    // Primary Viewfinder Parameters (Real-time Editable)
    private val _presetName = MutableStateFlow("Tùy Chỉnh (Custom)")
    val presetName: StateFlow<String> = _presetName.asStateFlow()

    private val _videoSourceType = MutableStateFlow("SYNTHETIC") // "TEMPLATE", "GALLERY", "SYNTHETIC"
    val videoSourceType: StateFlow<String> = _videoSourceType.asStateFlow()

    private val _videoUri = MutableStateFlow("radar_scanning")
    val videoUri: StateFlow<String> = _videoUri.asStateFlow()

    // Real-time Text Overlay State
    private val _textOverlayPrimary = MutableStateFlow("LIVE VIEW")
    val textOverlayPrimary: StateFlow<String> = _textOverlayPrimary.asStateFlow()

    private val _textOverlaySecondary = MutableStateFlow("SIMULATION MODE")
    val textOverlaySecondary: StateFlow<String> = _textOverlaySecondary.asStateFlow()

    private val _overlayPosition = MutableStateFlow("TOP_LEFT") // "TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT", "CENTER"
    val overlayPosition: StateFlow<String> = _overlayPosition.asStateFlow()

    private val _textOverlayColor = MutableStateFlow("#FFFFFFFF")
    val textOverlayColor: StateFlow<String> = _textOverlayColor.asStateFlow()

    private val _textBgColor = MutableStateFlow("#80000000")
    val textBgColor: StateFlow<String> = _textBgColor.asStateFlow()

    private val _textSize = MutableStateFlow(14)
    val textSize: StateFlow<Int> = _textSize.asStateFlow()

    // HUD Toggles
    private val _showTimestamp = MutableStateFlow(true)
    val showTimestamp: StateFlow<Boolean> = _showTimestamp.asStateFlow()

    private val _showRecIndicator = MutableStateFlow(true)
    val showRecIndicator: StateFlow<Boolean> = _showRecIndicator.asStateFlow()

    private val _showBattery = MutableStateFlow(true)
    val showBattery: StateFlow<Boolean> = _showBattery.asStateFlow()

    private val _showGrid = MutableStateFlow(false)
    val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

    // Live Filters & Tuning sliders
    private val _filterType = MutableStateFlow("NONE") // "NONE", "NIGHT_VISION", "MONOCHROME", "INFRARED", "SEPIA", "VHS"
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    private val _brightness = MutableStateFlow(1.0f) // 0.5f to 1.5f
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _contrast = MutableStateFlow(1.0f) // 0.5f to 1.5f
    val contrast: StateFlow<Float> = _contrast.asStateFlow()

    private val _saturation = MutableStateFlow(1.0f) // 0.0f to 2.0f
    val saturation: StateFlow<Float> = _saturation.asStateFlow()

    // Camera Hardware Toggles (Simulated)
    private val _digitalZoom = MutableStateFlow(1.0f) // 1.0f to 5.0f
    val digitalZoom: StateFlow<Float> = _digitalZoom.asStateFlow()

    private val _exposureCompensation = MutableStateFlow(0.0f) // -2.0f to +2.0f
    val exposureCompensation: StateFlow<Float> = _exposureCompensation.asStateFlow()

    // UI Interactive States
    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordDurationSeconds = MutableStateFlow(0)
    val recordDurationSeconds: StateFlow<Int> = _recordDurationSeconds.asStateFlow()

    // Capture flash visual trigger
    private val _flashTriggerFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val flashTriggerFlow: SharedFlow<Unit> = _flashTriggerFlow.asSharedFlow()

    // Sound effect trigger
    private val _shutterSoundFlow = MutableSharedFlow<String>(extraBufferCapacity = 1) // "SHUTTER" or "RECORD_START" or "RECORD_STOP"
    val shutterSoundFlow: SharedFlow<String> = _shutterSoundFlow.asSharedFlow()

    // Captured media outputs (for the in-app "Photos Taken" collection)
    val capturedPhotos = mutableStateListOf<CapturedItem>()

    // Recording job
    private var recordJob: Job? = null

    init {
        // Observe DB presets
        viewModelScope.launch {
            dao.getAllPresets().collectLatest { list ->
                _presets.value = list
                // Select first preset on launch if current is empty
                if (_activePreset.value == null && list.isNotEmpty()) {
                    loadPreset(list.first())
                }
            }
        }
    }

    fun loadPreset(preset: CameraPreset) {
        _activePreset.value = preset
        _presetName.value = preset.name
        _videoSourceType.value = preset.videoSourceType
        _videoUri.value = preset.videoUri
        _textOverlayPrimary.value = preset.textOverlayPrimary
        _textOverlaySecondary.value = preset.textOverlaySecondary
        _overlayPosition.value = preset.overlayPosition
        _textOverlayColor.value = preset.textOverlayColor
        _textBgColor.value = preset.textBgColor
        _textSize.value = preset.textSize
        _showTimestamp.value = preset.showTimestamp
        _showRecIndicator.value = preset.showRecIndicator
        _showBattery.value = preset.showBattery
        _showGrid.value = preset.showGrid
        _filterType.value = preset.filterType
        _brightness.value = preset.brightness
        _contrast.value = preset.contrast
        _saturation.value = preset.saturation
        _digitalZoom.value = 1.0f
        _exposureCompensation.value = 0.0f
    }

    // Quick toggles & sliders edits
    fun setVideoSource(type: String, uri: String) {
        _videoSourceType.value = type
        _videoUri.value = uri
        _activePreset.value = null // broken preset connection as it is modified
    }

    fun setOverlayPrimary(text: String) {
        _textOverlayPrimary.value = text
        _activePreset.value = null
    }

    fun setOverlaySecondary(text: String) {
        _textOverlaySecondary.value = text
        _activePreset.value = null
    }

    fun setOverlayPosition(pos: String) {
        _overlayPosition.value = pos
    }

    fun setOverlayColors(textHex: String, bgHex: String) {
        _textOverlayColor.value = textHex
        _textBgColor.value = bgHex
    }

    fun setTextSize(sp: Int) {
        _textSize.value = sp
    }

    fun setShowTimestamp(show: Boolean) {
        _showTimestamp.value = show
    }

    fun setShowRecIndicator(show: Boolean) {
        _showRecIndicator.value = show
    }

    fun setShowBattery(show: Boolean) {
        _showBattery.value = show
    }

    fun setShowGrid(show: Boolean) {
        _showGrid.value = show
    }

    fun setFilterType(filter: String) {
        _filterType.value = filter
    }

    fun setBrightness(b: Float) {
        _brightness.value = b
    }

    fun setContrast(c: Float) {
        _contrast.value = c
    }

    fun setSaturation(s: Float) {
        _saturation.value = s
    }

    fun setDigitalZoom(z: Float) {
        _digitalZoom.value = z
    }

    fun setExposureCompensation(e: Float) {
        _exposureCompensation.value = e
    }

    fun togglePlaying() {
        _isPlaying.value = !_isPlaying.value
    }

    // Trigger Photo Capture
    fun capturePhoto() {
        viewModelScope.launch {
            _flashTriggerFlow.emit(Unit)
            _shutterSoundFlow.emit("SHUTTER")
            
            // Generate a simulated capture record
            val timestamp = System.currentTimeMillis()
            val newItem = CapturedItem(
                id = timestamp,
                title = "FakeCam_${timestamp}",
                timestamp = timestamp,
                type = "PHOTO",
                presetName = _presetName.value,
                filterApplied = _filterType.value
            )
            capturedPhotos.add(0, newItem)
        }
    }

    // Trigger Video Simulation Recording
    fun toggleRecording() {
        if (_isRecording.value) {
            // Stop recording
            _isRecording.value = false
            recordJob?.cancel()
            viewModelScope.launch {
                _shutterSoundFlow.emit("RECORD_STOP")
                val timestamp = System.currentTimeMillis()
                val duration = _recordDurationSeconds.value
                val newItem = CapturedItem(
                    id = timestamp,
                    title = "FakeRec_${timestamp}",
                    timestamp = timestamp,
                    type = "VIDEO",
                    presetName = _presetName.value,
                    filterApplied = _filterType.value,
                    durationSeconds = duration
                )
                capturedPhotos.add(0, newItem)
            }
        } else {
            // Start recording
            _isRecording.value = true
            _recordDurationSeconds.value = 0
            viewModelScope.launch {
                _shutterSoundFlow.emit("RECORD_START")
            }
            recordJob = viewModelScope.launch {
                while (_isRecording.value) {
                    delay(1000)
                    _recordDurationSeconds.value += 1
                }
            }
        }
    }

    // Save active configuration as a new Camera Preset in Room
    fun saveCurrentAsPreset(customName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newPreset = CameraPreset(
                name = customName,
                videoSourceType = _videoSourceType.value,
                videoUri = _videoUri.value,
                textOverlayPrimary = _textOverlayPrimary.value,
                textOverlaySecondary = _textOverlaySecondary.value,
                overlayPosition = _overlayPosition.value,
                textOverlayColor = _textOverlayColor.value,
                textBgColor = _textBgColor.value,
                textSize = _textSize.value,
                showTimestamp = _showTimestamp.value,
                showRecIndicator = _showRecIndicator.value,
                showBattery = _showBattery.value,
                showGrid = _showGrid.value,
                filterType = _filterType.value,
                brightness = _brightness.value,
                contrast = _contrast.value,
                saturation = _saturation.value,
                isCustom = true
            )
            val id = dao.insertPreset(newPreset)
            // Reload with the saved preset
            val reloaded = newPreset.copy(id = id.toInt())
            _activePreset.value = reloaded
            _presetName.value = reloaded.name
        }
    }

    fun deletePreset(preset: CameraPreset) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deletePresetById(preset.id)
            if (_activePreset.value?.id == preset.id) {
                _activePreset.value = null
            }
        }
    }
}

// Data class to represent captured photos/videos inside our app's gallery list
data class CapturedItem(
    val id: Long,
    val title: String,
    val timestamp: Long,
    val type: String, // "PHOTO", "VIDEO"
    val presetName: String,
    val filterApplied: String,
    val durationSeconds: Int = 0
)

class CameraViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
