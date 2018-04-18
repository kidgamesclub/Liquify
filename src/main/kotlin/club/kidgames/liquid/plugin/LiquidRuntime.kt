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
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import org.bukkit.entity.Player
import java.util.logging.Logger

typealias ByPluginName = Multimap<PluginName, LiquidExtender>
typealias ByExtenderName = MutableMap<ExtensionName, LiquidExtender>
typealias ExtendersByType<E> = MutableMap<LiquidExtenderType, E>

val defaultFallbackResolver: FallbackResolver = { player, key -> null }


/**
 * Liquid text merging plugin.  This plugin uses liquid templating language to allow for robust rending capabilities.
 */
class LiquidRuntime(private val logger: Logger) : LiquidExtenderRegistry, LiquidRenderEngine by instance.engine {
  companion object {
    internal var instance: LiquidRuntime = LiquidRuntime(Logger.getLogger("LiquidRuntime"))

    @JvmStatic
    val engine: LiquidRenderEngine
      get() = instance

    @JvmStatic
    val registry: LiquidExtenderRegistry
      get() = instance
  }

  var isInitialized = false
  internal var fallbackResolver: FallbackResolver = defaultFallbackResolver
  internal val registeredPluginsByType: ExtendersByType<ByPluginName> = mutableMapOf()
  internal val registeredExtendersByType: ExtendersByType<ByExtenderName> = mutableMapOf()
  internal val conflictsByType: ExtendersByType<ByExtenderName> = mutableMapOf()
  internal var engine: LiquidRuntimeEngine = buildEngine()

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
        extenders(TAG).values.map { it as TagExtender }.map { it.tag },
        extenders(FILTER).values.map { it as FilterExtender }.map { it.filter },
        extenders(PLACEHOLDER).values.map { it as PlaceholderExtender },
        extenders(SNIPPET).values.map { it as SnippetExtender },
        fallbackResolver,
        logger)
  }

  fun register(extension: LiquidExtender): LiquidExtensionResult {
    val plugins = plugins(extension.type)
    if (plugins[extension.pluginId].isNotEmpty()) {
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
      return CONFLICT
    }

    extenders[extension.name] = extension
    plugins.put(extension.pluginId, extension)

    if (isInitialized) {
      this.engine = buildEngine()
    }

    return SUCCESS
  }

  fun renderSnippet(player: Player?, snippetId: String): String {
    val snippet = extenders(SNIPPET)[snippetId] as SnippetExtender
    return when (player) {
      null -> render(snippet.snippetText)
      else -> render(snippet.snippetText, player)
    }
  }
}
