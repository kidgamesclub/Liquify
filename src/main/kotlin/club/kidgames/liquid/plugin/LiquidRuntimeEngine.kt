package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidRenderEngine
import club.kidgames.liquid.api.PlaceholderExtender
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.api.models.LiquidModelMap
import club.kidgames.liquid.extensions.FallbackResolver
import club.kidgames.liquid.extensions.MinecraftFormat
import club.kidgames.liquid.extensions.MinecraftFormatFilter
import club.kidgames.liquid.extensions.MinecraftFormatTag
import club.kidgames.liquid.extensions.ModelContributor
import com.google.common.cache.CacheBuilder
import liqp.CacheSetup
import liqp.LiquidParser
import liqp.LiquidRenderer
import liqp.MutableParseSettings
import liqp.MutableRenderSettings
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
import liqp.parser.Flavor
import liqp.tags.Tag
import liqp.toNonNullString
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import java.io.File


/**
 * Liquid text merging plugin.  This plugin uses liquid templating language to allow for robust rending capabilities.
 */
class LiquidRuntimeEngine(tags: List<Tag> = listOf(),
                          filters: List<Filter> = listOf(),
                          baseDir:File,
                          var fallbackResolver: FallbackResolver = defaultFallbackResolver,
                          private val placeholders: List<PlaceholderExtender> = listOf(),
                          private val snippets: List<SnippetExtender> = listOf(),
                          internal val configureRenderer: MutableRenderSettings.() -> MutableRenderSettings = { this },
                          internal val configureParser: MutableParseSettings.() -> MutableParseSettings = { this },
                          private val logger: Logger = Logger.getLogger(LiquidRuntimeEngine::class.java.name)
) : LiquidRenderEngine {

  private var parser: LiquidParser
  private var renderer: LiquidRenderer
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
        .addTags(*tags.toTypedArray(), *formattingTags)
        .addFilters(*filters.toTypedArray())
        .cacheSettings(cacheSetup)
        .baseDir(baseDir)
        .flavor(Flavor.LIQUID)
        .maxTemplateSize(50000L)
        .configureParser()
        .toParser()


    this.executorService = Executors.newFixedThreadPool(15)
    renderer = LiquidRenderer.newInstance {
      executor = executorService
      maxIterations = 100
      maxStackSize = 100
      isStrictVariables = false
      maxRenderTimeMillis = 3000L
      configureRenderer()
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
    return executeWithContext(template, buildRenderContext(*modelContributor))
  }

  override fun render(template: String, vararg modelContributor: ModelContributor): String {
    return renderWithContext(template, buildRenderContext(*modelContributor))
  }

  fun buildRenderContext(vararg modelContributors: ModelContributor): RenderContext {
    val model = LiquidModelMap({ property, self ->
      fallbackResolver(property, self)
    })

    val renderContext = renderer.createRenderContext(model)

    placeholders.forEach {
      model.putSupplier(it.name, {model ->
        it.resolvePlaceholder(model)
      })
    }

    model.putSupplier("snippets", snippet@{
      val snippetMap = LiquidModelMap()
      snippets.forEach {snippet->
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
