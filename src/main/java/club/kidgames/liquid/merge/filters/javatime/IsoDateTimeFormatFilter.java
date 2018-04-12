package club.kidgames.liquid.merge.filters.javatime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import liqp.filters.Filter;

public class IsoDateTimeFormatFilter extends Filter {

    public IsoDateTimeFormatFilter() {
        super("iso");
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof LocalDate) {
            ret = DateTimeFormatter.ISO_DATE.format((LocalDate) value);

        } else if (value instanceof LocalTime) {
            ret = DateTimeFormatter.ISO_TIME.format((LocalTime) value);

        } else if (value instanceof TemporalAccessor) {
            ret = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format((TemporalAccessor) value);
        }

        return ret;
    }

}
