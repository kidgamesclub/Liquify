package club.kidgames.integrations

import club.kidgames.liquid.api.LiquifyRenderer
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.plugin.LiquifyExtenders
import club.kidgames.liquid.plugin.PluginInfo
import liqp.parser.Flavor.LIQUID
import org.bukkit.plugin.PluginManager
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

const val LIQUID_FILE_SUFFIX = "liquid"

/**
 * Integrates this plugin with others.  This class should contain as much plugin code as possible, given the
 * classloading issues with {@link JavaPlugin} that make testing difficult
 */
data class LiquifyIntegrator(private val renderer: LiquifyRenderer,
                             private val extenders: LiquifyExtenders,
                             private val pluginManager: PluginManager,
                             private val baseDir: File,
                             private val logger: Logger) {

  fun integrateWith3rdPartyPlugin(plugin: PluginInfo) {
    extractSnippets(plugin)
    extenders.integrators[plugin.name]
        ?.integratePlugin(renderer = renderer,
            extenders = extenders,
            pluginInfo = plugin,
            manager = pluginManager)
  }

  fun remove3rdPartyIntegration(plugin: PluginInfo) {

  }

  private fun extractSnippets(plugin: PluginInfo) {

    val includesDir = baseDir.resolve(LIQUID.includesDirName)
    val pluginSnippets = plugin.dataFolder.resolve(LIQUID.includesDirName)
    if (pluginSnippets.exists()) {
      logger.log(Level.INFO, "Found liquid snippet folder at $pluginSnippets")
      // Copy all snippets so they can be used as includes
      val copyToDir = includesDir.resolve(plugin.name).apply { mkdirs() }
      pluginSnippets.copyRecursively(copyToDir, overwrite = true)

      // Register snippet text
      copyToDir.walk()
          .filter { f -> f.isFile && f.extension == LIQUID_FILE_SUFFIX }
          .forEach { file ->
            val text = file.readText(Charsets.UTF_8)
            val parentPath = file.parentFile.relativeTo(copyToDir).invariantSeparatorsPath
                .trim('/')
                .replace('/', '.')
            val snippetName = when {
              parentPath.isNotEmpty() -> "$parentPath.${file.nameWithoutExtension}"
              else -> file.nameWithoutExtension
            }
            extenders.registerSnippet(SnippetExtender(plugin.name, snippetName, text))
          }
    }
  }
}
