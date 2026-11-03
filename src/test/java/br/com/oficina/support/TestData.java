package br.com.oficina.support;

import java.util.concurrent.ThreadLocalRandom;

public final class TestData {

    private TestData() {
    }

    public static String cpf() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            digits.append(random.nextInt(10));
        }
        for (int length = 9; length <= 10; length++) {
            int sum = 0;
            for (int i = 0; i < length; i++) {
                sum += Character.getNumericValue(digits.charAt(i)) * (length + 1 - i);
            }
            int remainder = (sum * 10) % 11;
            digits.append(remainder == 10 ? 0 : remainder);
        }
        String cpf = digits.toString();
        return cpf.chars().distinct().count() == 1 ? cpf() : cpf;
    }

    public static String licensePlate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "" + letter(random) + letter(random) + letter(random)
                + random.nextInt(10) + letter(random) + random.nextInt(10) + random.nextInt(10);
    }

    public static String code(String prefix) {
        return prefix + "-" + ThreadLocalRandom.current().nextInt(100_000, 999_999);
    }

    private static char letter(ThreadLocalRandom random) {
        return (char) ('A' + random.nextInt(26));
    }
}
