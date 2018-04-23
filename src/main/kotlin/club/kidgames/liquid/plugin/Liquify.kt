package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.api.LiquidModelMap
import club.kidgames.liquid.api.LiquifyExtender
import club.kidgames.liquid.api.LiquifyRenderer
import club.kidgames.liquid.api.PlaceholderExtender
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.liqp.ExtensionName
import club.kidgames.liquid.liqp.MinecraftFormat
import club.kidgames.liquid.liqp.MinecraftFormatFilter
import club.kidgames.liquid.liqp.MinecraftFormatTag
import club.kidgames.liquid.liqp.ModelContributor
import club.kidgames.liquid.liqp.PluginName
import com.google.common.cache.CacheBuilder
import com.google.common.collect.Multimap
import liqp.CacheSetup
import liqp.LiquidParser
import liqp.ext.filters.collections.CommaSeparatedFilter
import liqp.ext.filters.colors.DarkenFilter
import liqp.ext.filters.colors.ToRgbFilter
import liqp.ext.filters.javatime.IsoDateTimeFormatFilter
import liqp.ext.filters.javatime.MinusDaysFilter
import liqp.ext.filters.javatime.MinusHoursFilter
import liqp.ext.filters.javatime.MinusMinutesFilter
import liqp.ext.filters.javatime.MinusMonthsFilter
import liqp.ext.filters.javatime.MinusSecondsFilter
import liqp.ext.filters.javatime.MinusWeeksFilter
import liqp.ext.filters.javatime.MinusYearsFilter
import liqp.ext.filters.javatime.PlusDaysFilter
import liqp.ext.filters.javatime.PlusHoursFilter
import liqp.ext.filters.javatime.PlusMinutesFilter
import liqp.ext.filters.javatime.PlusMonthsFilter
import liqp.ext.filters.javatime.PlusSecondsFilter
import liqp.ext.filters.javatime.PlusWeeksFilter
import liqp.ext.filters.javatime.PlusYearsFilter
import liqp.ext.filters.strings.ToDoubleFilter
import liqp.ext.filters.strings.ToIntegerFilter
import liqp.filters.Filter
import liqp.nodes.RenderContext
import liqp.parser.Flavor.LIQUID
import liqp.tags.Tag
import liqp.toNonNullString
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.logging.Logger

typealias ByPluginName = Multimap<PluginName, LiquifyExtender>
typealias ByExtenderName = MutableMap<ExtensionName, LiquifyExtender>
typealias ExtendersByType<E> = MutableMap<LiquidExtenderType, E>

var liquifyInstance: Liquify = Liquify(
    Logger.getLogger(liquifyPluginName), liquifyPluginName, liquifyDataDir)

/**
 * Renders liquid templates for the Liquify plugin.  Internally, it maintains an engine instance that
 * can be rebuilt when configuration changes.
 *
 * Should stay agnostic of minecraft-specific types, including plugin classes.  This improves
 * testability
 */
class Liquify(var logger: Logger = Logger.getLogger("Liquify"),
              val name: String = liquifyPluginName,
              val dataFolder: File = liquifyDataDir,
              var isInitialized: Boolean = false) {

  val extenders:LiquifyExtenders = LiquifyExtenders(logger, onRegistered = Consumer { _ ->
    if(isInitialized) {
      renderer.reload()
    }
  })
  val renderer = ReloadingLiquifyRenderer(dataFolder, extenders)

  fun reload() {
    renderer.reload()
  }

  operator fun component1(): LiquifyRenderer {
    return renderer
  }

  operator fun component2(): LiquifyExtenders {
    return extenders
  }


}

