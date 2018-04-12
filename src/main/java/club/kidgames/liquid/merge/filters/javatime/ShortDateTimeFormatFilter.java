package club.kidgames.liquid.merge.filters.javatime;

import java.time.format.FormatStyle;
import java.util.Locale;

public class ShortDateTimeFormatFilter extends DateTimeFormatFilter {

    public ShortDateTimeFormatFilter(Locale locale) {
        super("short", FormatStyle.SHORT, locale);
    }

}
