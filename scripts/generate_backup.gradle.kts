// Add this to domain/build.gradle.kts to create a task that generates test backups
// Or run as a standalone script

import org.gradle.api.tasks.JavaExec

tasks.register<JavaExec>("generateTestBackup") {
    group = "verification"
    description = "Generates a test backup file with many books"
    
    classpath = sourceSets["desktopMain"].runtimeClasspath
    mainClass.set("ireader.domain.usecases.backup.GenerateTestBackupKt")
}
