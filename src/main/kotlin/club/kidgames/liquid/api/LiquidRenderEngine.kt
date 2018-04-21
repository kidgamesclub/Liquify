package club.kidgames.liquid.api

import club.kidgames.liquid.api.models.LiquidModelMap
import club.kidgames.liquid.extensions.ModelContributor
import org.bukkit.entity.Player
import java.util.function.Consumer

/**
 * Liquid text merging plugin.  This plugin uses liquid templating language to allow for robust rending capabilities.
 */
interface LiquidRenderEngine {
  fun execute(template: String, player: Player): Any? {
    return this.execute(template, { model: LiquidModelMap -> model.player = player })
  }

  fun render(template: String, player: Player): String {
    return render(template, { model -> model.player = player })
  }

  fun execute(template: String, modelContributor: List<Consumer<LiquidModelMap>>): Any? {
    return this.execute(template, *modelContributor
        .map { { model: LiquidModelMap -> it.accept(model) } }
        .toTypedArray())
  }

  fun render(template: String, modelContributor: List<Consumer<LiquidModelMap>>): String {
    return this.render(template, *modelContributor
        .map { { model: LiquidModelMap -> it.accept(model) } }
        .toTypedArray())
  }

  fun execute(template: String, vararg modelContributor: ModelContributor): Any?

  fun render(template: String, vararg modelContributor: ModelContributor): String
}
