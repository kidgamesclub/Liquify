package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.FallbackResolverExtender
import club.kidgames.liquid.api.FilterExtender
import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.api.LiquidExtenderType.FALLBACK_RESOLVER
import club.kidgames.liquid.api.LiquidExtenderType.FILTER
import club.kidgames.liquid.api.LiquidExtenderType.INTEGRATOR
import club.kidgames.liquid.api.LiquidExtenderType.PARSE_SETTINGS
import club.kidgames.liquid.api.LiquidExtenderType.PLACEHOLDER
import club.kidgames.liquid.api.LiquidExtenderType.RENDER_SETTINGS
import club.kidgames.liquid.api.LiquidExtenderType.SNIPPET
import club.kidgames.liquid.api.LiquidExtenderType.TAG
import club.kidgames.liquid.api.LiquidExtensionResult
import club.kidgames.liquid.api.LiquidExtensionResult.CONFLICT
import club.kidgames.liquid.api.LiquidExtensionResult.DUPLICATE
import club.kidgames.liquid.api.LiquidExtensionResult.SUCCESS
import club.kidgames.liquid.api.Liquify3rdPartyIntegrator
import club.kidgames.liquid.api.LiquifyExtender
import club.kidgames.liquid.api.LiquifyExtenderRegistry
import club.kidgames.liquid.api.ParseSettingsExtender
import club.kidgames.liquid.api.PlaceholderExtender
import club.kidgames.liquid.api.RenderSettingsExtender
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.api.TagExtender
import club.kidgames.liquid.liqp.FallbackResolver
import com.google.common.collect.HashMultimap
import liqp.filters.Filter
import liqp.tags.Tag
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger

data class LiquifyExtenders(private val logger: Logger,
                            private val onRegistered: Consumer<LiquifyExtender>,
                            private val onFailedRegistration: BiConsumer<LiquidExtensionResult, LiquifyExtender> = BiConsumer { _, _ -> })
  : LiquifyExtenderRegistry {

  private val registeredPluginsByType: ExtendersByType<ByPluginName> = mutableMapOf()
  private val registeredExtendersByType: ExtendersByType<ByExtenderName> = mutableMapOf()
  private val conflictsByType: ExtendersByType<ByExtenderName> = mutableMapOf()

  internal val snippets: List<SnippetExtender>
    get() = ext(SNIPPET)

  internal val renderSettings: List<RenderSettingsExtender>
    get() = ext(RENDER_SETTINGS)

  internal val parseSettings: List<ParseSettingsExtender>
    get() = ext(PARSE_SETTINGS)

  internal val snippetMap: Map<String, String>
    get() = snippets.map { it.name to it.snippetText }.toMap()

  internal val fallbackResolvers: List<FallbackResolver>
    get() {
      val fallbacks: List<FallbackResolverExtender> = ext(FALLBACK_RESOLVER)
      return fallbacks.map { it.resolver }
    }

  internal val placeholders: List<PlaceholderExtender>
    get() = ext(PLACEHOLDER)

  internal val tags: List<Tag>
    get() {
      val tagExt: Iterable<TagExtender> = ext(TAG)
      return tagExt.map { it.tag }
    }

  internal val filters: List<Filter>
    get() {
      val tagExt: Iterable<FilterExtender> = ext(FILTER)
      return tagExt.map { it.filter }
    }

  internal val integrators: Map<String, Liquify3rdPartyIntegrator>
    get() = extenders(INTEGRATOR) as Map<String, Liquify3rdPartyIntegrator>

  private inline fun <reified T : LiquifyExtender> ext(type: LiquidExtenderType): List<T> {
    return extenders(type).values.map { it as T }
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

  override fun isRegistered(type: LiquidExtenderType, name: String): Boolean {
    return extenders(type).containsKey(name)
  }

  override fun unregisterPlugin(pluginId: String) {}

  /**
   * Registers an extender with this instance of the liquify plugin.  Invokes the [onRegistered] if
   * the plugin was registered successfully, otherwise [onFailedRegistration]
   */
  override fun <E : LiquifyExtender> register(extender: E): LiquidExtensionResult {

    val plugins = plugins(extender.type)
    if (plugins[extender.pluginId].isNotEmpty()) {
      logger.log(Level.WARNING, "Duplicate extension ${extender.type} for ${extender.pluginId}: ${extender.name}")
      onFailedRegistration.accept(DUPLICATE, extender)
      return DUPLICATE
    } else {
      plugins.put(extender.pluginId, extender)
    }

    val extenders = extenders(extender.type)
    val conflicts = conflicts(extender.type)
    val existing = extenders[extender.name]

    if (existing != null && existing.pluginId != extender.pluginId) {
      conflicts[extender.name] = extender
      conflicts[extender.name] = existing
      logger.log(Level.WARNING, "Conflict for ${extender.name} of type ${extender.type} between ${extender.pluginId} and ${existing.pluginId}")
      onFailedRegistration.accept(CONFLICT, extender)
      return CONFLICT
    }

    extenders[extender.name] = extender
    plugins.put(extender.pluginId, extender)

    onRegistered.accept(extender)

    logger.log(Level.WARNING, "Registered ${extender.name} of type ${extender.type} for ${extender.pluginId}")
    return SUCCESS
  }
}

