package club.kidgames.liquid.merge.filters;

import liqp.filters.Filter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class DefaultValueFilter extends Filter {

    public DefaultValueFilter() {
        super("default");
    }

    public Object apply(Object value, Object... params) {
        final Object valueToUse = params.length == 1 ? params[0] : StringUtils.EMPTY;

        return ObjectUtils.defaultIfNull(value, valueToUse);
    }

}
