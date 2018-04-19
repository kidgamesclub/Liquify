package club.kidgames.liquid.extensions

import liqp.nodes.AtomNode
import liqp.nodes.LNode
import liqp.nodes.RenderContext
import liqp.tags.Tag

class MinecraftBaseFormatTag(format:MinecraftFormat) : Tag(format.name.decapitalize()) {

  val formatAsSet = setOf(format)

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
        context.withMinecraftFormat(StringBuilder(), formatAsSet, {output->
          output.append(nodes[0].render(context))
        })
      }
      else-> throw IllegalStateException("This tag should only have two parameters")
    }
  }


}
