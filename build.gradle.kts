import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("xyz.jpenilla.run-paper") version "3.0.0-beta.1"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.0"
    id("com.modrinth.minotaur") version "2.+"
}

group = project.findProperty("maven_group") as String
version = project.findProperty("plugin_version") as String

val minecraftVersion = project.findProperty("minecraft_version") as String
val javaVersion = (project.findProperty("java_version") as String).toInt()

base {
    archivesName.set(project.findProperty("archives_name") as String + "-$minecraftVersion")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

dependencies {
    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.release = javaVersion
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    runServer {
        runDirectory.set(layout.projectDirectory.dir("run/$minecraftVersion"))
    }
}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.
bukkitPluginYaml {
    main = "lv.id.bonne.animalpenpaper.AnimalPenPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.add("BONNe")
    apiVersion = minecraftVersion
    website = "https://github.com/BONNePlayground/AnimalPenPaperized"
}

tasks.processResources {
    filesMatching("paper-plugin.yml") {
        expand(
            "apiVersion" to minecraftVersion,
            "pluginVersion" to version,
        )
    }
}

val resourcePack = tasks.register<Zip>("resourcePack") {
    group = "build"
    archiveBaseName.set("animal_pen_resource_pack")
    archiveVersion.set("$version")
    archiveExtension.set("zip")

    from("src/main/resources/animal_pen_resource_pack")
}


modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("animalpenspaperized")
    versionNumber.set("$version-$minecraftVersion")
    versionName.set("$version-$minecraftVersion-Paper")
    versionType.set("release")
    gameVersions.addAll("1.21.8")
    loaders.add("paper")

    uploadFile.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })

    changelog.set(rootProject.file("CHANGELOG_LATEST.md").readText())
    syncBodyFrom.set(rootProject.file("README.md").readText())
}


publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/BONNePlayground/AnimalPenPaperized")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            artifactId = project.name.lowercase()
        }
    }
}