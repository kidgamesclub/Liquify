package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.merge.utils.SupplierMap
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.external.EZPlaceholderHook
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

open class LiquifyPlugin : JavaPlugin() {
  private val runtime: LiquidRuntime = LiquidRuntime(logger)

  override fun onEnable() {
    LiquidRuntime.instance = runtime
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
    runtime.fallbackResolver = fallback@{ player, parent ->
      try {
        val placeholders = PlaceholderAPI.getPlaceholders()[parent]
        logger.log(Level.INFO, "Placeholders: $placeholders for $parent")
        return@fallback when {
          placeholders != null -> SupplierMap.newInstance<String, Any?>(
              supplier@{ key ->

                val propName = key as String
                // Make sure it's formatted per PlaceholderAPI
                val placeholder = when (propName.startsWith("${parent}_")) {
                  true -> propName
                  false -> "${parent}_$propName"
                }


                val resolvedMessage = placeholders.onPlaceholderRequest(player, placeholder)
                logger.log(Level.INFO, "Resolving message: $parent.$key as $resolvedMessage")
                return@supplier resolvedMessage
              })
          else -> {
            val attempted = PlaceholderAPI.setPlaceholders(player, "%$parent%")
            logger.log(Level.INFO, "Falling back to direct resolution of $parent -> $attempted")
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
        //If a known snippet name, otherwise render somethign else
        return if (runtime.isRegistered(LiquidExtenderType.SNIPPET, identifier)) {
          runtime.renderSnippet(player!!, identifier)
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
