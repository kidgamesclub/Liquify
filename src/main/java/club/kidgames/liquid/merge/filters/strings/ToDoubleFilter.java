package club.kidgames.liquid.merge.filters.strings;

import liqp.filters.Filter;

public class ToDoubleFilter extends Filter {

    public ToDoubleFilter() {
        super("to_double");
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof String) {
            ret = Double.parseDouble((String) value);
        }

        return ret;
    }

}
