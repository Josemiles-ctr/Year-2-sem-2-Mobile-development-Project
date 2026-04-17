// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.dependency.check) apply false
    alias(libs.plugins.ksp) apply false
}

extra["room_version"] = "2.6.1"
