package club.kidgames.liquid.merge.filters.collections;

import java.util.Collection;
import java.util.Objects;
import liqp.filters.Filter;
import org.apache.commons.lang3.StringUtils;

public class HtmlBulletsFilter extends Filter {

    public HtmlBulletsFilter() {
        super("html_bullets");
    }

    public Object apply(Object value, Object... params) {
        Object ret = value;

        if (value instanceof Collection) {
            final StringBuilder builder = new StringBuilder();

            builder.append("<ul>");
            ((Collection<Object>) value).stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .filter(StringUtils::isNotBlank)
                    .forEach(item -> builder.append("<li>").append(item).append("</li>"));
            builder.append("</ul>");

            ret = builder.toString();
        }

        return ret;
    }

}
