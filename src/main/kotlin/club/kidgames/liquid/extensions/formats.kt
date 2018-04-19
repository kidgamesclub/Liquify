package club.kidgames.liquid.extensions

import club.kidgames.liquid.extensions.MinecraftFormatType.Color
import club.kidgames.liquid.extensions.MinecraftFormatType.Style
import liqp.nodes.RenderContext
import java.util.*

val MCFORMAT_VAR = "mc:format"

fun RenderContext.withMinecraftFormat(builder:StringBuilder, formats:Set<MinecraftFormat> = emptySet(), block: (StringBuilder)-> Unit):StringBuilder {
  return internalWithMinecraftFormat(this, builder, formats, true, {block(builder)})
}

fun RenderContext.withSimpleMinecraftFormat(builder:StringBuilder, formats:Set<MinecraftFormat> = emptySet(), block: (StringBuilder)-> Unit):StringBuilder {
  return internalWithMinecraftFormat(this, builder, formats, false, {block(builder)})
}


private fun internalWithMinecraftFormat(context: RenderContext, builder:StringBuilder, formats:Set<MinecraftFormat> = emptySet(), useStack:Boolean, block: ()-> Unit):StringBuilder {

  for (format in formats) {
    if(useStack) {
      context.pushFormat(format)
    }
    format.appendTo(builder)
  }

  block()

  for (format in formats) {
    if(useStack) {
      context.popFormat()
    }
  }

  context.appendFormats(builder)
  return builder
}

private fun RenderContext.getMFormats(): Deque<MinecraftFormat> {
  return this.get(MCFORMAT_VAR) ?: ArrayDeque<MinecraftFormat>().let { deque->
    this.set(MCFORMAT_VAR, deque)
    deque
  }
}

private fun RenderContext.appendFormats(builder:StringBuilder) {
  if (!builder.endsWith(MinecraftFormat.Reset.format())) {
    // This weird piece is because formats don't process on the stack.  Values of children are
    // calculated in advance and passed into the parent.
    builder.append(MinecraftFormat.Reset)
  }

  val colors = getMFormats().descendingIterator()
  while (colors.hasNext()) {
    val fmt = colors.next()
    if (fmt.type == Color) {
      builder.append(fmt)
      break
    }
  }

  val styles = getMFormats().descendingIterator()
  while (styles.hasNext()) {
    val fmt = styles.next()
    if (fmt.type == Style) {
      builder.append(fmt)
      break
    }
  }
}

fun RenderContext.pushFormat(format:MinecraftFormat) {
  getMFormats().addLast(format)
}

fun RenderContext.popFormat(){
  getMFormats().removeLast()
}


