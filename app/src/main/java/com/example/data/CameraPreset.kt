package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "camera_presets")
data class CameraPreset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val videoSourceType: String, // "TEMPLATE", "GALLERY", "SYNTHETIC"
    val videoUri: String, // Can be online URL, local File Uri, or synthetic id
    val textOverlayPrimary: String = "",
    val textOverlaySecondary: String = "",
    val overlayPosition: String = "TOP_LEFT", // "TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT", "CENTER"
    val textOverlayColor: String = "#FFFFFFFF", // Hex color (White)
    val textBgColor: String = "#80000000", // Hex color (Semi-transparent black)
    val textSize: Int = 14, // in sp
    val showTimestamp: Boolean = true,
    val showRecIndicator: Boolean = true,
    val showBattery: Boolean = true,
    val showGrid: Boolean = false,
    val filterType: String = "NONE", // "NONE", "NIGHT_VISION", "MONOCHROME", "INFRARED", "SEPIA", "VHS"
    val brightness: Float = 1.0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val timestampFormat: String = "yyyy-MM-dd HH:mm:ss",
    val isCustom: Boolean = true
)

@Dao
interface CameraPresetDao {
    @Query("SELECT * FROM camera_presets ORDER BY isCustom ASC, id DESC")
    fun getAllPresets(): Flow<List<CameraPreset>>

    @Query("SELECT * FROM camera_presets WHERE id = :id")
    suspend fun getPresetById(id: Int): CameraPreset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CameraPreset): Long

    @Query("DELETE FROM camera_presets WHERE id = :id")
    suspend fun deletePresetById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<CameraPreset>)
}

@Database(entities = [CameraPreset::class], version = 1, exportSchema = false)
abstract class PresetDatabase : RoomDatabase() {
    abstract fun presetDao(): CameraPresetDao

    companion object {
        @Volatile
        private var INSTANCE: PresetDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PresetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PresetDatabase::class.java,
                    "preset_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        prepopulateDatabase(database.presetDao())
                    }
                }
            }

            suspend fun prepopulateDatabase(dao: CameraPresetDao) {
                val defaultPresets = listOf(
                    CameraPreset(
                        name = "CCTV Siêu Thị (Store CCTV)",
                        videoSourceType = "TEMPLATE",
                        videoUri = "https://assets.mixkit.co/videos/preview/mixkit-supermarket-security-camera-footage-40540-large.mp4",
                        textOverlayPrimary = "CCTV CH-04 | CASH REGISTER",
                        textOverlaySecondary = "CAM FEEDS INC. - SECURITY SYSTEM",
                        overlayPosition = "TOP_LEFT",
                        filterType = "MONOCHROME",
                        showTimestamp = true,
                        showRecIndicator = true,
                        showBattery = false,
                        showGrid = true,
                        isCustom = false
                    ),
                    CameraPreset(
                        name = "Camera Hành Trình (Dashcam Front)",
                        videoSourceType = "TEMPLATE",
                        videoUri = "https://assets.mixkit.co/videos/preview/mixkit-highway-traffic-in-the-afternoon-41553-large.mp4",
                        textOverlayPrimary = "DASH_CAM FRONT VIEW [4K]",
                        textOverlaySecondary = "SPEED: 72 KM/H | GPS LOCK",
                        overlayPosition = "BOTTOM_LEFT",
                        filterType = "NONE",
                        showTimestamp = true,
                        showRecIndicator = true,
                        showBattery = true,
                        showGrid = false,
                        isCustom = false
                    ),
                    CameraPreset(
                        name = "Phòng Họp Văn Phòng (Office Camera)",
                        videoSourceType = "TEMPLATE",
                        videoUri = "https://assets.mixkit.co/videos/preview/mixkit-young-woman-working-at-office-with-laptop-42302-large.mp4",
                        textOverlayPrimary = "MEETING ROOM A - WEBCAM",
                        textOverlaySecondary = "STATUS: ACTIVE SESSION",
                        overlayPosition = "TOP_RIGHT",
                        filterType = "NONE",
                        showTimestamp = false,
                        showRecIndicator = false,
                        showBattery = false,
                        showGrid = false,
                        brightness = 1.05f,
                        contrast = 1.05f,
                        isCustom = false
                    ),
                    CameraPreset(
                        name = "Hệ Thống Quét Radar (Synthetic Radar Scanner)",
                        videoSourceType = "SYNTHETIC",
                        videoUri = "radar_scanning",
                        textOverlayPrimary = "SYS SCANNER: LOCK-ON",
                        textOverlaySecondary = "TARGET DIST: 2.45 KM | SCANNING...",
                        overlayPosition = "TOP_LEFT",
                        filterType = "NIGHT_VISION",
                        showTimestamp = true,
                        showRecIndicator = true,
                        showBattery = true,
                        showGrid = true,
                        isCustom = false
                    )
                )
                dao.insertPresets(defaultPresets)
            }
        }
    }
}
