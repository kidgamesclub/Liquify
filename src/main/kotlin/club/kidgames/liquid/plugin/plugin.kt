package club.kidgames.liquid.plugin

import java.io.File

const val LIQUIFY_PLUGIN_NAME = "Liquify"
val liquifyDataDir = File("./plugins/$LIQUIFY_PLUGIN_NAME/").apply { mkdirs() }
