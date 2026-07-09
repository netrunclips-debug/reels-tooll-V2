// Root project build file
plugins {
    kotlin("android") version "1.9.23" apply false
    id("com.android.application") version "8.2.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
