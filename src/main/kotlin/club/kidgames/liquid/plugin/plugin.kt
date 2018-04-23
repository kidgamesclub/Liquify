package club.kidgames.liquid.plugin

import java.io.File

val liquifyPluginName = "Liquify"
val liquifyDataDir = File("./plugins/$liquifyPluginName/").apply { mkdirs() }
