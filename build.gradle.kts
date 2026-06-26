plugins {
    id("java")
    id("application")
}

group = "com.tonic.vellum"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jline:jline:3.25.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // --release only exists on JDK 9+. When Gradle itself runs on JDK 8 (e.g. the IDE's
    // Gradle JVM), fall back to the source/target compatibility set above.
    if (JavaVersion.current().isJava9Compatible) {
        options.release.set(8)
    }
}

application {
    mainClass.set("com.tonic.examples.DashboardDemo")
}

tasks.test {
    useJUnitPlatform()
}
