package club.kidgames.liquid.merge.filters.javatime;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import liqp.filters.Filter;

public class MinusMonthsFilter extends Filter {

    public MinusMonthsFilter() {
        super("minus_months");
    }

    public Object apply(Object value, Object... params) {
        final long num = params.length == 1 ? (long) params[0] : 0;

        Object ret;

        if (value instanceof ZonedDateTime) {
            ret = ((ZonedDateTime) value).minusMonths(num);

        } else if (value instanceof LocalDate) {
            ret = ((LocalDate) value).minusMonths(num);

        } else {
            ret = value;
        }

        return ret;
    }

}
