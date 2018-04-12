package club.kidgames.liquid.merge.filters.javatime;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import liqp.filters.Filter;

public class PlusWeeksFilter extends Filter {

    public PlusWeeksFilter() {
        super("plus_weeks");
    }

    public Object apply(Object value, Object... params) {
        final long num = params.length == 1 ? (long) params[0] : 0;

        Object ret;

        if (value instanceof ZonedDateTime) {
            ret = ((ZonedDateTime) value).plusWeeks(num);

        } else if (value instanceof LocalDate) {
            ret = ((LocalDate) value).plusWeeks(num);

        } else {
            ret = value;
        }

        return ret;
    }

}
