package club.kidgames.integrations

import club.kidgames.liquid.api.FallbackResolverExtender
import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.api.LiquidIntegrationResult
import club.kidgames.liquid.api.LiquidIntegrationResult.SUCCESS
import club.kidgames.liquid.api.LiquidModelMap
import club.kidgames.liquid.api.Liquify3rdPartyIntegrator
import club.kidgames.liquid.api.LiquifyExtenderRegistry
import club.kidgames.liquid.api.LiquifyRenderer
import club.kidgames.liquid.liqp.FallbackResolver
import club.kidgames.liquid.plugin.LiquifyExtenders
import club.kidgames.liquid.plugin.PluginInfo
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.PlaceholderHook
import org.bukkit.entity.Player
import org.bukkit.plugin.PluginManager
import java.util.logging.Level
import java.util.logging.Logger

const val PLACEHOLDER_API = "PlaceholderAPI"

class PlaceholderAPIIntegrator(private val extenders: LiquifyExtenders,
                               private val logger: Logger) : Liquify3rdPartyIntegrator(PLACEHOLDER_API) {
  override fun integratePlugin(renderer: LiquifyRenderer, ext: LiquifyExtenderRegistry, pluginInfo: PluginInfo, manager: PluginManager): LiquidIntegrationResult {
    logger.log(Level.INFO, "Enabling Placeholder API integrations")

    // Add a fallback handler that looks to placeholder API
    extenders.registerFallbackResolver(getFallbackResolver(logger))

    // add a hook back to placeholder to render Liquify snippets
    PlaceholderAPI.registerPlaceholderHook(name, getPlaceholderAPIHook(renderer))

    return SUCCESS
  }

  private fun getFallbackResolver(logger: Logger): FallbackResolverExtender {
    val resolver: FallbackResolver = fallback@{ placeholder, model ->
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

    return FallbackResolverExtender(PLACEHOLDER_API, resolver)
  }

  private fun getPlaceholderAPIHook(renderer: LiquifyRenderer): PlaceholderHook {
    return object : PlaceholderHook() {
      override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        //If a known snippet name, otherwise render something else
        return if (extenders.isRegistered(LiquidExtenderType.SNIPPET, identifier)) {
          val snippetText = extenders.snippetMap[identifier]!!
          renderer.render(snippetText, player!!)
        } else {
          null
        }
      }
    }
  }
}
