package club.kidgames.liquid.merge.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Simple implementation that allows you to provide a supplier for a key, and the first time that map key is accessed,
 * the supplier will be invoked.

 */
public class SupplierMap<K, V> extends HashMap<K, V> {
  private final Map<K, Supplier<V>> keySuppliers = new HashMap<>();
  private final Function<Object, V> defaultSupplier;

  public static <K, V> SupplierMap<K, V> newInstance() {
    return new SupplierMap<>();
  }

  public static <K, V> SupplierMap<K, V> newInstance(Function<Object, V> defaultSupplier) {
    return new SupplierMap<>(defaultSupplier);
  }

  public SupplierMap(Function<Object, V> defaultSupplier) {
    this.defaultSupplier = defaultSupplier;
  }

  public SupplierMap() {
    this.defaultSupplier = key-> null;
  }

  public SupplierMap<K, V> putSupplier(K key, Supplier<V> supplier) {
    this.keySuppliers.put(key, supplier);
    return this;
  }

  @Override
  public synchronized V get(Object key) {
    K k = (K) key;
    if (!this.containsKey(key)) {
      final V value;
      if (keySuppliers.containsKey(k)) {
        final Supplier<V> supplier = keySuppliers.get(k);
        if (supplier != null) {
          value = supplier.get();
        } else {
          value = null;
        }
        keySuppliers.remove(key);
      } else {
        value = defaultSupplier.apply(key);
      }
      this.put(k, value);
    }

    return super.get(key);
  }
}
