package club.kidgames.liquid.merge.filters.collections;

import java.util.Collection;
import liqp.filters.Filter;
import org.apache.commons.lang3.StringUtils;

public class CommaSeparatedFilter extends Filter {

    public CommaSeparatedFilter() {
        super("comma_separated");
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof Collection) {
            ret = StringUtils.join((Collection) value, ", ");
        }

        return ret;
    }

}
