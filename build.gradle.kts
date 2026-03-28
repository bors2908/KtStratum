plugins {
    java
}

group = "ge.becrin.kt.stratum"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    // JSON
    implementation("org.json:json:20251224")

    // Google Guava collections
    implementation("com.google.guava:guava:33.5.0-jre")

    // Logging: SLF4J
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:jcl-over-slf4j:2.0.17")
}
