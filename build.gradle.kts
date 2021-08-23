plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
    id("java-test-fixtures")
    id("com.palantir.git-version") version "0.12.3"
    `maven-publish`
    id("com.jfrog.artifactory") version "4.21.0"
}

val gitVersion: groovy.lang.Closure<String> by extra

group = "io.plurex"
version = gitVersion().replace(".dirty", "")

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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    implementation("joda-time", "joda-time", jodaVersion)

    //Config
    implementation("io.github.config4k", "config4k", "0.4.1")

    //Logging
    implementation("org.slf4j", "slf4j-api", slf4jVersion)

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

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

val testFixturesSourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("testFixtures").allSource)
}

publishing {
    publications {
        create<MavenPublication>("plurexPangolin") {
            groupId = "io.plurex"
            artifactId = "pangolin"
            version = version
            from(components["java"])
            artifact(sourcesJar)
            artifact(testFixturesSourcesJar)
        }
    }
}

artifactory {
    setContextUrl("https://plurex.jfrog.io/artifactory")
    publish(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig> {
        repository(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.DoubleDelegateWrapper> {
            setProperty("repoKey", "io.plurex.pangolin")
            setProperty("username", System.getenv("JFROG_USER"))
            setProperty("password", System.getenv("JFROG_PASSWORD"))
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask> {
            publications("plurexPangolin")
        })
    })

}
