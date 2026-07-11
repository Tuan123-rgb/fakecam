package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SyntheticFeedView(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "synthetic_feed")
    
    // Rotating sweep angle for the radar
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    // Moving scan line position
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    // Pulsing circles scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    // Interactive waveform phase shifting
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07110C)) // Deep dark cyber background
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2
            val maxRadius = Math.min(width, height) * 0.45f

            // 1. Draw Tech Grid background
            val gridSpacing = 60f
            val gridColor = Color(0x0A39FF14) // Very faint cyber green
            
            // Vertical grid lines
            var x = 0f
            while (x < width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }
            
            // Horizontal grid lines
            var y = 0f
            while (y < height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            // 2. Draw Concentric Target Circles
            val radarColor = Color(0x3339FF14) // Neon green with alpha
            drawCircle(
                color = radarColor,
                radius = maxRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = radarColor,
                radius = maxRadius * 0.66f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = radarColor,
                radius = maxRadius * 0.33f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1f)
            )
            
            // Pulsing target expanding outward
            drawCircle(
                color = Color(0x1139FF14),
                radius = maxRadius * pulseScale,
                center = Offset(centerX, centerY)
            )

            // Crosshair lines
            drawLine(
                color = radarColor,
                start = Offset(centerX - maxRadius - 20f, centerY),
                end = Offset(centerX + maxRadius + 20f, centerY),
                strokeWidth = 1.5f
            )
            drawLine(
                color = radarColor,
                start = Offset(centerX, centerY - maxRadius - 20f),
                end = Offset(centerX, centerY + maxRadius + 20f),
                strokeWidth = 1.5f
            )

            // 3. Draw Radar Sweeper (Rotating Cone)
            val angleRad = Math.toRadians(radarSweepAngle.toDouble()).toFloat()
            val sweepX = centerX + maxRadius * cos(angleRad)
            val sweepY = centerY + maxRadius * sin(angleRad)
            
            // Draw sweeping line
            drawLine(
                color = Color(0xFF39FF14), // Bright neon green laser
                start = Offset(centerX, centerY),
                end = Offset(sweepX, sweepY),
                strokeWidth = 3f
            )

            // 4. Draw Interactive Sine Wave Scope (at the bottom)
            val wavePath = Path()
            val waveY = centerY + maxRadius + 100f
            val waveWidth = maxRadius * 1.5f
            val waveStart = centerX - waveWidth / 2
            
            if (waveY < height - 50f) {
                wavePath.moveTo(waveStart, waveY)
                for (pixelX in 0..waveWidth.toInt() step 5) {
                    val progress = pixelX / waveWidth
                    // Dampen the waves at both ends (gaussian envelope)
                    val envelope = sin(progress * Math.PI).toFloat()
                    val theta = progress * 6 * Math.PI + wavePhase
                    val offsetVal = sin(theta).toFloat() * 40f * envelope
                    
                    wavePath.lineTo(waveStart + pixelX, waveY + offsetVal)
                }
                drawPath(
                    path = wavePath,
                    color = Color(0xAA39FF14),
                    style = Stroke(width = 2.5f)
                )
            }

            // 5. Draw Scanning Laser Line (Gliding up and down)
            val laserY = scanLineY * height
            drawLine(
                color = Color(0x88FF3914), // Bright neon orange-red laser line
                start = Offset(0f, laserY),
                end = Offset(width, laserY),
                strokeWidth = 3f
            )
            // Soft laser glow under it
            drawRect(
                color = Color(0x15FF3914),
                topLeft = Offset(0f, laserY - 15f),
                size = Size(width, 30f)
            )

            // 6. Draw "TARGET LOCKED" indicators
            if (isPlaying) {
                // Outer corners of the viewfinder target
                val offset = maxRadius + 50f
                val cornerSize = 40f
                
                val corners = listOf(
                    // Top-Left
                    Pair(Offset(centerX - offset, centerY - offset), Pair(Offset(1f, 0f), Offset(0f, 1f))),
                    // Top-Right
                    Pair(Offset(centerX + offset, centerY - offset), Pair(Offset(-1f, 0f), Offset(0f, 1f))),
                    // Bottom-Left
                    Pair(Offset(centerX - offset, centerY + offset), Pair(Offset(1f, 0f), Offset(0f, -1f))),
                    // Bottom-Right
                    Pair(Offset(centerX + offset, centerY + offset), Pair(Offset(-1f, 0f), Offset(0f, -1f)))
                )

                for (corner in corners) {
                    val pt = corner.first
                    val dirs = corner.second
                    // Draw horizontal segment
                    drawLine(
                        color = Color(0xFF39FF14),
                        start = pt,
                        end = Offset(pt.x + dirs.first.x * cornerSize, pt.y),
                        strokeWidth = 4f
                    )
                    // Draw vertical segment
                    drawLine(
                        color = Color(0xFF39FF14),
                        start = pt,
                        end = Offset(pt.x, pt.y + dirs.second.y * cornerSize),
                        strokeWidth = 4f
                    )
                }
            }
        }
    }
}
