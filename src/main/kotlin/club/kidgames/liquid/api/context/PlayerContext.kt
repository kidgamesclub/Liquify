package club.kidgames.liquid.api.context

import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.PluginManager

data class PlayerContext(val plugins: PluginManager,
                         val server: Server,
                         val player: Player): LiquidContext
