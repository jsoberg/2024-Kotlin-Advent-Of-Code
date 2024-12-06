plugins {
    kotlin("jvm") version libs.versions.kotlin
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.11.1"
    }
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.stdlib)
    implementation(libs.soberg.aoc.api)
}
