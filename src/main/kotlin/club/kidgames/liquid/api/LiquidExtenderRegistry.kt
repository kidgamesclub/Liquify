package club.kidgames.liquid.api;

/**
 * Liquid text merging plugin.  This plugin uses liquid templating language to allow for robust rending capabilities.
 */

interface LiquidExtenderRegistry {
  fun registerPlaceholder(extender: PlaceholderExtender)
  fun registerTag(extender: TagExtender)
  fun registerFilter(extender: FilterExtender)
  fun registerSnippet(extender: SnippetExtender)
  fun isRegistered(type: LiquidExtenderType, name: String): Boolean
}
