package club.kidgames.liquid.api.context

import org.bukkit.event.Event

data class EventContext<out E:Event>(val event:E): LiquidContext
