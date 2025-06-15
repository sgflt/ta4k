plugins {
    `java-library`
}

description = "JDBC support for ta4j"

dependencies {
    api(project(":ta4j-core"))

    // Logging
    implementation(libs.slf4j.api)

    // Lombok for annotations
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Test dependencies
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

