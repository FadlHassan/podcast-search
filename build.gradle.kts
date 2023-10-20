plugins {
    kotlin("jvm") version "1.9.0"
    application
    id("com.apollographql.apollo3") version "3.8.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}

apollo {
    service("podcast-category-search") {
        srcDir("src/main/graphql/podcast-category-search")
        packageName.set("com.podcast.category.search")
        schemaFile.set(file("src/main/graphql/podcast-category-search/schema.graphqls"))
    }
}