package club.kidgames.liquid.api.models

import club.kidgames.liquid.extensions.EntrySupplier
import club.kidgames.liquid.extensions.FallbackResolver
import liqp.nodes.RenderContext
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Supplier

/**
 * Simple implementation that allows you to provide a supplier for a key, and the first time that map key is accessed,
 * the supplier will be invoked.  Also supports a default supplier, which is invoked for non-defined map keys, and the
 * value is cached thereafter.
 *
 * The idea is to defer all calculations as late as possible, in the case that map entries are expensive to retrieve
 * or calculate, and you don't know which ones will be needed in advance.
 */
class LiquidModelMap : HashMap<String, Any?> {
  private val keySuppliers = HashMap<String, EntrySupplier>()
  private val defaultSupplier: FallbackResolver

  constructor(defaultSupplier: FallbackResolver) {
    this.defaultSupplier = defaultSupplier
  }

  constructor() {
    this.defaultSupplier = { key, self -> null }
  }

  fun putSupplier(key: String, supplier: Supplier<Any?>): LiquidModelMap {
    this.keySuppliers[key] = { _ -> supplier.get() }
    return this
  }

  fun putSupplier(key: String, supplier: EntrySupplier): LiquidModelMap {
    this.keySuppliers[key] = supplier
    return this
  }

  @Synchronized
  override operator fun get(key: String): Any? {
    if (!this.containsKey(key)) {
      synchronized(this) {
        // Not doing a check inside the synchronized block... allowing the possibility of clobbering in exchange
        // for eliminating the extra call every time
        val value: Any?
        if (keySuppliers.containsKey(key)) {
          value = keySuppliers[key]?.let {supplier->
            supplier(this)
          }
          keySuppliers.remove(key)
        } else {
          value = defaultSupplier(key, this)
        }
        this[key] = value
      }
    }

    return super.get(key)
  }

  companion object {
    @JvmStatic fun newInstance(): LiquidModelMap {
      return LiquidModelMap()
    }

    @JvmStatic fun newInstance(defaultSupplier: FallbackResolver): LiquidModelMap {
      return LiquidModelMap(defaultSupplier)
    }

    @JvmStatic fun newInstance(defaultSupplier: (Any)-> Any?): LiquidModelMap {
      return LiquidModelMap({ key, _ -> defaultSupplier(key) })
    }
  }
}
