package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.FilterExtender
import club.kidgames.liquid.api.LiquidExtender
import club.kidgames.liquid.api.LiquidExtenderRegistry
import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.api.LiquidExtenderType.FILTER
import club.kidgames.liquid.api.LiquidExtenderType.PLACEHOLDER
import club.kidgames.liquid.api.LiquidExtenderType.SNIPPET
import club.kidgames.liquid.api.LiquidExtenderType.TAG
import club.kidgames.liquid.api.LiquidRenderEngine
import club.kidgames.liquid.api.PlaceholderExtender
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.api.TagExtender
import club.kidgames.liquid.api.events.LiquidExtensionResult
import club.kidgames.liquid.api.events.LiquidExtensionResult.CONFLICT
import club.kidgames.liquid.api.events.LiquidExtensionResult.DUPLICATE
import club.kidgames.liquid.api.events.LiquidExtensionResult.SUCCESS
import club.kidgames.liquid.extensions.ExtensionName
import club.kidgames.liquid.extensions.FallbackResolver
import club.kidgames.liquid.extensions.ModelContributor
import club.kidgames.liquid.extensions.PluginName
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

typealias ByPluginName = Multimap<PluginName, LiquidExtender>
typealias ByExtenderName = MutableMap<ExtensionName, LiquidExtender>
typealias ExtendersByType<E> = MutableMap<LiquidExtenderType, E>

val defaultFallbackResolver: FallbackResolver = { _, _ -> null }
val liquidRuntimeInstance: LiquidRuntime = LiquidRuntime(
    Logger.getLogger(liquifyPluginName), liquifyPluginName, liquifyDataDir)

/**
 * Renders liquid templates for the Liquify plugin.  Internally, it maintains an engine instance that
 * can be rebuilt if configuration changes.
 *
 * Should stay agnostic of minecraft-specific types, including plugins.
 */
data class LiquidRuntime(var logger: Logger,
                         val name: String,
                         val dataFolder: File,
                         var isInitialized: Boolean = false,
                         private val registeredPluginsByType: ExtendersByType<ByPluginName> = mutableMapOf(),
                         private val registeredExtendersByType: ExtendersByType<ByExtenderName> = mutableMapOf(),
                         private val conflictsByType: ExtendersByType<ByExtenderName> = mutableMapOf()
) : LiquidExtenderRegistry, LiquidRenderEngine {

  /**
   * Backing property for fallbackResolver
   */
  private var _fallbackResolver: FallbackResolver = defaultFallbackResolver

  private var engine: LiquidRenderEngine = buildEngine()

  val snippets: Map<String, String>
    get() {
      return extenders(SNIPPET).values
          .map { it as SnippetExtender }
          .map { it.name to it.snippetText }
          .toMap()
    }

  var fallbackResolver: FallbackResolver
    set(fallbackResolver) {
      this._fallbackResolver = fallbackResolver
      engine = buildEngine()
    }
    get() {
      return _fallbackResolver
    }

  private fun conflicts(type: LiquidExtenderType): ByExtenderName {
    return conflictsByType.getOrPut(type, { mutableMapOf() })
  }

  private fun plugins(type: LiquidExtenderType): ByPluginName {
    return registeredPluginsByType.getOrPut(type, { HashMultimap.create() })
  }

  private fun extenders(type: LiquidExtenderType): ByExtenderName {
    return registeredExtendersByType.getOrPut(type, { mutableMapOf() })
  }

  override fun registerPlaceholder(extender: PlaceholderExtender) {
    register(extender)
  }

  override fun registerTag(extender: TagExtender) {
    register(extender)
  }

  override fun registerFilter(extender: FilterExtender) {
    register(extender)
  }

  override fun registerSnippet(extender: SnippetExtender) {
    register(extender)
  }

  override fun isRegistered(type: LiquidExtenderType, name: String): Boolean {
    return extenders(type).containsKey(name)
  }

  private fun buildEngine(): LiquidRuntimeEngine {
    return LiquidRuntimeEngine(
        tags = extenders(TAG).values.map { it as TagExtender }.map { it.tag },
        baseDir = dataFolder,
        filters = extenders(FILTER).values.map { it as FilterExtender }.map { it.filter },
        placeholders = extenders(PLACEHOLDER).values.map { it as PlaceholderExtender },
        snippets = extenders(SNIPPET).values.map { it as SnippetExtender },
        fallbackResolver = fallbackResolver,
        logger = logger)
  }

  fun register(extension: LiquidExtender): LiquidExtensionResult {
    val plugins = plugins(extension.type)
    if (plugins[extension.pluginId].isNotEmpty()) {
      logger.log(Level.WARNING, "Duplicate extension ${extension.type} for ${extension.pluginId}: ${extension.name}")
      return DUPLICATE
    } else {
      plugins.put(extension.pluginId, extension)
    }

    val extenders = extenders(extension.type)
    val conflicts = conflicts(extension.type)
    val existing = extenders[extension.name]

    if (existing != null && existing.pluginId != extension.pluginId) {
      conflicts[extension.name] = extension
      conflicts[extension.name] = existing
      logger.log(Level.WARNING, "Conflict for ${extension.name} of type ${extension.type} between ${extension.pluginId} and ${existing.pluginId}")

      return CONFLICT
    }

    extenders[extension.name] = extension
    plugins.put(extension.pluginId, extension)

    if (isInitialized) {
      this.engine = buildEngine()
    }

    logger.log(Level.WARNING, "Registered ${extension.name} of type ${extension.type} for ${extension.pluginId}")
    return SUCCESS
  }

  override fun execute(template: String, vararg modelContributor: ModelContributor): Any? {
    return engine.execute(template, *modelContributor)
  }

  override fun render(template: String, vararg modelContributor: ModelContributor): String {
    return engine.render(template, *modelContributor)
  }

  fun unregisterAll(pluginName: String?) {
  }

  fun resetFallbackResolver() {
    this.fallbackResolver = defaultFallbackResolver
  }

  companion object {
    @JvmStatic
    val renderer: LiquidRenderEngine
      get() = liquidRuntimeInstance

    @JvmStatic
    val registry: LiquidExtenderRegistry
      get() = liquidRuntimeInstance
  }
}
