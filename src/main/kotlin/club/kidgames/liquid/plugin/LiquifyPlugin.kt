package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.api.models.LiquidModelMap
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.external.EZPlaceholderHook
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

open class LiquifyPlugin : JavaPlugin() {

  private val runtime:LiquidRuntime

  init {
    liquidRuntimeInstance.logger = logger
    runtime = liquidRuntimeInstance
  }

  override fun onEnable() {
    server.pluginManager.registerEvents(LiquidPluginListener(logger, this), this)
    this.getCommand("liquify").executor = LiquifyCommand(runtime)
    this.getCommand("lq").executor = LiquifyCommand(runtime)
    if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
      integratePlaceholderAPI()
    }
  }

  override fun onLoad() {
    runtime.isInitialized = true
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
    PlaceholderAPI.registerPlaceholderHook(this, object: EZPlaceholderHook(this, "snippet") {
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

  fun resetFallbackResolver() {
    runtime.fallbackResolver = defaultFallbackResolver
  }
}
