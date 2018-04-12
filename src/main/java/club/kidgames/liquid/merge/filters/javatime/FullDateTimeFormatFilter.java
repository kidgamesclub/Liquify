package club.kidgames.liquid.merge.filters.javatime;

import java.time.format.FormatStyle;
import java.util.Locale;

public class FullDateTimeFormatFilter extends DateTimeFormatFilter {

    public FullDateTimeFormatFilter(Locale locale) {
        super("full", FormatStyle.FULL, locale);
    }

}
