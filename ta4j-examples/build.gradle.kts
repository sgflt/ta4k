plugins {
    application
}

description = "Examples for ta4j"

dependencies {
    implementation(project(":ta4j-core"))
    implementation(project(":ta4j-csv"))

    // JSON serialization for examples
    implementation(libs.gson)
}

