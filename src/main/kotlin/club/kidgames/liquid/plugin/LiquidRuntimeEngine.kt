package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidRenderEngine
import club.kidgames.liquid.api.PlaceholderExtender
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.extensions.MinecraftFormat
import club.kidgames.liquid.extensions.MinecraftFormatFilter
import club.kidgames.liquid.extensions.MinecraftFormatTag
import club.kidgames.liquid.merge.filters.collections.CommaSeparatedFilter
import club.kidgames.liquid.merge.filters.colors.DarkenFilter
import club.kidgames.liquid.merge.filters.colors.ToRgbFilter
import club.kidgames.liquid.merge.filters.javatime.IsoDateTimeFormatFilter
import club.kidgames.liquid.merge.filters.javatime.MinusDaysFilter
import club.kidgames.liquid.merge.filters.javatime.MinusHoursFilter
import club.kidgames.liquid.merge.filters.javatime.MinusMinutesFilter
import club.kidgames.liquid.merge.filters.javatime.MinusMonthsFilter
import club.kidgames.liquid.merge.filters.javatime.MinusSecondsFilter
import club.kidgames.liquid.merge.filters.javatime.MinusWeeksFilter
import club.kidgames.liquid.merge.filters.javatime.MinusYearsFilter
import club.kidgames.liquid.merge.filters.javatime.PlusDaysFilter
import club.kidgames.liquid.merge.filters.javatime.PlusHoursFilter
import club.kidgames.liquid.merge.filters.javatime.PlusMinutesFilter
import club.kidgames.liquid.merge.filters.javatime.PlusMonthsFilter
import club.kidgames.liquid.merge.filters.javatime.PlusSecondsFilter
import club.kidgames.liquid.merge.filters.javatime.PlusWeeksFilter
import club.kidgames.liquid.merge.filters.javatime.PlusYearsFilter
import club.kidgames.liquid.merge.filters.strings.ToDoubleFilter
import club.kidgames.liquid.merge.filters.strings.ToIntegerFilter
import club.kidgames.liquid.api.models.LiquidModelMap
import club.kidgames.liquid.extensions.FallbackResolver
import club.kidgames.liquid.extensions.ModelContributor
import com.google.common.cache.CacheBuilder
import liqp.CacheSetup
import liqp.RenderSettings
import liqp.TemplateEngine
import liqp.TemplateFactory
import liqp.TemplateFactorySettings
import liqp.filters.Filter
import liqp.nodes.RenderContext
import liqp.parser.Flavor
import liqp.tags.Tag
import liqp.toNonNullString
import org.bukkit.entity.Player
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Liquid text merging plugin.  This plugin uses liquid templating language to allow for robust rending capabilities.
 */
class LiquidRuntimeEngine(tags: List<Tag> = listOf(),
                          filters: List<Filter> = listOf(),
                          private val placeholders: List<PlaceholderExtender> = listOf(),
                          private val snippets: List<SnippetExtender> = listOf(),
                          internal var fallbackResolver: FallbackResolver = defaultFallbackResolver,
                          internal val configureRenderSettings: RenderSettings.() -> RenderSettings = { this },
                          internal val configureTemplateFactory: TemplateFactorySettings.() -> TemplateFactorySettings = { this },
                          logger: Logger = Logger.getLogger(LiquidRuntimeEngine::class.java.name)
) : LiquidRenderEngine {

  private var templateFactory: TemplateFactory
  private var engine: TemplateEngine
  private var executorService: ExecutorService

  init {
    val cacheSetup: CacheSetup = object : CacheSetup {
      override fun accept(t: CacheBuilder<*, *>) {
        t.expireAfterWrite(12, TimeUnit.HOURS)
      }
    }

    val formattingFilters = MinecraftFormat.values()
        .map {
          MinecraftFormatFilter(it)
        }
        .toTypedArray()


    val formattingTags = MinecraftFormat.values()
        .map {
          MinecraftFormatTag(it)
        }
        .toTypedArray()

    // java time
    templateFactory = TemplateFactory.newBuilder()
        .withFilters(
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
        .withTags(*tags.toTypedArray(), *formattingTags)
        .withFilters(*filters.toTypedArray())
        .withFilters()
        .cacheSettings(cacheSetup)
        .flavor(Flavor.LIQUID)
        .maxTemplateSize(50000L)
        .configureTemplateFactory()
        .build()

    this.executorService = Executors.newFixedThreadPool(15)
    engine = TemplateEngine.newInstance(RenderSettings()
        .executor(executorService)
        .maxIterations(100)
        .maxStackSize(100)
        .strictVariables(false)
        .maxRenderTimeMillis(3000L)
        .configureRenderSettings())
  }

  internal fun renderWithContext(templateString: String, context: RenderContext): String {
    return executeWithContext(templateString, context).toNonNullString()
  }

  internal fun executeWithContext(templateString: String, context: RenderContext): Any? {
    val template = templateFactory.parse(templateString)
    return engine.executeWithContext(template, context)
  }

  override fun execute(templateString: String, player: Player): Any? {
    val renderModel = buildRenderContext(player)
    return executeWithContext(templateString, renderModel)
  }

  override fun render(templateString: String, model: ModelContributor): String {
    return renderWithContext(templateString, buildRenderContext(model))
  }

  override fun execute(templateString: String, model: ModelContributor): Any? {
    val context = buildRenderContext(model)
    return executeWithContext(templateString, context)
  }

  override fun render(templateString: String, player: Player): String {
    return execute(templateString, player).toNonNullString()
  }

  override fun execute(templateString: String): Any? {
    val renderModel = buildRenderContext()
    return executeWithContext(templateString, renderModel)
  }

  override fun render(templateString: String): String {
    return execute(templateString).toNonNullString()
  }

  override fun withSettings(configurer: RenderSettings.() -> Any?): LiquidRenderEngine {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  internal fun buildRenderContext(player: Player): RenderContext {
    return buildRenderContext { modelMap ->
      modelMap.player = player
      modelMap.server = player.server
      modelMap.world = player.world
    }
  }

  internal fun buildRenderContext(modelContributor: ModelContributor = {}): RenderContext {
    val model = LiquidModelMap({ property, self ->
      fallbackResolver(property, self)
    })

    val renderContext = engine.createRenderContext(model)

    placeholders.forEach {
      model.putSupplier(it.name, {model ->
        it.resolvePlaceholder(model.player)
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

    modelContributor(model)

    return renderContext
  }
}
