package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.js.models.PluginPerformanceMetrics
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Dashboard showing performance metrics for JavaScript plugins.
 */
@Composable
fun JSPluginPerformanceDashboard(
    metrics: Map<String, PluginPerformanceMetrics>,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Cache the entries list to avoid recreation on each recomposition
    val metricsList = remember(metrics) { metrics.entries.toList() }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = localizeHelper.localize(Res.string.plugin_performance_metrics),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        if (metricsList.isEmpty()) {
            item {
                Text(
                    text = localizeHelper.localize(Res.string.no_performance_data_available_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(metricsList) { (pluginId, metric) ->
                PluginMetricsCard(pluginId, metric)
            }
        }
    }
}

@Composable
private fun PluginMetricsCard(
    pluginId: String,
    metrics: PluginPerformanceMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = pluginId,
                style = MaterialTheme.typography.titleMedium
            )
            
            Divider()
            
            MetricRow("Load Time", "${metrics.loadTime}ms")
            MetricRow("Avg Execution", "${metrics.avgExecutionTime}ms")
            MetricRow("Max Execution", "${metrics.maxExecutionTime}ms")
            MetricRow("Total Calls", "${metrics.totalCalls}")
            MetricRow("Failed Calls", "${metrics.failedCalls}")
            MetricRow("Error Rate", "${(metrics.errorRate * 100).toInt()}%")
            MetricRow("Memory Usage", formatBytes(metrics.memoryUsage))
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
