package club.kidgames.liquid.api.context

import liqp.lookup.HasProperties

data class LiquidExtensionContext<C : LiquidContext>(
    val contextType: Class<C>,
    val namespace: String,
    val model: HasProperties) : LiquidContext
