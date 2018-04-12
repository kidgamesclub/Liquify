package club.kidgames.liquid.merge.filters.colors;

import java.awt.Color;
import liqp.filters.Filter;

public class DarkenFilter extends Filter {

    public DarkenFilter() {
        super("darken");
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof String && params.length == 1) {
            final double darkenAmount = (double) params[0];

            final Color decoded = Color.decode((String) value);
            final Color darker = darker(decoded, darkenAmount);

            ret = "#" + Integer.toHexString(darker.getRGB()).substring(2).toUpperCase();
        }

        return ret;
    }

    private Color darker (Color color, double fraction) {
        int red   = (int) Math.round (color.getRed()   * (1.0 - fraction));
        int green = (int) Math.round (color.getGreen() * (1.0 - fraction));
        int blue  = (int) Math.round (color.getBlue()  * (1.0 - fraction));

        if (red   < 0) red   = 0; else if (red   > 255) red   = 255;
        if (green < 0) green = 0; else if (green > 255) green = 255;
        if (blue  < 0) blue  = 0; else if (blue  > 255) blue  = 255;

        int alpha = color.getAlpha();

        return new Color (red, green, blue, alpha);
    }

}
