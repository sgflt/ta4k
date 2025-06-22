plugins {
    application
}

description = "Examples for ta4j"

dependencies {
    implementation(project(":ta4j-core"))
    implementation(project(":ta4j-csv"))

    // JSON serialization for examples
    implementation(libs.gson)
    
    // Test dependencies
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
}

// Configure test
tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

