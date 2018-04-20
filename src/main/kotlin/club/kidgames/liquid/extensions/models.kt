package club.kidgames.liquid.extensions

import club.kidgames.liquid.api.models.LiquidModelMap
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player

var LiquidModelMap.player: Player?
  get() {
    val player: Player by this
    return player
  }
  set(player) {
    this["player"] = player
  }

var LiquidModelMap.server: Server?
  get() {
    val server: Server? by this
    return server
  }
  set(server) {
    this["server"] = server
  }

var LiquidModelMap.world: World?
  get() {
    val world: World? by this
    return world
  }
  set(world) {
    this["world"] = world
  }

