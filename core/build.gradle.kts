plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
    sourceSets {
        main {
            kotlin.srcDir("../IMPLEMENTATION/KOTLIN_PORT/src/main/kotlin")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}
