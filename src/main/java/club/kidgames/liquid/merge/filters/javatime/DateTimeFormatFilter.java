package club.kidgames.liquid.merge.filters.javatime;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import liqp.filters.Filter;

abstract class DateTimeFormatFilter extends Filter {

    private final FormatStyle style;
    private final Locale locale;

    public DateTimeFormatFilter(String name, FormatStyle style, Locale locale) {
        super(name);
        Preconditions.checkNotNull(style, "style must not be null");
        Preconditions.checkNotNull(locale, "locale must not be null");

        this.style = style;
        this.locale = locale;
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof LocalDate) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(style).withLocale(locale);

            ret = formatter.format((LocalDate) value);

        } else if (value instanceof LocalTime) {
            final FormatStyle styleToUse = style == FormatStyle.SHORT ? style : FormatStyle.MEDIUM;
            final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(styleToUse).withLocale(locale);

            ret = formatter.format((LocalTime) value);

        } else if (value instanceof TemporalAccessor) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(style).withLocale(locale);

            ret = formatter.format((TemporalAccessor) value);
        } else if (value instanceof Date) {
            //I don't think this QUITE works... maybe need some help on it.
            ZoneId zone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(((Date) value).getTimezoneOffset() / 60));
            OffsetDateTime offsetDate = OffsetDateTime.ofInstant(((Date) value).toInstant(), zone);
            final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(style).withLocale(locale);
            ret = formatter.format(offsetDate);
        }

        return ret;
    }

}
