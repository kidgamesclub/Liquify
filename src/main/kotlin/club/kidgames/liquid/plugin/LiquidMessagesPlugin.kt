package club.kidgames.liquid.plugin

import club.kidgames.liquid.api.LiquidExtenderType
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.external.EZPlaceholderHook
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

open class LiquidMessagesPlugin : JavaPlugin() {
  private var runtime: LiquidRuntime = LiquidRuntime(logger)

  override fun onEnable() {
    server.pluginManager.registerEvents(LiquidPluginListener(logger, runtime), this)
    if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
      // add a hook
      PlaceholderAPI.registerPlaceholderHook(this, object: EZPlaceholderHook(this, "snippet") {
        override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
          //If a known snippet name, otherwise render somethign else
          if (runtime.isRegistered(LiquidExtenderType.SNIPPET, identifier)) {
            return runtime.renderSnippet(player!!, identifier)
          } else {
            return null
          }
        }
      })
    }
  }

  override fun onDisable() {
    super.onDisable()
  }

  override fun onLoad() {
    runtime.isInitialized = true
  }
}
