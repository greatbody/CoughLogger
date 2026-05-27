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
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                val w = size.width
                val h = size.height
                val labelPx = 22f               // 底部标签预留高度
                val chartH = h - labelPx
                val n = counts.size             // 24
                val slot = w / n
                val barW = slot * 0.7f
                val gap = slot * 0.3f

                // y 基线
                drawLine(
                    color = axisColor,
                    start = Offset(0f, chartH),
                    end = Offset(w, chartH),
                    strokeWidth = 1f
                )

                val nativePaint = android.graphics.Paint().apply {
                    color = labelColor.toArgb()
                    textSize = 10.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                for (i in 0 until n) {
                    val ratio = counts[i].toFloat() / max
                    val barH = chartH * ratio
                    val x = i * slot + gap / 2
                    val y = chartH - barH
                    if (counts[i] > 0) {
                        drawRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barW, barH)
                        )
                    } else {
                        // 0 值画一个浅基线方便定位
                        drawRect(
                            color = axisColor.copy(alpha = 0.25f),
                            topLeft = Offset(x, chartH - 1f),
                            size = Size(barW, 1f)
                        )
                    }
                    // 每 3 小时一个 x 轴标签：0,3,6,...,21
                    if (i % 3 == 0) {
                        drawContext.canvas.nativeCanvas.drawText(
                            i.toString(),
                            x + barW / 2,
                            h - 4f,
                            nativePaint
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
