package club.kidgames.liquid.liqp

import flattened
import isMinecraftFormat
import liqp.nodes.LNode
import liqp.nodes.RenderContext
import liqp.tags.CustomTag
import liqp.tags.Tag
import minecraftFormat

/**
 * A tag that renders minecraft color, ensuring to reset and reapply previous colors after a tag's execution is complete.
 * @property nestled A nestled tag is a format tag that is the only non-whitespace child of another format tag
 * @property format The enum value for this tag
 * @property isReset Whether this tag needs to perform a reset when it completes rendering.
 */
class MinecraftFormatTag(val format: MinecraftFormat,
                         private val nestled: MinecraftFormatTag? = null) : CustomTag(format.name.decapitalize()) {
  private val formatAsSet = setOf(format)
  private var _isReset: Boolean = true
  private var isReset: Boolean
    get() {
      return _isReset
    }
    set(isReset) {
      this._isReset = isReset
      if (!isReset && nestled != null) {
        // If a tag is nestled, notify it that it does not need to perform a reset when it
        // finishes rendering
        nestled.isReset = false
      }
    }

  init {
    nestled?.isReset = false
  }

  /**
   * Determines if this tag has "nestled" children.
   */
  override fun createTagForNode(vararg tokens: LNode): Tag {
    val flattened = listOf(*tokens).flattened
    return when {
      flattened.size == 1 && flattened[0].isMinecraftFormat -> {
        // These tags should be collapsed
        MinecraftFormatTag(format, flattened[0].minecraftFormat)
      }
      else -> this
    }
  }

  /**
   * Renders this tag.
   */
  override fun render(context: RenderContext, vararg nodes: LNode): Any {
    return context.withMinecraftFormat(formatAsSet,
        isReset = this.isReset,
        renderBlock = {
          nodes.joinToString(transform = { node ->
            node.render(context).toString()
          })
        })
  }
}
