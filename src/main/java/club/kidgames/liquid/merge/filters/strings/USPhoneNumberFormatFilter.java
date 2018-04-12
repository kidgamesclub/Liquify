package club.kidgames.liquid.merge.filters.strings;

import javax.swing.text.MaskFormatter;
import liqp.filters.Filter;

/**
 *
 */
public class USPhoneNumberFormatFilter extends Filter {

    public USPhoneNumberFormatFilter() {
        super("usphone");
    }

    @Override
    public Object apply(Object o, Object... objects) {
        String rtn;
        if (o != null) {

            try {
                String phoneMask= "###-###-####";
                String phoneNumber= String.valueOf(o).replace("+", "");
                int extraLength = phoneNumber.length() - 10;
                phoneNumber = phoneNumber.substring(extraLength);

                MaskFormatter maskFormatter= new MaskFormatter(phoneMask);
                maskFormatter.setValueContainsLiteralCharacters(false);
                rtn = maskFormatter.valueToString(phoneNumber) ;
            } catch (Exception e) {
                rtn = o.toString();
            }
        } else {
            rtn = null;
        }
        return rtn;
    }
}
