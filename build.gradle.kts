import io.papermc.hangarpublishplugin.model.Platforms
import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("xyz.jpenilla.run-paper") version "3.0.0-beta.1"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.0"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.modrinth.minotaur") version "2.+"
    id("com.matthewprenger.cursegradle") version "1.4.0"
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

hangarPublish {
    publications.register("plugin") {
        version.set(project.version as String)
        channel.set("Release")
        id.set("AnimalPenPaperized")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))

        changelog.set(rootProject.file("CHANGELOG_LATEST.md").readText())

        platforms {
            register(Platforms.PAPER) {
                // TODO: If you're using ShadowJar, replace the jar lines with the appropriate task:
                //   jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                // Set the JAR file to upload
                jar.set(tasks.jar.flatMap { it.archiveFile })

                // Set platform versions from gradle.properties file
                val versions: List<String> = (property("paper_version") as String)
                    .split(",")
                    .map { it.trim() }
                platformVersions.set(versions)
            }
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("animalpenspaperized")
    versionNumber.set("$version-$minecraftVersion")
    versionName.set("$version-$minecraftVersion-Paper")
    versionType.set("release")
    gameVersions.addAll("1.21.8")
    loaders.add("paper")

    uploadFile.set(tasks.jar)

    changelog.set(rootProject.file("CHANGELOG_LATEST.md").readText())
    syncBodyFrom.set(rootProject.file("README.md").readText())
}


curseforge {
    project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
        apiKey = System.getenv("CURSEFORGE_TOKEN")
        id = "1336479"
        changelog = rootProject.file("CHANGELOG_LATEST.md").readText()
        changelogType = "text"
        releaseType = "release"

        addGameVersion("1.21.8")
        addGameVersion("Paper")
        addGameVersion("Java 21")

        mainArtifact(tasks.jar)
        addArtifact(resourcePack.get())
    })
}