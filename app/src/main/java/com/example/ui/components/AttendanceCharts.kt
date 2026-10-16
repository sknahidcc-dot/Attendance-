package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimeSlotStat
import com.example.ui.theme.*

@Composable
fun SimpleBarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    maxHeightDp: Int = 180
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.height(100.dp), contentAlignment = Alignment.Center) {
            Text("কোনো ডেটা পাওয়া যায়নি", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxHeightDp.dp)
            ) {
                val barCount = data.size
                val spaceBetween = 14.dp.toPx()
                val totalSpaces = (barCount - 1).coerceAtLeast(1) * spaceBetween
                val barWidth = ((size.width - totalSpaces) / barCount).coerceIn(16.dp.toPx(), 44.dp.toPx())

                val bottomY = size.height - 24.dp.toPx()
                val availableHeight = bottomY - 20.dp.toPx()

                data.forEachIndexed { index, (label, value) ->
                    val barHeight = (value.toFloat() / maxValue) * availableHeight
                    val x = index * (barWidth + spaceBetween)
                    val y = bottomY - barHeight

                    val isPeak = value == maxValue && value > 0

                    drawRoundRect(
                        brush = if (isPeak) {
                            VioletRedGradient
                        } else {
                            Brush.verticalGradient(listOf(RoyalPurpleLight, RoyalPurple))
                        },
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight.coerceAtLeast(6.dp.toPx())),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    if (value > 0) {
                        drawContext.canvas.nativeCanvas.drawText(
                            value.toString(),
                            x + barWidth / 2,
                            y - 8f,
                            android.graphics.Paint().apply {
                                color = if (isPeak) android.graphics.Color.parseColor("#DC2626") else android.graphics.Color.parseColor("#7C3AED")
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x + barWidth / 2,
                        size.height - 4f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#64748B")
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyHeatmapChart(
    stats: List<TimeSlotStat>,
    modifier: Modifier = Modifier
) {
    val morningCount = stats.filter { it.period == "সকাল" }.sumOf { it.count }
    val afternoonCount = stats.filter { it.period == "দুপুর" }.sumOf { it.count }
    val eveningCount = stats.filter { it.period == "সন্ধ্যা" }.sumOf { it.count }
    val total = (morningCount + afternoonCount + eveningCount).coerceAtLeast(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "লাইব্রেরিতে প্রবেশের সময় অনুপাত (সকাল, দুপুর ও সন্ধ্যা)",
                style = MaterialTheme.typography.titleMedium,
                color = TextDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PeriodSummaryBox(
                    title = "সকাল (6 AM - 12 PM)",
                    count = morningCount,
                    percent = (morningCount * 100 / total),
                    accentColor = RoyalPurpleDark,
                    bgColor = RoyalPurpleBg,
                    modifier = Modifier.weight(1f)
                )
                PeriodSummaryBox(
                    title = "দুপুর (12 PM - 5 PM)",
                    count = afternoonCount,
                    percent = (afternoonCount * 100 / total),
                    accentColor = InfoBlue,
                    bgColor = InfoBlueBg,
                    modifier = Modifier.weight(1f)
                )
                PeriodSummaryBox(
                    title = "সন্ধ্যা (5 PM - 10 PM)",
                    count = eveningCount,
                    percent = (eveningCount * 100 / total),
                    accentColor = RubyRed,
                    bgColor = RubyRedBg,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "প্রতি ঘণ্টার ভিত্তিতে শিক্ষার্থী উপস্থিতির হিটম্যাপ",
                style = MaterialTheme.typography.titleMedium,
                color = TextDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val maxHourCount = stats.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stats.forEach { slot ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = slot.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkSecondary,
                            modifier = Modifier.width(55.dp),
                            fontWeight = FontWeight.Medium
                        )

                        val ratio = slot.count.toFloat() / maxHourCount.toFloat()
                        val barColor = when {
                            ratio >= 0.7f -> RubyRed
                            ratio >= 0.35f -> RoyalPurple
                            slot.count > 0 -> RoyalPurpleLight
                            else -> BorderLight
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(18.dp)
                                .background(SurfaceLight, RoundedCornerShape(6.dp))
                        ) {
                            if (slot.count > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(ratio.coerceIn(0.08f, 1f))
                                        .background(barColor, RoundedCornerShape(6.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${slot.count} জন",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (slot.count > 0) TextDark else TextLightMuted,
                            modifier = Modifier.width(45.dp),
                            fontWeight = if (slot.count > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSummaryBox(
    title: String,
    count: Int,
    percent: Int,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextDarkSecondary,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$count জন",
            style = MaterialTheme.typography.titleLarge,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
