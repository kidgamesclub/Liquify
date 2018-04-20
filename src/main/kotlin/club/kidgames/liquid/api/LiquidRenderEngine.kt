package club.kidgames.liquid.api;

import club.kidgames.liquid.extensions.ModelContributor
import liqp.RenderSettings
import org.bukkit.entity.Player

/**
 * Liquid text merging plugin.  This plugin uses liquid templating language to allow for robust rending capabilities.
 */
interface LiquidRenderEngine {
  fun execute(templateString: String, player: Player): Any?

  fun render(templateString: String, player: Player): String

  fun execute(templateString: String, model: ModelContributor): Any?

  fun render(templateString: String, model: ModelContributor): String

  fun execute(templateString: String): Any?

  fun render(templateString: String): String

  fun withSettings(configurer: RenderSettings.()->Any?): LiquidRenderEngine
}
