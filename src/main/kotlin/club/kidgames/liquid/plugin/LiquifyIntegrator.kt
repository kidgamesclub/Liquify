package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.api.models.LiquidModelMap
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.PlaceholderHook
import org.bukkit.entity.Player
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Integrates this plugin with others.  This class should contain as much plugin code as possible, given the
 * classloading issues with {@link JavaPlugin} that make testing difficult
 */
data class LiquifyIntegrator(var runtime: LiquidRuntime, val logger: Logger) {

  private val name: String = runtime.name

  fun extractSnippets(plugin: PluginInfo) {
    val includesDir = runtime.dataFolder.resolve("snippets")
    val pluginSnippets = plugin.dataFolder.resolve("snippets")
    if (pluginSnippets.exists()) {
      logger.log(Level.INFO, "Found liquid snippet folder at $pluginSnippets")
      // Copy all snippets so they can be used as includes
      val copyToDir = includesDir.resolve(plugin.name).apply { mkdirs() }
      pluginSnippets.copyRecursively(copyToDir, overwrite = true)

      // Register snippet text
      copyToDir.walk()
          .filter { f -> f.isFile && f.extension == "liquid" }
          .forEach { file ->
            val text = file.readText(Charsets.UTF_8)
            val parentPath = file.parentFile.relativeTo(copyToDir).invariantSeparatorsPath
                .trim('/')
                .replace('/', '.')
            val snippetName = when {
              parentPath.isNotEmpty() -> "$parentPath.${file.nameWithoutExtension}"
              else -> file.nameWithoutExtension
            }
            runtime.registerSnippet(SnippetExtender(plugin.name, snippetName, text))
          }
    }
  }

  fun integratePlaceholderAPI() {
    logger.log(Level.INFO, "Enabling Placeholder API integrations")
    runtime.fallbackResolver = fallback@{ placeholder, model ->
      try {
        val placeholders = PlaceholderAPI.getPlaceholders()[placeholder]
        logger.log(Level.INFO, "Placeholders: $placeholders for $placeholder")
        return@fallback when {
          placeholders != null -> LiquidModelMap.newInstance(
              supplier@{ key ->

                val propName = key as String

                val resolvedMessage = placeholders.onPlaceholderRequest(model.player, propName)
                logger.log(Level.INFO, "Resolving message: $placeholder.$key as $resolvedMessage")
                return@supplier resolvedMessage
              })
          else -> {
            val attempted = PlaceholderAPI.setPlaceholders(model.player, "%$placeholder%")
            logger.log(Level.INFO, "Falling back to direct resolution of $placeholder -> $attempted")
            when {
              attempted.startsWith("%") -> null
              else -> attempted
            }
          }
        }
      } catch (e: Exception) {
        logger.log(Level.SEVERE, "Failure resolving PlaceholderAPI placeholder", e)
        null
      }
    }

    // add a hook
    PlaceholderAPI.registerPlaceholderHook(name, object : PlaceholderHook() {
      override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        //If a known snippet name, otherwise render something else
        return if (runtime.isRegistered(LiquidExtenderType.SNIPPET, identifier)) {
          val snippetText = runtime.snippets[identifier]!!
          runtime.render(snippetText, player!!)
        } else {
          null
        }
      }
    })
  }

}
