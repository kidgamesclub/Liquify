package club.kidgames.liquid.liqp

import asString
import liqp.filters.FilterChainPointer
import liqp.filters.FilterParams
import liqp.filters.LFilter
import liqp.nodes.RenderContext
import java.util.concurrent.atomic.AtomicReference

const val filterFlagKey = "mcformat"

class MinecraftFormatFilter(val format: MinecraftFormat) : LFilter {

  private val formats = setOf(format)

  private val _name = format.name.decapitalize()

  override fun getName(): String {
    return _name
  }

  /**
   * This runs immediately after the content has been rendered.  It's the best time to
   */
  override fun onFilterAction(params: FilterParams, chain: FilterChainPointer, context: RenderContext, result: AtomicReference<Any?>) {
    chain.once(filterFlagKey, {
      context.currentFormatString(forceReset = false)
    })
  }

  /**
   * At the end of the filter chain, we write the format strings once
   */
  override fun onEndChain(params: FilterParams, chain: FilterChainPointer, context: RenderContext, result: AtomicReference<Any?>) {
    chain.with(filterFlagKey, { format:String->
      val resultString = result.asString()
      val builder = StringBuilder()
      if (resultString.isNotBlank()) {
        builder.append(format)
        builder.append(resultString)
        builder.append(context.currentFormatString(forceReset = true))
      }
      result.set(builder.toString())
    })
  }

  override fun doFilter(params: FilterParams,
                        chain: FilterChainPointer,
                        context: RenderContext,
                        result: AtomicReference<Any?>) {

    //
    // 1. Append the colors to the stack.  We do this before rendering so child tags can look them
    //    up and append to them.
    for (format in formats) {
      context.pushFormat(format)
    }

    //
    // 2. Process other filters
    chain.continueChain()

    //
    // 3. Remove formats from the render stack
    for (format in formats) {
      context.popFormat()
    }
  }
}
