package club.kidgames.liquid.extensions

import liqp.nodes.AtomNode
import liqp.nodes.AttributeNode
import liqp.nodes.LNode
import liqp.nodes.RenderContext
import liqp.tags.Tag

class MinecraftFormatTag : Tag("mc") {
  /**
   * Renders this tag.
   *
   * @param context the context (variables) with which this node should be rendered.
   * @param nodes   the nodes of this tag is created with. See the file `src/grammar/LiquidWalker.g` to see how each of
   * the tags is created.
   *
   * @return an Object denoting the rendered AST.
   */
  override fun render(context: RenderContext, vararg nodes: LNode): Any {

    return when (nodes.size) {
      0-> ""
      1-> {
        nodes[0].render(context)
      }
      2-> {
        val node = nodes[0] as AtomNode
        val tagData = node.render(context)?.toString()
        val formats = tagData?.let {
          tagData.split(",")
              .map { it.trim().removeSurrounding("'").removeSurrounding("\"").split(" ") }
              .flatten()
              .mapNotNull { MinecraftFormat.findFormat(it) }
              .toSet()
        } ?: setOf()

        context.withMinecraftFormat(StringBuilder(), formats, {output->
          output.append(nodes[1].render(context))
        })
      }
      else-> throw IllegalStateException("This tag should only have two parameters")
    }
  }


}
