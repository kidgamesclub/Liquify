package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidExtender
import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.api.LiquifyRenderer
import club.kidgames.liquid.api.models.LiquidModelMap
import club.kidgames.liquid.extensions.ExtensionName
import club.kidgames.liquid.extensions.FallbackResolver
import club.kidgames.liquid.extensions.MinecraftFormat
import club.kidgames.liquid.extensions.MinecraftFormatFilter
import club.kidgames.liquid.extensions.MinecraftFormatTag
import club.kidgames.liquid.extensions.ModelContributor
import club.kidgames.liquid.extensions.PluginName
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
import liqp.nodes.RenderContext
import liqp.parser.Flavor
import liqp.parser.Flavor.*
import liqp.toNonNullString
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.logging.Logger

typealias ByPluginName = Multimap<PluginName, LiquidExtender>
typealias ByExtenderName = MutableMap<ExtensionName, LiquidExtender>
typealias ExtendersByType<E> = MutableMap<LiquidExtenderType, E>

val defaultFallbackResolver: FallbackResolver = { _, _ -> null }
val liquifyInstance: Liquify = Liquify(
    Logger.getLogger(liquifyPluginName), liquifyPluginName, liquifyDataDir)

/**
 * Renders liquid templates for the Liquify plugin.  Internally, it maintains an engine instance that
 * can be rebuilt if configuration changes.
 *
 * Should stay agnostic of minecraft-specific types, including plugins.
 */
data class Liquify(var logger: Logger = Logger.getLogger("Liquify"),
                   val name: String = liquifyPluginName,
                   val dataFolder: File = liquifyDataDir,
                   var isInitialized: Boolean = false) {

  private var _fallbackResolver = defaultFallbackResolver
  var fallbackResolver:FallbackResolver
    get() = _fallbackResolver
    set(fallback) {
      _fallbackResolver = fallback
      renderer = buildRenderer()
    }

  val extenders = LiquifyExtenders(logger, onRegistered = Consumer { _ -> buildRenderer() })
  var renderer = buildRenderer()

  private fun buildRenderer(): LiquifyRenderer {

    val tags = extenders.tags.toTypedArray()
    val filters = extenders.filters.toTypedArray()
    val placeholders = extenders.placeholders
    val snippets = extenders.snippets

    /**
     * Liquid text merging plugin.  This plugin uses liquid templating language to allow for robust rending capabilities.
     */
    return object : LiquifyRenderer {

      private var parser: LiquidParser
      private var renderer: liqp.LiquidRenderer
      private var executorService: ExecutorService

      init {
        val cacheSetup: CacheSetup = object : CacheSetup {
          override fun accept(t: CacheBuilder<*, *>) {
            t.expireAfterWrite(12, TimeUnit.HOURS)
          }
        }

        val formattingFilters = MinecraftFormat.values()
            .map { MinecraftFormatFilter(it) }
            .toTypedArray()

        val formattingTags = MinecraftFormat.values()
            .map { MinecraftFormatTag(it) }
            .toTypedArray()

        // java time
        parser = LiquidParser.newBuilder()
            .addFilters(
                MinusYearsFilter(),
                MinusMonthsFilter(),
                MinusWeeksFilter(),
                MinusDaysFilter(),
                MinusHoursFilter(),
                MinusMinutesFilter(),
                MinusSecondsFilter(),
                PlusYearsFilter(),
                PlusMonthsFilter(),
                PlusWeeksFilter(),
                PlusDaysFilter(),
                PlusHoursFilter(),
                PlusMinutesFilter(),
                PlusSecondsFilter(),
                IsoDateTimeFormatFilter(),

                // collections
                CommaSeparatedFilter(),

                // strings
                ToIntegerFilter(),
                ToDoubleFilter(),

                // colors
                ToRgbFilter(),
                DarkenFilter(),
                *formattingFilters
            )
            .addTags(*tags, *formattingTags)
            .addFilters(*filters)
            .cacheSettings(cacheSetup)
            .baseDir(dataFolder)
            .flavor(LIQUID)
            .maxTemplateSize(50000L)
            .toParser()


        this.executorService = Executors.newFixedThreadPool(15)
        renderer = liqp.LiquidRenderer.newInstance {
          executor = executorService
          baseDir = dataFolder
          flavor = LIQUID

          maxIterations = 100
          maxStackSize = 100
          isStrictVariables = false
          maxRenderTimeMillis = 3000L
          this
        }
      }

      fun renderWithContext(templateString: String, context: RenderContext): String {
        return executeWithContext(templateString, context).toNonNullString()
      }

      fun executeWithContext(templateString: String, context: RenderContext): Any? {
        val template = parser.parse(templateString)
        return renderer.executeWithContext(template, context)
      }

      override fun execute(template: String, vararg modelContributor: ModelContributor): Any? {
        return executeWithContext(template, newRenderContext(*modelContributor))
      }

      override fun render(template: String, vararg modelContributor: ModelContributor): String {
        return renderWithContext(template, newRenderContext(*modelContributor))
      }

      override fun render(template: String, context: RenderContext): String {
        return renderWithContext(template, context).toNonNullString()
      }

      override fun newRenderContext(vararg modelContributors: ModelContributor): RenderContext {
        val model = LiquidModelMap({ property, self ->
          fallbackResolver(property, self)
        })

        val renderContext = renderer.createRenderContext(model)

        placeholders.forEach {
          model.putSupplier(it.name, { model ->
            it.resolvePlaceholder(model)
          })
        }

        model.putSupplier("snippets", snippet@{
          val snippetMap = LiquidModelMap()
          snippets.forEach { snippet ->
            snippetMap.putSupplier(snippet.name, {
              this.executeWithContext(snippet.snippetText, renderContext)
            })
          }
          return@snippet snippetMap
        })

        modelContributors.forEach { contributeModels -> contributeModels(model) }

        return renderContext
      }
    }
  }
}
