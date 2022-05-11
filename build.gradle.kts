import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    id("org.jetbrains.dokka") version "1.6.21"
    id("signing")
    id("maven-publish")
}

group = "io.txture"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    api("jakarta.json:jakarta.json-api:2.1.0")
    implementation("org.slf4j:slf4j-api:1.7.36")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.21")
    dokkaJavadocPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.21")

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
    testImplementation("io.strikt:strikt-core:0.34.1")
    testImplementation("ch.qos.logback:logback-classic:1.2.11")
}

signing {
    isRequired = !version.toString().endsWith("-SNAPSHOT") && tasks.withType<PublishToMavenRepository>().any {
        gradle.taskGraph.hasTask(it)
    }
    sign(publishing.publications)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    test {
        useJUnitPlatform()
    }

    val javadocJar by creating(Jar::class) {
        dependsOn(dokkaJavadoc)
        archiveClassifier.set("javadoc")
        from(dokkaJavadoc)
    }

    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    artifacts {
        archives(sourcesJar)
        archives(javadocJar)
    }

}

publishing {
    repositories {
        maven {
            name = "s01.oss.sonatype.org"
            val isReleaseVersion = !project.version.toString().endsWith("SNAPSHOT")
            val releaseRepo = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotRepo = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            setUrl(if (isReleaseVersion) {
                releaseRepo
            } else {
                snapshotRepo
            })
            credentials {
                username = if (project.hasProperty("ossrhUsername")) {
                    project.properties["ossrhUsername"] as String
                } else {
                    "Unknown user"
                }
                password = if (project.hasProperty("ossrhPassword")) {
                    project.properties["ossrhPassword"] as String
                } else {
                    "Unknown password"
                }
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            pom {
                groupId = project.group.toString()
                name.set(project.name)
                version = project.version.toString()
                description.set("Hornox is a fast, simple-stupid BSON serializer, deserializer and node extractor for the JVM.")
                url.set("https://github.com/Txture/hornox-bson")

                from(components["java"])
                artifact(tasks.getByName("sourcesJar"))
                artifact(tasks.getByName("javadocJar"))

                packaging = "jar"

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                scm {
                    connection.set("scm:git:git@github.com:Txture/hornox-bson.git")
                    developerConnection.set("scm:git:git@github.com:Txture/hornox-bson.git")
                    url.set("https://github.com/Txture/hornox-bson")
                }

                developers {
                    developer {
                        id.set("Martin Häusler")
                        name.set("Martin Häusler")
                        email.set("martin.haeusler@txture.io")
                    }
                }
            }
        }
    }
}