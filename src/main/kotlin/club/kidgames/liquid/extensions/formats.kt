package club.kidgames.liquid.extensions

import club.kidgames.liquid.extensions.MinecraftFormat.*
import club.kidgames.liquid.extensions.MinecraftFormatType.Color
import club.kidgames.liquid.extensions.MinecraftFormatType.Style
import liqp.nodes.RenderContext
import java.util.*

val MCFORMAT_VAR = "mc:format"

typealias RenderBlock = () -> String

/**
 * Renders child nodes with minecraft formatting
 *
 * @param formats The formats being added to the formatting stack
 * @param isReset Whether we should perform a reset after rendering
 * @param forceFormat Whether to force rendering of colors (otherwise, it may be deferred to another tag)
 * @param renderBlock The block of code that performs the actual rendering.
 */
fun RenderContext.withMinecraftFormat(formats: Set<MinecraftFormat> = emptySet(),
                                      isReset: Boolean,
                                      forceFormat: Boolean = false,
                                      renderBlock: RenderBlock): StringBuilder {

  val output = StringBuilder()
  val context = this

  //
  // 1. Append the colors to the stack.  We do this before rendering so child tags can look them
  //    up and append to them.
  for (format in formats) {
    context.pushFormat(format)
  }

  //
  // 2. Render the format string into a variable.  Rendering children can change the format
  //    string, so we capture it before rendering
  val formatString = context.currentFormatString(forceReset = false)

  //
  // 3. Render the block, and capture the output.  We'll make decisions based on what was
  //    rendered
  val childOutput = renderBlock()

  //
  // 4. If necessary, write the formatString.  We don't write the format string if the child node
  //    already appended colors.
  val childRenderedFormat = childOutput.startsWith(FORMAT_CHAR)
  if (!childRenderedFormat || forceFormat) {
    output.append(formatString)
  }

  //
  // 5. Append the child node output
  output.append(childOutput)

  //
  // 6. Remove formats from the render stack
  for (format in formats) {
    context.popFormat()
  }


  //
  // 7. If necessary, restore color formats for future content, by rendering a reset character
  //    followed by the previous formats
  if (isReset) {
    val resetFormatString = context.currentFormatString(childOutput, true)
    output.append(resetFormatString)
  }

  return output
}

private val RenderContext.minecraftFormatStack: Deque<MinecraftFormat>
  get() {
    return this.get(MCFORMAT_VAR) ?: ArrayDeque<MinecraftFormat>().let { deque ->
      this.set(MCFORMAT_VAR, deque)
      deque
    }
  }

val RenderContext.minecraftColor:MinecraftFormat
  get() {
    return minecraftFormatStack
        .firstOrNull { format -> format.type == Color }
    ?: NoColor
  }

val RenderContext.minecraftStyle:MinecraftFormat
  get() {
    return minecraftFormatStack
        .firstOrNull { format -> format.type == Style }
    ?: NoStyle
  }

/**
 * Returns the current minecraft format string
 */
private fun RenderContext.currentFormatString(prev: String = "", forceReset: Boolean): String {
  val builder = StringBuilder()

  if ((forceReset || !prev.isEmpty()) && !prev.endsWith(Reset.formatString)) {
    // This weird piece is because formats don't process on the stack.  Values of children are
    // calculated in advance and passed into the parent.
    builder.append(Reset.formatString)
  }

  minecraftColor.appendTo(builder)
  minecraftStyle.appendTo(builder)
  return builder.toString()
}

fun RenderContext.pushFormat(format: MinecraftFormat) {
  minecraftFormatStack.push(format)
}

fun RenderContext.popFormat() {
  minecraftFormatStack.pop()
}


