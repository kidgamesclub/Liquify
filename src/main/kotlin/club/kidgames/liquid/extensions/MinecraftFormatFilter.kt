package club.kidgames.liquid.extensions

import liqp.filters.Filter
import liqp.filters.FilterChain
import liqp.filters.FilterParams
import liqp.nodes.RenderContext
import java.util.concurrent.atomic.AtomicReference

class MinecraftFormatFilter(val format: MinecraftFormat) : Filter(format.name.decapitalize()) {

  private val formatAsSet = setOf(format)


  /**
   * Applies the filter on the 'value', with the given 'context'.
   *
   * @param value   the string value `AAA` in: `{{ 'AAA' | f:1,2,3 }}`
   * @param context the template context.
   * @param params  the values [1, 2, 3] in: `{{ 'AAA' | f:1,2,3 }}`
   *
   * @return the result of the filter.
   */
  override fun apply(value: Any?, context: RenderContext, vararg params: Any): Any? {
    return when {
      value == null -> value
      value.toString().isBlank() -> value
      else -> {
        context.withMinecraftFormat(formatAsSet,
            forceFormat = true,
            isReset = true,
            renderBlock = {
              value.toString()
            })
      }
    }
  }

  override fun doFilterEnd(params: FilterParams, chain: FilterChain, context: RenderContext, result: AtomicReference<Any?>) {

  }

  override fun doStartFilterChain(params: FilterParams, chain: FilterChain, context: RenderContext, result: AtomicReference<Any?>) {

  }

  override fun doFilter(params: FilterParams, chain: FilterChain, context: RenderContext, result: AtomicReference<Any?>) {

  }
}
