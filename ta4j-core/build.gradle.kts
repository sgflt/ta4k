plugins {
    id("me.champeau.jmh") version "0.7.2"
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
    
    // JMH for performance testing
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
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
        includeEngines("junit-jupiter")
    }

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }

    // JVM args for tests
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

// JMH configuration
jmh {
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(1)
    timeUnit.set("ms")
    includes.set(listOf(".*LiveTradingPerformanceTest.*"))
    // Run JMH tests from test source set
    duplicateClassesStrategy.set(DuplicatesStrategy.WARN)
    jvmArgs.set(listOf(
        "-Dlogback.configurationFile=logback-test.xml",
        "-Dorg.slf4j.simpleLogger.defaultLogLevel=off",
        "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener"
    ))
}

