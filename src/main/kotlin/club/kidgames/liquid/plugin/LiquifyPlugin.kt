package club.kidgames.liquid.plugin

import club.kidgames.integrations.LiquifyIntegrator
import club.kidgames.integrations.PlaceholderAPIIntegrator
import org.bukkit.plugin.java.JavaPlugin

/**
 * Spigot plugin that renders liquid templates and snippets.  This class should be as minimal
 * as possible.  For any rendering concerns, use [LiquifyRenderer],
 * and for integrations, see [LiquifyIntegrator]
 */
open class LiquifyPlugin : JavaPlugin() {

  internal val liquify: Liquify = Liquify(logger, name, dataFolder)
  private val integrator: LiquifyIntegrator
  private val listener: LiquidPluginListener

  init {
    liquifyInstance = liquify

    val (renderer, extenders) = liquify
    integrator = LiquifyIntegrator(renderer, extenders, server.pluginManager, dataFolder, logger)
    listener = LiquidPluginListener(integrator)
  }

  internal fun reload():Boolean {
    unload()
    load()
    return true
  }

  internal fun unload() {
    integrator.unregisterAll()
    liquify.isInitialized = false
    liquify.unload()
    liquify.refresh()
  }

  internal fun load() {
    server.pluginManager.registerEvents(listener, this)

    // Built-in PlaceholderAPI integration
    val (renderer, extenders) = liquify
    extenders.registerIntegrator(PlaceholderAPIIntegrator(extenders, logger))

    this.getCommand("liquify").executor = LiquifyCommand(logger, liquify.renderer, this::reload)
    this.getCommand("lq").executor = LiquifyCommand(logger, liquify.renderer, this::reload)

    // Look for any plugins registered prior to this plugin
    server.pluginManager.plugins
        .filter { it.isEnabled }
        .forEach {
          val pluginInfo = PluginInfo(it.name, it.dataFolder)
          integrator.integrateWith3rdPartyPlugin(pluginInfo)
        }

    liquify.isInitialized = true
    liquify.refresh()
  }

  override fun onDisable() = this.unload()
  override fun onEnable() = this.load()
}
