package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidRenderEngine
import club.kidgames.liquid.api.PlaceholderExtender
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.merge.filters.collections.CommaSeparatedFilter
import club.kidgames.liquid.merge.filters.colors.DarkenFilter
import club.kidgames.liquid.merge.filters.colors.MinecraftChatFormat
import club.kidgames.liquid.merge.filters.colors.MinecraftColorize
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
import club.kidgames.liquid.merge.utils.SupplierMap
import com.google.common.cache.CacheBuilder
import liqp.CacheSetup
import liqp.RenderSettings
import liqp.TemplateEngine
import liqp.TemplateFactory
import liqp.filters.Filter
import liqp.nodes.RenderContext
import liqp.parser.Flavor
import liqp.tags.Tag
import liqp.toNonNullString
import lombok.extern.slf4j.Slf4j
import org.bukkit.entity.Player
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Liquid text merging plugin.  This plugin uses liquid templating language to allow for robust rending capabilities.
 */
@Slf4j
class LiquidRuntimeEngine(tags: List<Tag> = listOf(),
                          filters: List<Filter> = listOf(),
                          private val placeholders: List<PlaceholderExtender> = listOf(),
                          private val snippets: List<SnippetExtender> = listOf(),
                          internal var fallbackResolver: FallbackResolver = defaultFallbackResolver,
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
            MinecraftChatFormat(),
            MinecraftColorize())
        .withTags(*tags.toTypedArray())
        .withFilters(*filters.toTypedArray())
        .cacheSettings(cacheSetup)
        .flavor(Flavor.LIQUID)
        .maxTemplateSize(50000L)
        .build()

    this.executorService = Executors.newFixedThreadPool(15)
    engine = TemplateEngine.newInstance(RenderSettings()
        .executor(executorService)
        .maxIterations(100)
        .maxStackSize(100)
        .strictVariables(false)
        .maxRenderTimeMillis(3000L))
  }

  internal fun renderWithContext(templateString: String, context: RenderContext): String {
    return executeWithContext(templateString, context).toNonNullString()
  }

  internal fun executeWithContext(templateString: String, context: RenderContext): Any? {
    val template = templateFactory.parse(templateString)
    return engine.executeWithContext(template, context)
  }

  override fun execute(templateString: String, player: Player): Any? {
    val renderModel = buildRenderModel(player)
    return executeWithContext(templateString, renderModel)
  }

  override fun render(templateString: String, player: Player): String {
    return execute(templateString, player).toNonNullString()
  }

  override fun execute(templateString: String): Any? {
    val renderModel = buildRenderModel()
    return executeWithContext(templateString, renderModel)
  }

  override fun render(templateString: String): String {
    return execute(templateString).toNonNullString()
  }

  override fun withSettings(configurer: RenderSettings.() -> Any?): LiquidRenderEngine {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  internal fun buildRenderModel(player: Player? = null): RenderContext {
    val model: RenderModel = SupplierMap.newInstance<String, Any?>({ property ->
      fallbackResolver(player, property as String)
    })
    val renderContext = engine.createRenderContext(model)

    placeholders.forEach {
      model.putSupplier(it.name, {
        it.resolvePlaceholder(player)
      })
    }

    model.putSupplier("snippets", snippet@{
      val snippetMap = SupplierMap.newInstance<String, Any?>()
      snippets.forEach {

        snippetMap.putSupplier(it.name, {
          this.executeWithContext(it.snippetText, renderContext)
        })
      }
      return@snippet snippetMap
    })

    if (player != null) {
      model["player"] = player
      model["server"] = player.server
      model["world"] = player.world
    }

    return renderContext
  }
}

typealias RenderModel = SupplierMap<String, Any?>
typealias FallbackResolver = (Player?, String) -> Any?
