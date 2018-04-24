package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidModelMap
import club.kidgames.liquid.api.LiquifyRenderer
import club.kidgames.liquid.api.PlaceholderExtender
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.liqp.FallbackResolver
import club.kidgames.liquid.liqp.MinecraftFormat
import club.kidgames.liquid.liqp.MinecraftFormatFilter
import club.kidgames.liquid.liqp.MinecraftFormatTag
import club.kidgames.liquid.liqp.ModelContributor
import com.google.common.cache.CacheBuilder
import liqp.CacheSetup
import liqp.LiquidParser
import liqp.LiquidRenderer
import liqp.MutableRenderSettings
import liqp.RenderSettings
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
import liqp.toNonNullString
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Logger;

class ReloadingLiquifyRenderer(val dataFolder: File,
                               val logger: Logger,
                               val extenders: LiquifyExtenders) : LiquifyRenderer {

  private val executorService: ExecutorService = Executors.newFixedThreadPool(15)

  private val cacheSetup: CacheSetup = object : CacheSetup {
    override fun accept(t: CacheBuilder<*, *>) {
      t.expireAfterWrite(12, TimeUnit.HOURS)
    }
  }

  lateinit var parser: LiquidParser
  private lateinit var renderer: liqp.LiquidRenderer
  private lateinit var fallbackResolvers: List<FallbackResolver>
  private lateinit var snippets: List<SnippetExtender>
  private lateinit var placeholders: List<PlaceholderExtender>

  internal val renderSettings: RenderSettings
    get() = renderer.settings

  init {
    reload()
  }

  internal fun reload() {
    logger.info("Reloading Liquify rendering engine")
    val tags = extenders.tags
        .union(MinecraftFormat.values()
            .map { MinecraftFormatTag(it) })

    val filters = extenders.filters
        .union(MinecraftFormat.values()
            .map { MinecraftFormatFilter(it) })

    // These values are accessed on every render, so we want to capture them when the component is reloaded, rather
    // than inspecting/collapsing the extender collection each time.
    fallbackResolvers = extenders.fallbackResolvers
    snippets = extenders.snippets
    placeholders = extenders.placeholders

    // java time
    val parseSettings = LiquidParser.newBuilder()
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
            DarkenFilter()
        )
        .addTags(*tags.toTypedArray())
        .addFilters(*filters.toTypedArray())
        .cacheSettings(cacheSetup)
        .baseDir(dataFolder)
        .flavor(Flavor.LIQUID)
        .maxTemplateSize(50000L)

    // Run all settings extenders
    extenders.parseSettings.forEach { it.configureParser(parseSettings) }

    val liquidParser = parseSettings.toParser()

    val renderSettings = MutableRenderSettings(liquidParser.toRenderSettings())
        .apply {
          executor = executorService
          baseDir = dataFolder
          flavor = Flavor.LIQUID
          maxIterations = 100
          maxStackSize = 100
          isStrictVariables = false
          maxRenderTimeMillis = 3000L
        }

    for (renderSettingsExtender in extenders.renderSettings) {
      renderSettingsExtender.configureRenderer(renderSettings)
    }
    this.renderer = LiquidRenderer(parser = liquidParser, settings = renderSettings.build())
    this.parser = liquidParser
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
    val model = LiquidModelMap(extenders.fallbackResolvers)

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

  fun withRenderSettings(configure: MutableRenderSettings.() -> Unit): ReloadingLiquifyRenderer {
    val copy = ReloadingLiquifyRenderer(this.dataFolder, this.logger, this.extenders)

    val newSettings = this.renderSettings
        .toMutableRenderSettings()
        .apply { configure() }
        .build()
    copy.renderer = renderer.withRenderSettings(newSettings)
    return copy
  }
}
