package club.kidgames.liquid.merge.filters.javatime;

import com.google.common.base.Preconditions;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import liqp.filters.Filter;

public class CustomDateTimeFormatFilter extends Filter {

    private final Locale locale;

    public CustomDateTimeFormatFilter(Locale locale) {
        super("custom");
        Preconditions.checkNotNull(locale, "locale must not be null");

        this.locale = locale;
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof TemporalAccessor && params.length == 1) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern((String) params[0], this.locale);

            ret = formatter.format((TemporalAccessor) value);
        }

        return ret;
    }

}
