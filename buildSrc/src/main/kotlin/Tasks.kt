import org.gradle.api.Project
import org.gradle.kotlin.dsl.TaskContainerScope
import java.io.File
private const val runResourceTasks = "generateResources"
fun TaskContainerScope.registerResources(project: Project) {
//    with(project) {
//        register(runResourceTasks) {
//            this.mustRunAfter("generateMRjvmMain")
//            val parentFile = project.parent!!.projectDir
//            if(File(parentFile, "i18n/build/generated/moko-resources/jvmMain/res/").exists()) {
//                val desktopRes = File(parentFile, "i18n/build/generated/moko-resources/jvmMain/res/")
//                val commonRes = File(parentFile, "i18n/src/commonMain/moko-resources/drawable/")
//                val dest = File(parentFile, "desktop/build/resources/main/")
//
//                commonRes.copyRecursively(File(dest, "drawable/"), true)
//                desktopRes.copyRecursively(dest, true)
//
//            }
//        }
//        named("jvmProcessResources") {
//            dependsOn(runResourceTasks)
//        }
//
//    }
}