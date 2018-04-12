package club.kidgames.liquid.merge.filters.strings;

import liqp.filters.Filter;

public class ToIntegerFilter extends Filter {

    public ToIntegerFilter() {
        super("to_integer");
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof String) {
            ret = Integer.parseInt((String) value);
        }

        return ret;
    }

}
