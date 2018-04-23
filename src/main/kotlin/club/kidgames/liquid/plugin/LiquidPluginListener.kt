package club.kidgames.liquid.plugin

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import java.util.logging.Logger

class LiquidPluginListener(val logger: Logger,
                           val integrator: LiquifyIntegrator,
                           val runtime:LiquidRuntime) : Listener {

  @EventHandler
  fun onEnable(event: PluginEnableEvent) {
    if (event.plugin.isEnabled && event.plugin.name == "PlaceholderAPI") {
      integrator.integratePlaceholderAPI()
    }
    if (event.plugin.isEnabled) {
      integrator.extractSnippets(PluginInfo(event.plugin.name, event.plugin.dataFolder))
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  fun onPluginUnload(e: PluginDisableEvent) {
    runtime.unregisterAll(e.plugin.name)
    if (e.plugin.name == "PlaceholderAPI") {
      runtime.resetFallbackResolver()
    }
  }
}
