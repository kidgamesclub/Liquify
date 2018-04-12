package club.kidgames.liquid.merge.filters.javatime;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import liqp.filters.Filter;

public class MinusYearsFilter extends Filter {

    public MinusYearsFilter() {
        super("minus_years");
    }

    public Object apply(Object value, Object... params) {
        final long num = params.length == 1 ? (long) params[0] : 0;

        Object ret;

        if (value instanceof ZonedDateTime) {
            ret = ((ZonedDateTime) value).minusYears(num);

        } else if (value instanceof LocalDate) {
            ret = ((LocalDate) value).minusYears(num);

        } else {
            ret = value;
        }

        return ret;
    }

}
