package club.kidgames.liquid.api.context

import org.bukkit.Server
import org.bukkit.plugin.PluginManager

data class ServerContext(val server: Server,
                         val plugins: PluginManager): LiquidContext
