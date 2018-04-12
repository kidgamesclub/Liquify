package club.kidgames.liquid.plugin

import club.kidgames.liquid.merge.utils.SupplierMap
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import java.util.logging.Level
import java.util.logging.Logger

class LiquidPluginListener(val logger: Logger,
                           val liquidRuntime: LiquidRuntime) : Listener {

  @EventHandler
  fun onEnable(event: PluginEnableEvent) {
    if (event.plugin.isEnabled && event.plugin.name == "PlaceholderAPI") {
      logger.log(Level.INFO, "Enabling Placeholder API integrations")
      liquidRuntime.fallbackResolver = fallback@{ player, parent ->
        try {
          val placeholders = PlaceholderAPI.getPlaceholders()[parent]
          return@fallback when {
            placeholders != null -> SupplierMap.newInstance<String, Any?>(
                { key ->
                  val propName = key as String
                  // Make sure it's formatted per PlaceholderAPI
                  val placeholder = when (propName.startsWith("${parent}_")) {
                    true -> propName
                    false -> "${parent}_$propName"
                  }

                  placeholders.onPlaceholderRequest(player, placeholder)
                })
            else -> {
              val attempted = PlaceholderAPI.setPlaceholders(player, "%$parent%")
              when {
                attempted.startsWith("%") -> null
                else -> attempted
              }
            }
          }
        } catch (e: Exception) {
          logger.log(Level.SEVERE, "Failure resolving PlaceholderAPI placeholder", e)
          null
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  fun onPluginUnload(e: PluginDisableEvent) {
    if (e.plugin.name == "PlaceholderAPI") {
      liquidRuntime.fallbackResolver = defaultFallbackResolver
    }
  }
}
