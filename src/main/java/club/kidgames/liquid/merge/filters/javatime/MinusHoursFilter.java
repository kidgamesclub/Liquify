package club.kidgames.liquid.merge.filters.javatime;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import liqp.filters.Filter;

public class MinusHoursFilter extends Filter {

    public MinusHoursFilter() {
        super("minus_hours");
    }

    public Object apply(Object value, Object... params) {
        final long num = params.length == 1 ? (long) params[0] : 0;

        Object ret;

        if (value instanceof ZonedDateTime) {
            ret = ((ZonedDateTime) value).minusHours(num);

        } else if (value instanceof LocalTime) {
            ret = ((LocalTime) value).minusHours(num);

        } else {
            ret = value;
        }

        return ret;
    }

}
