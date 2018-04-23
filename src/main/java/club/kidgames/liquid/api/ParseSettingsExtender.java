package club.kidgames.liquid.api;

import liqp.MutableParseSettings;
import org.jetbrains.annotations.NotNull;

public interface ParseSettingsExtender extends LiquifyExtender {
  void configureParser(MutableParseSettings settings);

  @NotNull
  @Override
  default String getName() {
    return getPluginId();
  }

  @NotNull
  @Override
  default LiquidExtenderType getType() {
    return LiquidExtenderType.PARSE_SETTINGS;
  }
}
