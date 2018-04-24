package club.kidgames.liquid.plugin

import club.kidgames.liquid.liqp.MinecraftFormat
import club.kidgames.liquid.liqp.MinecraftFormat.*
import liqp.nodes.LookupNode
import liqp.nodes.RenderContext
import liqp.traverse.iterator
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.logging.Level
import java.util.logging.Logger

class LiquifyCommand(val logger: Logger,
                     val renderer: ReloadingLiquifyRenderer,
                     val reload: () -> Boolean) : CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {

    if (args.isEmpty()) {
      return false
    }

    return when (args.first()) {
      "reload" -> {
        sender.send("${Green.formatString}Liquify reloaded successfully")
        reload()
      }
      else -> resolveMessage(sender, args)
    }
  }

  fun resolveMessage(sender: CommandSender, args: Array<String>): Boolean {
    var template = args.joinToString(" ").trim()
    if (!template.startsWith("{")) {
      template = "{{$template}}"
    }

    sender.send("Template string: $Reset$Green$Bold $template")
    try {
      val context = sender.toRenderContext(renderer)
      // Create a matching render context - but in strict mode
      val strictContext = sender.toRenderContext(renderer.withRenderSettings {
        isStrictVariables = true
      })

      val result = renderer.executeWithContext(template, context)
      if (result is String? && result.isNullOrEmpty()) {
        sender.send("$Yellow  The template rendered an empty value")
      } else {
        sender.send("  Output:$Aqua$Bold $result")
      }

      if (result != null) {
        sender.send("  Result Type: $Aqua${result::class.java}")
      }

      sender.send("Variables: ")
      val templateInst = renderer.parser.parse(template)
      val numProcessed = templateInst
          .iterator<LookupNode>()
          .count { lookupNode ->
            try {
              val rendered = lookupNode.render(strictContext)
              if (rendered != null) {
                sender.send("$Green  $lookupNode -> $rendered (type: ${rendered::class.java})")
              } else {
                sender.send("$Yellow  $lookupNode -> $rendered")
              }

            } catch (e: Exception) {
              sender.send("$Red  $lookupNode -> Unable to resolve: $e")
              logger.log(Level.SEVERE, "Error resolving in strict mode: $e", e)
            }
            true
          }
      if (numProcessed == 0) {
        sender.send("$Italic  No variables to resolve")
      }
    } catch (e: Exception) {
      logger.log(Level.SEVERE, "Error rendering $e", e)
      sender.send("$Red Error while rendering: $Bold$e")
      return true
    }

    return true
  }
}

fun CommandSender.send(message: String) {
  when (this) {
    is Player -> this.sendRawMessage(message)
    else -> this.sendMessage(message)
  }
}

fun CommandSender.toRenderContext(renderer: ReloadingLiquifyRenderer): RenderContext {
  return when (this) {
    is Player -> renderer.newRenderContext({ map -> map.player = this })
    else -> renderer.newRenderContext()
  }
}
