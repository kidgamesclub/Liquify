package club.kidgames.liquid.merge.filters.javatime;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import liqp.filters.Filter;

public class PlusYearsFilter extends Filter {

    public PlusYearsFilter() {
        super("plus_years");
    }

    public Object apply(Object value, Object... params) {
        final long num = params.length == 1 ? (long) params[0] : 0;

        Object ret;

        if (value instanceof ZonedDateTime) {
            ret = ((ZonedDateTime) value).plusYears(num);

        } else if (value instanceof LocalDate) {
            ret = ((LocalDate) value).plusYears(num);

        } else {
            ret = value;
        }

        return ret;
    }

}
