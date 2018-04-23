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

  private val liquify: Liquify
  private val integrator: LiquifyIntegrator

  init {
    liquify = Liquify(logger, name, dataFolder)
    liquifyInstance = liquify

    val (renderer, extenders) = liquify
    integrator = LiquifyIntegrator(renderer, extenders, server.pluginManager, dataFolder, logger)

    // By default, integrates PlaceholderAPI
    extenders.registerIntegrator(PlaceholderAPIIntegrator(extenders, logger))
  }

  override fun onEnable() {
    server.pluginManager.registerEvents(LiquidPluginListener(integrator), this)

    this.getCommand("liquify").executor = LiquifyCommand(liquify.renderer)
    this.getCommand("lq").executor = LiquifyCommand(liquify.renderer)

    // Look for any plugins registered prior to this plugin
    server.pluginManager.plugins
        .filter { it.isEnabled }
        .forEach {
          val pluginInfo = PluginInfo(it.name, it.dataFolder)
          integrator.integrateWith3rdPartyPlugin(pluginInfo)
        }
  }

  override fun onLoad() {
    liquify.isInitialized = true
  }
}
