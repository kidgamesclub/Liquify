package club.kidgames.liquid.api

import club.kidgames.liquid.liqp.EntrySupplier
import club.kidgames.liquid.liqp.FallbackResolver
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Supplier

/**
 * Used as the rendering model for Liquify templates.  This model supports:
 *
 * * Key-based suppliers, for lazy/deferred resolution of data.
 * * Fallback supplier/s, for resolving unknown or dynamic keys (the values are cached after being resolved the first time)
 *
 * The idea is to defer all calculations as late as possible, in the case that map entries are expensive to retrieve
 * or calculate, and you don't know which ones will be needed in advance.
 */
class LiquidModelMap(private val defaultSuppliers: List<FallbackResolver>) : HashMap<String, Any?>() {
  constructor(vararg defaultSuppliers: FallbackResolver) : this(listOf(*defaultSuppliers))

  private val keySuppliers = HashMap<String, EntrySupplier>()
  var player: Player? by this
  var world: World? by this
  var server: Server? by this

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
          value = keySuppliers[key]?.let { supplier ->
            supplier(this)
          }
          keySuppliers.remove(key)
        } else {
          value = defaultSuppliers
              .mapNotNull { fallback ->
                fallback.invoke(key, this)
              }
              .firstOrNull()
        }
        this[key] = value
      }
    }

    return super.get(key)
  }

  companion object {
    @JvmStatic
    fun newInstance(): LiquidModelMap {
      return LiquidModelMap()
    }

    @JvmStatic
    fun newInstance(defaultSupplier: FallbackResolver): LiquidModelMap {
      return LiquidModelMap(defaultSupplier)
    }

    @JvmStatic
    fun newInstance(defaultSupplier: (Any) -> Any?): LiquidModelMap {
      return LiquidModelMap({ key, _ -> defaultSupplier(key) })
    }
  }
}
