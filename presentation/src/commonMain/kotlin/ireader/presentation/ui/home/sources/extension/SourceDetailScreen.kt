package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.home.sources.extension.composables.LetterIcon
import ireader.presentation.imageloader.IImageLoader
import java.util.*

data class SourceDetailScreen(
    val catalog: Catalog
) : VoyagerScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        IScaffold(
            topBar = {
                TopAppBar(
                    title = { Text(localize(MR.strings.source)) },
                    navigationIcon = {
                        AppIconButton(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = localize(MR.strings.go_back),
                            onClick = { navigator.pop() }
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            SourceDetailContent(
                catalog = catalog,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun SourceDetailContent(
    catalog: Catalog,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Source Icon and Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (catalog) {
                is CatalogLocal -> {
                    IImageLoader(
                        model = catalog,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp)
                    )
                }
                else -> {
                    LetterIcon(
                        text = catalog.name,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Column {
                Text(
                    text = catalog.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (catalog is CatalogInstalled) {
                    Text(
                        text = "v${catalog.versionName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider()

        // Language
        DetailItem(
            label = localize(MR.strings.language),
            value = when (catalog) {
                is CatalogInstalled -> catalog.source?.lang?.uppercase(Locale.getDefault()) ?: localize(MR.strings.unknown)
                else -> localize(MR.strings.unknown)
            }
        )

        // Description
        val description = when (catalog) {
            is CatalogInstalled -> catalog.source?.let { "Source: ${it.name}" } ?: "No description available"
            else -> "No description available"
        }
        DetailItem(
            label = "Description",
            value = description
        )

        // Status
        DetailItem(
            label = localize(MR.strings.status),
            value = when (catalog) {
                is CatalogInstalled -> localize(MR.strings.installed)
                else -> localize(MR.strings.available)
            }
        )

        // Package Name (for installed sources)
        if (catalog is CatalogInstalled) {
            DetailItem(
                label = "Package Name",
                value = catalog.pkgName
            )
        }

        HorizontalDivider()

        // Report as Broken Button
        Button(
            onClick = { /* TODO: Implement report functionality */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Report as Broken")
        }

        // Statistics Section (if available)
        if (catalog is CatalogInstalled) {
            HorizontalDivider()
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // You can add more statistics here as needed
            DetailItem(
                label = "Version Code",
                value = catalog.versionCode.toString()
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
