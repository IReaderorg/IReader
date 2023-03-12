import org.gradle.api.Project
import org.gradle.kotlin.dsl.TaskContainerScope
import java.io.File
private const val runResourceTasks = "generateResources"
fun TaskContainerScope.registerResources(project: Project) {
    with(project) {
        register(runResourceTasks) {
            this.mustRunAfter("generateMRdesktopMain")
            val parentFile = project.parent!!.projectDir
            val desktopRes = File(parentFile, "i18n/build/generated/moko/desktopMain/ireaderi18nresources/res/")
            val commonRes = File(parentFile, "i18n/src/commonMain/resources/drawable/")
            val dest = File(parentFile, "desktop/build/resources/main/")

                commonRes.copyRecursively(File(dest, "drawable/"), true)
                desktopRes.copyRecursively(dest, true)

        }
        named("desktopProcessResources") {
            dependsOn(runResourceTasks)
        }

    }
}