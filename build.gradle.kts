plugins {
    kotlin("jvm") version "1.5.10"
    id("java-test-fixtures")
}

group = "io.plurex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

val kotlinStdlibVersion: String by project
val kotlinSerializationVersion: String by project
val kotlinVersion: String by project
val kotlinXVersion: String by project
val slf4jVersion: String by project
val jodaVersion: String by project

val junitJupiterVersion: String by project
val mockkVersion: String by project
val assertKVersion: String by project

dependencies {
    implementation(kotlin(kotlinStdlibVersion))
    implementation("org.jetbrains.kotlin", "kotlin-reflect", kotlinVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", kotlinXVersion)

    api("joda-time", "joda-time", jodaVersion)

    //Config
    api("io.github.config4k", "config4k", "0.4.1")

    //Logging
    api("org.slf4j", "slf4j-api", slf4jVersion)

    testFixturesImplementation("io.mockk", "mockk", mockkVersion)
    testFixturesImplementation("org.jetbrains.kotlin", "kotlin-reflect", kotlinVersion)

    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitJupiterVersion)
    testImplementation("io.mockk", "mockk", mockkVersion)
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", junitJupiterVersion)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=io.ktor.util.KtorExperimentalAPI")
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")

    }
    compileTestFixturesKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes")
        kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")

    }

    test {
        useJUnitPlatform()
    }
}
