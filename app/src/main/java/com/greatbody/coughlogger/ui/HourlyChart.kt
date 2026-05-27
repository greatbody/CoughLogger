package com.greatbody.coughlogger.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 24 小时咳嗽频次柱状图。
 * 输入 counts.size == 24；显示 0–23 时段；自动按最大值归一。
 */
@Composable
fun HourlyChart(
    counts: IntArray,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    axisColor: Color = MaterialTheme.colorScheme.outline,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val total = counts.sum()
    val max = (counts.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(modifier = modifier) {
        Text(
            text = "全天合计：$total 次  ·  峰值：$max 次/时",
            fontSize = 12.sp,
            color = labelColor,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val w = size.width
                val h = size.height
                val countTextPx = 9.sp.toPx()
                val axisTextPx = 10.sp.toPx()
                val topPx = countTextPx + 6f    // 顶部预留：柱顶数字（字号+余量）
                val labelPx = axisTextPx + 14f  // 底部预留：x 轴标签
                val chartH = h - labelPx - topPx
                val n = counts.size             // 24
                val slot = w / n
                val barW = slot * 0.7f
                val gap = slot * 0.3f
                val baselineY = topPx + chartH

                // y 基线
                drawLine(
                    color = axisColor,
                    start = Offset(0f, baselineY),
                    end = Offset(w, baselineY),
                    strokeWidth = 1f
                )

                val axisPaint = android.graphics.Paint().apply {
                    color = labelColor.toArgb()
                    textSize = axisTextPx
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val countPaint = android.graphics.Paint().apply {
                    color = barColor.toArgb()
                    textSize = countTextPx
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }

                for (i in 0 until n) {
                    val ratio = counts[i].toFloat() / max
                    val barH = chartH * ratio
                    val x = i * slot + gap / 2
                    val y = baselineY - barH
                    if (counts[i] > 0) {
                        drawRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barW, barH)
                        )
                        // 柱顶数字（baseline 始终在柱顶上方 2px，topPx 保证不出界）
                        drawContext.canvas.nativeCanvas.drawText(
                            counts[i].toString(),
                            x + barW / 2,
                            y - 2f,
                            countPaint
                        )
                    } else {
                        drawRect(
                            color = axisColor.copy(alpha = 0.25f),
                            topLeft = Offset(x, baselineY - 1f),
                            size = Size(barW, 1f)
                        )
                    }
                    // 每 3 小时一个 x 轴标签：0,3,6,...,21
                    if (i % 3 == 0) {
                        drawContext.canvas.nativeCanvas.drawText(
                            i.toString(),
                            x + barW / 2,
                            h - 6f,
                            axisPaint
                        )
                    }
                }
            }
        }
    }
}

private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt()
)
