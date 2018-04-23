package club.kidgames.liquid.plugin

import org.bukkit.plugin.java.JavaPlugin

/**
 * Spigot plugin that renders liquid templates and snippets.  This class should be as minimal
 * as possible.  For any rendering concerns, use {@see LiquidRuntime}, and for integrations,
 * see {@link LiquifyIntegrator}
 */
open class LiquifyPlugin : JavaPlugin() {

  private val liquify: Liquify
  private val integrator: LiquifyIntegrator

  init {
    liquifyInstance.logger = logger
    liquify = liquifyInstance
    integrator = LiquifyIntegrator(liquify, logger)
  }

  override fun onEnable() {
    server.pluginManager.registerEvents(LiquidPluginListener(liquify, integrator, liquify.extenders), this)
    this.getCommand("liquify").executor = LiquifyCommand(liquify.renderer)
    this.getCommand("lq").executor = LiquifyCommand(liquify.renderer)
    if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
      integrator.integratePlaceholderAPI()
    }
    server.pluginManager.plugins
        .filter { it.isEnabled }
        .forEach {
          integrator.extractSnippets(PluginInfo(it.name, it.dataFolder))
        }
  }

  override fun onLoad() {
    liquify.isInitialized = true
  }
}
