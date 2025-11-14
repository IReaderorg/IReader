import org.gradle.api.Project
import org.gradle.kotlin.dsl.TaskContainerScope

// Moko resources tasks removed - using Compose Multiplatform Resources instead
fun TaskContainerScope.registerResources(project: Project) {
    // No longer needed - Compose Multiplatform handles resources automatically
}