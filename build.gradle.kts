// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // OWASP declared here so the version is managed centrally; applied in :app
    alias(libs.plugins.owasp.dependency.check) apply false
    // Checks whether dependency updates are available; run: ./gradlew dependencyUpdates
    alias(libs.plugins.ben.manes.versions)
}

// Filter out non-stable releases (alphas, betas, RCs) from dependency update reports
fun isNonStable(version: String): Boolean {
    val nonStableKeywords = listOf("alpha", "beta", "rc", "cr", "m", "preview", "snapshot")
    return nonStableKeywords.any { version.lowercase().contains(it) }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf { isNonStable(candidate.version) && !isNonStable(currentVersion) }
    outputFormatter = "json,html"
    outputDir = "build/reports/dependency-updates"
}