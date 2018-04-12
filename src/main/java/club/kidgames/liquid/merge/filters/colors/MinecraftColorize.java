package club.kidgames.liquid.merge.filters.colors;

import java.awt.Color;
import liqp.filters.Filter;
import org.apache.commons.lang3.StringUtils;

public class MinecraftColorize extends Filter {

  public MinecraftColorize() {
    super("mcolor");
  }

  public Object apply(Object value, Object... params) {
    if (value != null && StringUtils.isNotBlank(value.toString())) {
      StringBuilder output = new StringBuilder();
      for (Object param : params) {
        if (param != null) {
          String pString = String.valueOf(param);
          final Color color = Color.decode(pString);
          output.append('\u00A7').append(Integer.toHexString(color.getRGB()).substring(2).toUpperCase()).append(';');
        }
      }
      output.append(value);
      return output.toString();
    } else {
      return value;
    }
  }
}
