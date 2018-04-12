package club.kidgames.liquid.merge.filters.javatime;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Created by ericm on 8/21/16.
 */
public class DateUtils {

    public static OffsetDateTime toZonedDateTime(Date value) {
        if (value == null) {
            return null;
        } else {
            //I don't think this QUITE works... maybe need some help on it.
            ZoneId zone = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(value.getTimezoneOffset() / 60));
            return OffsetDateTime.ofInstant(value.toInstant(), zone);
        }
    }
}
