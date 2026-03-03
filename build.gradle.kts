// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // OWASP declared here so the version is managed centrally; applied in :app
    alias(libs.plugins.owasp.dependency.check) apply false
}