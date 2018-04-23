package club.kidgames.liquid

import java.io.File

val liquifyTestDir = File("./build/plugin/Liquify").apply { mkdirs() }

fun setupLiquifyTestDir() {
  // Copy from src/main/resources
  File("./src/test/resources/Liquify")
      .apply { mkdirs() }
      .copyRecursively(liquifyTestDir)
}
