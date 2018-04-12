package club.kidgames.liquid.merge.filters.colors;

import java.awt.Color;
import liqp.filters.Filter;

public class ToRgbFilter extends Filter {

    public ToRgbFilter() {
        super("to_rgb");
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof String) {
            final Color decoded = Color.decode((String) value);

            ret = String.format("%s,%s,%s", decoded.getRed(), decoded.getGreen(), decoded.getBlue());
        }

        return ret;
    }

}
