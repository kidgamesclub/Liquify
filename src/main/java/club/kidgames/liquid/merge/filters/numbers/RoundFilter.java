package club.kidgames.liquid.merge.filters.numbers;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import liqp.filters.Filter;

public class RoundFilter extends Filter {

    private final Locale locale;

    public RoundFilter(Locale locale) {
        super("round");
        Preconditions.checkNotNull(locale, "locale must not be null");

        this.locale = locale;
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof Number && params.length == 1) {
            final int places = ((Long) params[0]).intValue();

            if (value instanceof Integer) {
                ret = String.format(locale, "%." + places + "f", ((Integer) value).doubleValue());

            } else {
                final double rounded = new BigDecimal(((Number) value).doubleValue()).setScale(places, RoundingMode.HALF_UP).doubleValue();

                ret = String.format(locale, "%." + places + "f", rounded);
            }
        }

        return ret;
    }

}
