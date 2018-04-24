package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.api.LiquifyExtender
import club.kidgames.liquid.api.LiquifyRenderer
import club.kidgames.liquid.liqp.ExtensionName
import club.kidgames.liquid.liqp.PluginName
import com.google.common.collect.Multimap
import java.io.File
import java.util.function.Consumer
import java.util.logging.Logger

typealias ByPluginName = Multimap<PluginName, LiquifyExtender>
typealias ByExtenderName = MutableMap<ExtensionName, LiquifyExtender>
typealias ExtendersByType<E> = MutableMap<LiquidExtenderType, E>

var liquifyInstance: Liquify = Liquify(
    Logger.getLogger(LIQUIFY_PLUGIN_NAME), LIQUIFY_PLUGIN_NAME, liquifyDataDir)

/**
 * Renders liquid templates for the Liquify plugin.  Internally, it maintains an engine instance that
 * can be rebuilt when configuration changes.
 *
 * Should stay agnostic of minecraft-specific types, including plugin classes.  This improves
 * testability
 */
class Liquify(var logger: Logger = Logger.getLogger("Liquify"),
              val name: String = LIQUIFY_PLUGIN_NAME,
              val dataFolder: File = liquifyDataDir,
              var isInitialized: Boolean = false) {

  val extenders:LiquifyExtenders = LiquifyExtenders(logger, onRegistered = Consumer { _ ->
    if(isInitialized) {
      renderer.reload()
    }
  })
  val renderer = ReloadingLiquifyRenderer(dataFolder, logger, extenders)

  fun unload() {
    extenders.unregisterAll()
  }

  fun refresh() {
    renderer.reload()
  }

  operator fun component1(): LiquifyRenderer {
    return renderer
  }

  operator fun component2(): LiquifyExtenders {
    return extenders
  }

  companion object {
    @JvmStatic
    val instance:Liquify = liquifyInstance
  }
}

