package club.kidgames.liquid.merge.filters.javatime;

import java.time.format.FormatStyle;
import java.util.Locale;

public class MediumDateTimeFormatFilter extends DateTimeFormatFilter {

    public MediumDateTimeFormatFilter(Locale locale) {
        super("medium", FormatStyle.MEDIUM, locale);
    }

}
