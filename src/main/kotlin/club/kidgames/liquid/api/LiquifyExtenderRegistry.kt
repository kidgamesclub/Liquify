package club.kidgames.liquid.api;

import club.kidgames.liquid.liqp.FallbackResolver

/**
 * Liquid text merging plugin.  This plugin uses liquid templating language to allow for robust rending capabilities.
 */
interface LiquifyExtenderRegistry {

  fun registerFallbackResolver(extender: FallbackResolverExtender) {
    register(extender)
  }

  fun registerPlaceholder(extender: PlaceholderExtender) {
    register(extender)
  }

  fun registerTag(extender: TagExtender) {
    register(extender)
  }

  fun registerFilter(extender: FilterExtender) {
    register(extender)
  }

  fun registerIntegrator(integrator:Liquify3rdPartyIntegrator) {
    register(integrator)
  }

  fun registerParseSettings(parseSettings: ParseSettingsExtender) {
    register(parseSettings)
  }

  fun registerRenderSettings(renderSettings: RenderSettingsExtender) {
    register(renderSettings)
  }

  /**
   * Registers an extender with this instance of the liquify plugin.
   */
  fun <E: LiquifyExtender> register(extender:E): LiquidExtensionResult

  fun registerSnippet(extender: SnippetExtender) {
    register(extender)
  }

  fun isRegistered(type: LiquidExtenderType, name: String): Boolean
  fun unregisterPlugin(pluginId: String)
}
