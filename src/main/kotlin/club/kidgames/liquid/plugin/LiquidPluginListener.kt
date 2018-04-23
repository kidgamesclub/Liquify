package club.kidgames.liquid.plugin

import club.kidgames.integrations.LiquifyIntegrator
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

class LiquidPluginListener(private val integrator: LiquifyIntegrator) : Listener {

  @EventHandler
  fun onEnable(e: PluginEnableEvent) {
    val plugin = PluginInfo(e.plugin.name, e.plugin.dataFolder)
    integrator.integrateWith3rdPartyPlugin(plugin)
  }

  @EventHandler(priority = EventPriority.HIGH)
  fun onPluginUnload(e: PluginDisableEvent) {
    val plugin = PluginInfo(e.plugin.name, e.plugin.dataFolder)
    integrator.remove3rdPartyIntegration(plugin)
  }
}
