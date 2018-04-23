package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquifyExtenderRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import java.util.logging.Logger

class LiquidPluginListener(private val liquify: Liquify,
                           private val integrator: LiquifyIntegrator,
                           private val extenders:LiquifyExtenders) : Listener {

  @EventHandler
  fun onEnable(event: PluginEnableEvent) {
    val enabledPlugin = event.plugin
    if (enabledPlugin.isEnabled && enabledPlugin.name == "PlaceholderAPI") {
      integrator.integratePlaceholderAPI()
    }
    if (enabledPlugin.isEnabled) {
      integrator.extractSnippets(PluginInfo(enabledPlugin.name, enabledPlugin.dataFolder))
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  fun onPluginUnload(e: PluginDisableEvent) {
    val disabledPlugin = e.plugin
    extenders.unregisterPlugin(disabledPlugin.name)
    if (disabledPlugin.name == "PlaceholderAPI") {
      liquify.fallbackResolver = defaultFallbackResolver
    }
  }
}
