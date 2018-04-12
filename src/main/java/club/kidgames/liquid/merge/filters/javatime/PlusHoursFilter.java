package club.kidgames.liquid.merge.filters.javatime;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import liqp.filters.Filter;

public class PlusHoursFilter extends Filter {

    public PlusHoursFilter() {
        super("plus_hours");
    }

    public Object apply(Object value, Object... params) {
        final long num = params.length == 1 ? (long) params[0] : 0;

        Object ret;

        if (value instanceof ZonedDateTime) {
            ret = ((ZonedDateTime) value).plusHours(num);

        } else if (value instanceof LocalTime) {
            ret = ((LocalTime) value).plusHours(num);

        } else {
            ret = value;
        }

        return ret;
    }

}
