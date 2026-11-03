package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MoneyTest {

    @Test
    void shouldNormalizeToTwoDecimalPlaces() {
        assertThat(Money.of("10").amount()).isEqualByComparingTo("10.00");
        assertThat(Money.of("10").amount().scale()).isEqualTo(2);
    }

    @Test
    void shouldRoundHalfUp() {
        assertThat(Money.of("10.005").amount()).isEqualByComparingTo("10.01");
    }

    @Test
    void shouldAdd() {
        assertThat(Money.of("100.00").add(Money.of("50.25"))).isEqualTo(Money.of("150.25"));
    }

    @Test
    void shouldSubtract() {
        assertThat(Money.of("100.00").subtract(Money.of("30.00"))).isEqualTo(Money.of("70.00"));
    }

    @Test
    void shouldMultiplyByQuantity() {
        assertThat(Money.of("25.50").multiply(3)).isEqualTo(Money.of("76.50"));
        assertThat(Money.of("25.50").multiply(0)).isEqualTo(Money.ZERO);
    }

    @Test
    void shouldIdentifyZero() {
        assertThat(Money.ZERO.isZero()).isTrue();
        assertThat(Money.of("0.01").isZero()).isFalse();
    }

    @Test
    void shouldCompare() {
        assertThat(Money.of("10.00")).isGreaterThan(Money.of("9.99"));
        assertThat(Money.of("10")).isEqualByComparingTo(Money.of("10.00"));
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Money(null));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Money(new BigDecimal("-0.01")))
                .withMessage("Valor monetário não pode ser negativo.");
    }

    @Test
    void shouldRejectSubtractionResultingInNegativeAmount() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Money.of("10.00").subtract(Money.of("20.00")));
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Money.of("10.00").multiply(-1))
                .withMessage("Quantidade não pode ser negativa.");
    }
}
