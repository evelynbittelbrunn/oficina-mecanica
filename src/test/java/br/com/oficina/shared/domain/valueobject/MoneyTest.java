package br.com.oficina.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithTwoDecimalPlaces() {
        Money money = new Money(new BigDecimal("10"));

        assertThat(money.amount()).isEqualByComparingTo("10.00");
    }

    @Test
    void shouldAddMoneyValues() {
        Money first = new Money(new BigDecimal("100.00"));
        Money second = new Money(new BigDecimal("50.00"));

        Money result = first.add(second);

        assertThat(result.amount()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldSubtractMoneyValues() {
        Money first = new Money(new BigDecimal("100.00"));
        Money second = new Money(new BigDecimal("30.00"));

        Money result = first.subtract(second);

        assertThat(result.amount()).isEqualByComparingTo("70.00");
    }

    @Test
    void shouldMultiplyMoneyByQuantity() {
        Money money = new Money(new BigDecimal("25.50"));

        Money result = money.multiply(3);

        assertThat(result.amount()).isEqualByComparingTo("76.50");
    }

    @Test
    void shouldIdentifyZeroValue() {
        Money money = new Money(BigDecimal.ZERO);

        assertThat(money.isZero()).isTrue();
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(null));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Money(new BigDecimal("-10.00")));
    }

    @Test
    void shouldRejectSubtractionResultingInNegativeAmount() {
        Money first = new Money(new BigDecimal("10.00"));
        Money second = new Money(new BigDecimal("20.00"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> first.subtract(second));
    }

    @Test
    void shouldRejectNegativeQuantity() {
        Money money = new Money(new BigDecimal("10.00"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> money.multiply(-1));
    }
}
