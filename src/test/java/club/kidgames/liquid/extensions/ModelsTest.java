package club.kidgames.liquid.extensions;

import static org.assertj.core.api.Assertions.assertThat;

import club.kidgames.liquid.api.models.LiquidModelMap;
import org.junit.Test;

public class ModelsTest {
  @Test
  public void testExtensionMethods() {
    final LiquidModelMap modelMap = new LiquidModelMap((key, map)-> "The bees knees is key " + key);
    assertThat(modelMap.get("free")).isEqualTo("The bees knees is key free");

  }
}
