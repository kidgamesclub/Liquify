package club.kidgames.liquid.merge.filters.colors;

import liqp.filters.Filter;
import org.apache.commons.lang3.StringUtils;

public class MinecraftChatFormat extends Filter {

  public MinecraftChatFormat() {
    super("mformat");
  }

  public Object apply(Object value, Object... params) {
    if (value != null && StringUtils.isNotBlank(value.toString())) {
      StringBuilder output = new StringBuilder();
      for (Object param : params) {

        if (param != null) {
          String pString = String.valueOf(param);
          for (int i = 0; i < pString.length(); i++) {
            output.append('\u00A7').append(pString.charAt(i));
          }
        }
      }
      output.append(value);
      return output.toString();
    } else {
      return value;
    }
  }
}
