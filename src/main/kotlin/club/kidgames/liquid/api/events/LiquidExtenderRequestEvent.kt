package club.kidgames.liquid.api.events

import club.kidgames.liquid.api.LiquidExtenderRegistry
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

data class LiquidExtenderRequestEvent(private val registry: LiquidExtenderRegistry)
  : Event(false),
    LiquidExtenderRegistry by registry {

  private val handlerList = HandlerList()

  override fun getHandlers(): HandlerList {
    return handlerList
  }
}
