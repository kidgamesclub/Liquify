package club.kidgames.liquid

import java.io.File

val liquifyTestServerDir = File("./build/plugin/testserver/").apply { mkdirs() }

val liquifyTestDir: File
  get() {
    val testDir = liquifyTestServerDir
    // Copy from src/main/resources
    File("./src/test/resources/testserver")
        .apply { mkdirs() }
        .copyRecursively(testDir, overwrite = true)
    return testDir.resolve("plugins/Liquify")
  }
