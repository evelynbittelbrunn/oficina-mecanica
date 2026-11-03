package br.com.oficina.shared.domain.valueobject;

import java.util.regex.Pattern;

final class Digits {

    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    private Digits() {
    }

    static String extract(String value) {
        return NON_DIGIT.matcher(value).replaceAll("");
    }

    static boolean allEqual(String digits) {
        return digits.chars().distinct().count() == 1;
    }

    static int at(String digits, int position) {
        return Character.getNumericValue(digits.charAt(position));
    }
}
