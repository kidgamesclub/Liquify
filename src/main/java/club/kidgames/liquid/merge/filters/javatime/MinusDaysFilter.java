package club.kidgames.liquid.merge.filters.javatime;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import liqp.filters.Filter;

public class MinusDaysFilter extends Filter {

    public MinusDaysFilter() {
        super("minus_days");
    }

    public Object apply(Object value, Object... params) {
        final long num = params.length == 1 ? (long) params[0] : 0;

        Object ret;

        if (value instanceof ZonedDateTime) {
            ret = ((ZonedDateTime) value).minusDays(num);

        } else if (value instanceof LocalDate) {
            ret = ((LocalDate) value).minusDays(num);

        } else {
            ret = value;
        }

        return ret;
    }

}
