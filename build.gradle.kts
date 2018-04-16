import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.codehaus.groovy.tools.shell.util.Logger.io
import org.gradle.api.internal.file.pattern.PatternMatcherFactory.compile
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCallArgument.DefaultArgument.arguments

plugins {
  id("org.gradle.kotlin.kotlin-dsl").version("0.16.0")
  id("com.github.johnrengelman.shadow").version("2.0.3")
  id("io.mverse.project").version("0.5.16")
}

repositories {
  jcenter()
  maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
  maven("http://repo.bstats.org/content/repositories/releases/")
  maven("http://repo.dmulloy2.net/nexus/repository/public/")
}

mverse {
  groupId = "club.kidgames"
  //        checkstyleLocation = "/Users/ericm/etc/checkstyle/checkstyle"
  modules {
    compile("guava")
    compile("kotlin-stdlib")
    compile("spigot-api")
  }

  coverageRequirement = 0.0
  dependencies.lombok = false
  dependencies.streamEx = false
  dependencies.logback = false
}

findbugs {
  isIgnoreFailures = true
}

dependencyManagement {
  dependencies {
    dependency("club.kidgames:liqp:0.7.13")
    dependency("me.clip:PlaceholderAPI:2.5.+")
    dependency("org.spigotmc:spigot-api:1.12.+")
    dependency("org.yaml:snakeyaml:1.18")
  }
}

dependencies {
  compile("club.kidgames:liqp:0.7.13")
  compileOnly("me.clip:PlaceholderAPI")
  testCompile("me.clip:PlaceholderAPI")
  compileOnly("org.spigotmc:spigot-api")
}

val shadowJar:ShadowJar by tasks
shadowJar.dependencies {
  exclude(dependency(":spigot-api"))
  exclude(dependency(":PlaceholderAPI"))
  include(dependency(":liqp"))
  include(dependency(":commons-lang"))
}
