package club.kidgames.liquid.extensions

import club.kidgames.liquid.api.models.LiquidModelMap

typealias PluginName = String
typealias ExtensionName = String

typealias PropertyName = String
typealias FallbackResolver = (PropertyName, LiquidModelMap) -> Any?
typealias EntrySupplier = (LiquidModelMap) -> Any?
typealias ModelContributor = (LiquidModelMap)-> Unit
