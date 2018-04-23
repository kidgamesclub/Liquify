package club.kidgames.liquid.plugin

import org.bukkit.plugin.java.JavaPlugin

/**
 * Spigot plugin that renders liquid templates and snippets.  This class should be as minimal
 * as possible.  For any rendering concerns, use {@see LiquidRuntime}, and for integrations,
 * see {@link LiquifyIntegrator}
 */
open class LiquifyPlugin : JavaPlugin() {

  private val runtime: LiquidRuntime
  private val integrator: LiquifyIntegrator

  init {
    liquidRuntimeInstance.logger = logger
    runtime = liquidRuntimeInstance
    integrator = LiquifyIntegrator(runtime, logger)
  }

  override fun onEnable() {
    server.pluginManager.registerEvents(LiquidPluginListener(logger, integrator, runtime), this)
    this.getCommand("liquify").executor = LiquifyCommand(runtime)
    this.getCommand("lq").executor = LiquifyCommand(runtime)
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
    runtime.isInitialized = true
  }
}
