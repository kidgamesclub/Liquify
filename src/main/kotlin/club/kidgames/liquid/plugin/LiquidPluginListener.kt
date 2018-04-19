package club.kidgames.liquid.plugin

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import java.util.logging.Logger

class LiquidPluginListener(val logger: Logger,
                           val plugin: LiquifyPlugin) : Listener {

  @EventHandler
  fun onEnable(event: PluginEnableEvent) {
    if (event.plugin.isEnabled && event.plugin.name == "PlaceholderAPI") {
      plugin.integratePlaceholderAPI()
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  fun onPluginUnload(e: PluginDisableEvent) {
    if (e.plugin.name == "PlaceholderAPI") {
      plugin.resetFallbackResolver()
    }
  }
}
