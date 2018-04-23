package club.kidgames.liquid.api;

import liqp.MutableParseSettings;
import liqp.MutableRenderSettings;
import org.jetbrains.annotations.NotNull;

public interface RenderSettingsExtender extends LiquifyExtender {
  void configureRenderer(MutableRenderSettings settings);

  @NotNull
  @Override
  default LiquidExtenderType getType() {
    return LiquidExtenderType.RENDER_SETTINGS;
  }

  @NotNull
  @Override
  default String getName() {
    return getPluginId();
  }
}
