package org.ireader.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Configuration activity for library widget
 */
class LibraryWidgetConfigActivity : ComponentActivity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the result to CANCELED initially
        setResult(Activity.RESULT_CANCELED)
        
        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        // If the widget ID is invalid, finish the activity
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            MaterialTheme {
                WidgetConfigScreen(
                    onConfigSaved = { config ->
                        saveWidgetConfig(config)
                        finishWithSuccess()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
    
    private fun saveWidgetConfig(config: String) {
        LibraryWidgetConfigManager.saveWidgetConfig(this, appWidgetId, config)
        
        // Update the widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        LibraryWidget.updateAppWidget(this, appWidgetManager, appWidgetId)
    }
    
    private fun finishWithSuccess() {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

@Composable
fun WidgetConfigScreen(
    onConfigSaved: (String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedType by remember { mutableStateOf("updates_grid") }
    var maxItems by remember { mutableStateOf(10) }
    var showCover by remember { mutableStateOf(true) }
    var showTitle by remember { mutableStateOf(true) }
    var showUnreadCount by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configure Library Widget",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Widget type selection
        Text(
            text = "Widget Type",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Type options
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            RadioButtonOption(
                text = "Updates Grid",
                selected = selectedType == "updates_grid",
                onClick = { selectedType = "updates_grid" }
            )
            RadioButtonOption(
                text = "Reading List",
                selected = selectedType == "reading_list",
                onClick = { selectedType = "reading_list" }
            )
            RadioButtonOption(
                text = "Favorites",
                selected = selectedType = "favorites",
                onClick = { selectedType = "favorites" }
            )
            RadioButtonOption(
                text = "Statistics",
                selected = selectedType == "statistics",
                onClick = { selectedType = "statistics" }
            )
        }
        
        // Display options
        Text(
            text = "Display Options",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        SwitchOption(
            text = "Show Cover",
            checked = showCover,
            onCheckedChange = { showCover = it }
        )
        
        SwitchOption(
            text = "Show Title",
            checked = showTitle,
            onCheckedChange = { showTitle = it }
        )
        
        SwitchOption(
            text = "Show Unread Count",
            checked = showUnreadCount,
            onCheckedChange = { showUnreadCount = it }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    // Create config JSON
                    val config = """
                        {
                            "type": "$selectedType",
                            "maxItems": $maxItems,
                            "showCover": $showCover,
                            "showTitle": $showTitle,
                            "showUnreadCount": $showUnreadCount
                        }
                    """.trimIndent()
                    onConfigSaved(config)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun RadioButtonOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SwitchOption(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
