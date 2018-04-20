import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.mverse.gradle.main
import io.mverse.gradle.sourceSets
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.codehaus.groovy.tools.shell.util.Logger.io
import org.gradle.api.internal.file.pattern.PatternMatcherFactory.compile
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCallArgument.DefaultArgument.arguments
import sun.tools.jar.resources.jar

plugins {
  id("org.gradle.kotlin.kotlin-dsl").version("0.16.0")
  id("com.github.johnrengelman.shadow").version("2.0.3")
  id("io.mverse.project").version("0.5.23")
}

repositories {
  jcenter()
  maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
  maven("http://repo.bstats.org/content/repositories/releases/")
  maven("http://repo.dmulloy2.net/nexus/repository/public/")
}

mverse {
  isDefaultDependencies = false
  groupId = "club.kidgames"
  dependencies {
    compile(guava())
    compile("kotlin-stdlib")
    compile("kotlin-reflect")
    compile("spigot-api")
    compile("jackson-annotations")
    compile(streamEx())
    compile(commonsLang3())
    compile("antlr4-runtime")
    compileOnly(lombok())
    testCompile(junit())
    testCompile(assertj())
    testCompile(mockito())
  }

  coverageRequirement = 0.0

}

findbugs {
  isIgnoreFailures = true
}

dependencyManagement {
  dependencies {
    dependency("club.kidgames:liqp:0.7.19")
    dependency("org.mockito:mockito-core:2.18.0")

    dependency("me.clip:PlaceholderAPI:2.5.+")
    dependency("org.antlr:antlr4-runtime:4.7.1")
    dependency("org.spigotmc:spigot-api:1.12.+")
    dependency("org.yaml:snakeyaml:1.18")
  }
}

dependencies {
  compile("club.kidgames:liqp")
  testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.0.0-alpha03")

  compileOnly("me.clip:PlaceholderAPI")
  testCompile("me.clip:PlaceholderAPI")
  compileOnly("org.spigotmc:spigot-api")
}

tasks.withType(ShadowJar::class.java) {
  dependsOn("jar")
  dependencies {
    exclude(dependency(":spigot-api"))
    exclude(dependency(":PlaceholderAPI"))
    include(dependency(":liqp"))
    include(dependency(":antlr4-runtime"))
    include(dependency(":commons-lang"))
    include(dependency(":streamex"))
    include(dependency(":jackson-annotations"))
    include(dependency(":kotlin-reflect"))
  }
  classifier = null
  version = null
}


tasks["build"].dependsOn("shadowJar")
