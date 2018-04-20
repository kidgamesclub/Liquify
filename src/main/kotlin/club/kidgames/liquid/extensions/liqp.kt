import club.kidgames.liquid.extensions.MinecraftFormatTag
import liqp.nodes.AtomNode
import liqp.nodes.BlockNode
import liqp.nodes.LNode
import liqp.nodes.TagNode

/**
 * Flattens child block nodes, and removes all whitespace only atom nodes
 */
val Iterable<LNode>.flattened: List<LNode>
  get() {
    val children = mutableListOf<LNode>()
    this.forEach { token ->
      when (token) {
        is AtomNode -> {
          val value: String = token.get()
          if (value.trim().isNotEmpty()) {
            children.add(token)
          }
        }
        is BlockNode -> {
          children.addAll(token.children.flattened)
        }
        else -> {
          children.add(token)
        }
      }
    }

    return children
  }

/**
 * Whether a node is a minecraft format node
 */
val LNode.isMinecraftFormat: Boolean
  get() {
    return this is TagNode && this.tag is MinecraftFormatTag
  }

/**
 * Returns the current node as a minecraft tag, or error
 */
val LNode.minecraftFormat: MinecraftFormatTag
  get() {
    return when {
      this.isMinecraftFormat -> (this as TagNode).tag as MinecraftFormatTag
      else -> throw IllegalStateException("This node is not a minecraft tag node")
    }
  }
