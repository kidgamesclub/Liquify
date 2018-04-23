package club.kidgames.liquid.extensions

import club.kidgames.liquid.api.models.LiquidModelMap
import liqp.MutableParseSettings
import liqp.MutableRenderSettings

typealias PluginName = String
typealias ExtensionName = String

typealias PropertyName = String
typealias FallbackResolver = (PropertyName, LiquidModelMap) -> Any?
typealias EntrySupplier = (LiquidModelMap) -> Any?
typealias ModelContributor = (LiquidModelMap)-> Unit

typealias RenderConfiguration = MutableRenderSettings.()->MutableRenderSettings
typealias ParseConfiguration = MutableParseSettings.()->MutableParseSettings
