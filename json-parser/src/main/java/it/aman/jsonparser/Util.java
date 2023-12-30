package it.aman.jsonparser;

public class Util {

    public static boolean isAlphaNumeric(char c) {
        return Character.isDigit(c) || Character.isAlphabetic(c);
    }

    public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private Util() {
        //
    }
}
