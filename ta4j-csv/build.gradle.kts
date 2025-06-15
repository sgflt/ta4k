plugins {
    `java-library`
}

description = "CSV support for ta4j"

dependencies {
    api(project(":ta4j-core"))

    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // Logging
    implementation(libs.slf4j.api)

    // CSV parsing  
    implementation(libs.commons.csv)
    implementation(libs.opencsv)
}

