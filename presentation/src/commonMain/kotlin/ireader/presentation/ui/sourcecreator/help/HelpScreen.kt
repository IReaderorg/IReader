package ireader.presentation.ui.sourcecreator.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.usersource.help.SourceCreatorHelp

/**
 * Help and tutorial screen for source creation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    onStartTutorial: () -> Unit
) {
    var selectedTopic by remember { mutableStateOf<SourceCreatorHelp.HelpTopic?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedTopic?.title ?: "Help & Tutorials") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTopic != null) {
                            selectedTopic = null
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (selectedTopic != null) {
            HelpTopicDetail(
                topic = selectedTopic!!,
                onRelatedTopicClick = { topicId ->
                    selectedTopic = SourceCreatorHelp.getTopicById(topicId)
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            HelpTopicsList(
                onTopicClick = { selectedTopic = it },
                onStartTutorial = onStartTutorial,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun HelpTopicsList(
    onTopicClick: (SourceCreatorHelp.HelpTopic) -> Unit,
    onStartTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Interactive tutorial card
        item {
            Card(
                onClick = onStartTutorial,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Tutorial",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Step-by-step guide to create your first source",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }
        }
        
        // Quick tips
        item {
            QuickTipsCard()
        }
        
        // Help topics
        item {
            Text(
                text = "Help Topics",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(SourceCreatorHelp.allTopics) { topic ->
            HelpTopicCard(
                topic = topic,
                onClick = { onTopicClick(topic) }
            )
        }
    }
}

@Composable
private fun QuickTipsCard() {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Quick Tips",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    SourceCreatorHelp.quickTips.forEach { tip ->
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpTopicCard(
    topic: SourceCreatorHelp.HelpTopic,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = topic.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun HelpTopicDetail(
    topic: SourceCreatorHelp.HelpTopic,
    onRelatedTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Content
        item {
            Text(
                text = topic.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Examples
        if (topic.examples.isNotEmpty()) {
            item {
                Text(
                    text = "Examples",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(topic.examples) { example ->
                ExampleCard(example = example)
            }
        }
        
        // Tips
        if (topic.tips.isNotEmpty()) {
            item {
                TipsCard(tips = topic.tips)
            }
        }
        
        // Related topics
        if (topic.relatedTopics.isNotEmpty()) {
            item {
                Text(
                    text = "Related Topics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(topic.relatedTopics) { topicId ->
                val relatedTopic = SourceCreatorHelp.getTopicById(topicId)
                if (relatedTopic != null) {
                    Card(
                        onClick = { onRelatedTopicClick(topicId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = relatedTopic.title,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExampleCard(example: SourceCreatorHelp.HelpExample) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = example.description,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = example.code,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.padding(8.dp)
                )
            }
            if (example.result != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "→ ${example.result}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TipsCard(tips: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tips",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            tips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
