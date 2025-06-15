plugins {
    `java-library`
}

description = "ta4j is a Java library providing a simple API for technical analysis."

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // Logging
    implementation(libs.slf4j.api)

    // Project lombok (optional)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Test dependencies
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.vintage)
    testImplementation(libs.assertj.core)
    testImplementation(libs.logback.classic)
    testImplementation(libs.mockk)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    // Test-only dependencies
    testImplementation(libs.commons.math3)
    testImplementation(libs.poi)
}

// Configure source sets for mixed Java/Kotlin sources
sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
        kotlin {
            srcDirs("src/main/java")
        }
    }
    test {
        java {
            srcDirs("src/test/java")
        }
        kotlin {
            srcDirs("src/test/java")
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Ta4j Organization"
        )
    }
}

// Configure test logging
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

