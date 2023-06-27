plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.runicrealms"
version = "1.0"

repositories {
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.runicrealms"
            artifactId = "spy"
            version = "1.0"
            from(components["java"])
        }
    }
}
afterEvaluate {
    tasks.getByName("build").dependsOn("shadowJar")
}