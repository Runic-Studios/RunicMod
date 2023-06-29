val artifactName = "mod"
val rrGroup: String by rootProject.extra
val rrVersion: String by rootProject.extra

plugins {
    `java-library`
    `maven-publish`
}

group = rrGroup
version = rrVersion

dependencies {
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.acf)
    compileOnly(commonLibs.paper)
    compileOnly(project(":Projects:Common"))
    compileOnly(project(":Projects:Items"))
    compileOnly(project(":Projects:Core"))
    compileOnly(project(":Projects:Chat"))
    compileOnly(project(":Projects:Database"))
    compileOnly(project(":Projects:Bank"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rrGroup
            artifactId = artifactName
            version = rrVersion
            from(components["java"])
        }
    }
}