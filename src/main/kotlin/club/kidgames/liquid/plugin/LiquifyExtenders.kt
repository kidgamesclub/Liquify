package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.FilterExtender
import club.kidgames.liquid.api.LiquidExtender
import club.kidgames.liquid.api.LiquidExtenderType
import club.kidgames.liquid.api.LiquidExtenderType.FILTER
import club.kidgames.liquid.api.LiquidExtenderType.PLACEHOLDER
import club.kidgames.liquid.api.LiquidExtenderType.SNIPPET
import club.kidgames.liquid.api.LiquidExtenderType.TAG
import club.kidgames.liquid.api.LiquifyExtenderRegistry
import club.kidgames.liquid.api.PlaceholderExtender
import club.kidgames.liquid.api.SnippetExtender
import club.kidgames.liquid.api.TagExtender
import club.kidgames.liquid.api.events.LiquidExtensionResult
import com.google.common.collect.HashMultimap
import liqp.filters.Filter
import liqp.tags.Tag
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger

data class LiquifyExtenders(private val logger: Logger,
                            private val onRegistered: Consumer<LiquidExtender>,
                            private val onFailedRegistration: Consumer<LiquidExtender> = Consumer {})
  : LiquifyExtenderRegistry {

  private val registeredPluginsByType: ExtendersByType<ByPluginName> = mutableMapOf()
  private val registeredExtendersByType: ExtendersByType<ByExtenderName> = mutableMapOf()
  private val conflictsByType: ExtendersByType<ByExtenderName> = mutableMapOf()

  internal val snippets: List<SnippetExtender>
    get() = ext(SNIPPET)

  internal val snippetMap: Map<String, String>
    get() = snippets.map { it.name to it.snippetText }.toMap()

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

  private inline fun <reified T:LiquidExtender> ext(type: LiquidExtenderType): List<T> {
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

  override fun unregisterPlugin(pluginId: String) {}

  private fun register(extension: LiquidExtender): LiquidExtensionResult {
    val plugins = plugins(extension.type)
    if (plugins[extension.pluginId].isNotEmpty()) {
      logger.log(Level.WARNING, "Duplicate extension ${extension.type} for ${extension.pluginId}: ${extension.name}")
      return LiquidExtensionResult.DUPLICATE
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

      return LiquidExtensionResult.CONFLICT
    }

    extenders[extension.name] = extension
    plugins.put(extension.pluginId, extension)

    onRegistered.accept(extension)

    logger.log(Level.WARNING, "Registered ${extension.name} of type ${extension.type} for ${extension.pluginId}")
    return LiquidExtensionResult.SUCCESS
  }
}

