package club.kidgames.liquid.api

import club.kidgames.liquid.liqp.FallbackResolver
import club.kidgames.liquid.plugin.PluginInfo
import liqp.MutableParseSettings
import liqp.filters.Filter
import liqp.tags.Tag
import org.bukkit.plugin.PluginManager

/**
 * Extends the liquify plugin by adding one of the following capabilities:
 *
 * * Adds a new [Tag]
 * * Adds a new [Filter]
 * * Adds placeholder resolvers via [PlaceholderExtender], for known keys
 * * Adds fallback resolvers for dynamic keys via [FallbackResolver]
 */
interface LiquifyExtender {
  val pluginId: String
  val name: String
  val type: LiquidExtenderType
}

abstract class PlaceholderExtender(override val pluginId: String,
                                   override val name: String) : LiquifyExtender {
  final override val type = LiquidExtenderType.PLACEHOLDER
  abstract fun resolvePlaceholder(model: LiquidModelMap): Any?
}

data class FallbackResolverExtender(override val pluginId: String, val resolver: FallbackResolver) : LiquifyExtender {
  override val name = pluginId
  override val type = LiquidExtenderType.FALLBACK_RESOLVER
}

abstract class Liquify3rdPartyIntegrator(override val pluginId: String) : LiquifyExtender {
  override val name = pluginId
  override val type = LiquidExtenderType.INTEGRATOR
  abstract fun integratePlugin(renderer: LiquifyRenderer,
                               extenders: LiquifyExtenderRegistry,
                               pluginInfo: PluginInfo,
                               manager: PluginManager): LiquidIntegrationResult
}

data class SnippetExtender(override val pluginId: String,
                           override val name: String, val snippetText: String) : LiquifyExtender {
  override val type = LiquidExtenderType.SNIPPET
}

data class FilterExtender(override val pluginId: String,
                          override val name: String, val filter: Filter) : LiquifyExtender {
  override val type = LiquidExtenderType.FILTER
}

data class TagExtender(override val pluginId: String,
                       override val name: String, val tag: Tag) : LiquifyExtender {
  override val type = LiquidExtenderType.TAG
}




data class LiquidExtenderInfo(val pluginId: String, val name: String)

enum class LiquidExtenderType {
  SNIPPET,
  FILTER,
  TAG,
  PLACEHOLDER,
  FALLBACK_RESOLVER,
  INTEGRATOR,
  PARSE_SETTINGS,
  RENDER_SETTINGS
}

enum class LiquidExtensionResult {
  SUCCESS,
  DUPLICATE,
  CONFLICT,
  MISSING_DEPENDENCY,
  BLOCKED,
  ERROR
}

enum class LiquidIntegrationResult {
  SUCCESS,
  DUPLICATE,
  CONFLICT,
  BLOCKED,
  ERROR
}
