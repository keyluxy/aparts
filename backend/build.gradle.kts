plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)

    kotlin("plugin.serialization") version "1.9.22"

}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "com.example.ApplicationKt"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.45.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.2")

    // Библиотека для хеширования паролей
    implementation("org.mindrot:jbcrypt:0.4")

    // Зависимость для работы с java.time
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")

    // kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")

    implementation("io.ktor:ktor-client-cio:2.3.0")

    // Зависимости для работы с Excel
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.auth0:java-jwt:4.4.0")

}
