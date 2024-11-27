plugins {
    kotlin("jvm") version "2.0.21"
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.11"
    }
}

dependencies {
    implementation("com.github.jsoberg:Kotlin-AoC-API:1.0")
}
