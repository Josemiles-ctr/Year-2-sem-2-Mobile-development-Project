package com.example.mobiledev.feature.hospital.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobiledev.ui.components.GlassyCard

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Analytics & Reports",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        val stats = listOf(
            ReportStat("Total Requests", "124", Icons.Default.Assignment, Color(0xFF2196F3)),
            ReportStat("Completed", "98", Icons.Default.DoneAll, Color(0xFF4CAF50)),
            ReportStat("Avg Response", "12m", Icons.Default.TrendingUp, Color(0xFFFF9800)),
            ReportStat("Efficiency", "86%", Icons.Default.BarChart, Color(0xFF9C27B0))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(stats) { stat ->
                StatCard(stat)
            }
        }
    }
}

@Composable
private fun StatCard(stat: ReportStat) {
    GlassyCard(
        modifier = Modifier.height(140.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        border = null
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = stat.color
            )
            Column {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class ReportStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)
