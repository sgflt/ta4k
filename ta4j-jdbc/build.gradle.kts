import jdk.tools.jlink.resources.plugins

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

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "junit-vintage")
    }

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }

    // JVM args for tests
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

