package br.com.oficina.shared.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void shouldCalculateTotalPages() {
        assertThat(new PageResult<>(List.of(1, 2), 0, 2, 5).totalPages()).isEqualTo(3);
        assertThat(new PageResult<>(List.of(), 0, 10, 0).totalPages()).isZero();
        assertThat(new PageResult<>(List.of(), 0, 0, 0).totalPages()).isZero();
    }

    @Test
    void shouldMapContentKeepingMetadata() {
        PageResult<String> page = new PageResult<>(List.of(1, 2), 1, 2, 4).map(String::valueOf);

        assertThat(page.content()).containsExactly("1", "2");
        assertThat(page.number()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(4);
    }

    @Test
    void aggregateShouldPublishEventsOnlyOnce() {
        AggregateRoot aggregate = new AggregateRoot() {
            {
                registerEvent(() -> Instant.EPOCH);
            }
        };

        assertThat(aggregate.pullEvents()).hasSize(1);
        assertThat(aggregate.pullEvents()).isEmpty();
    }
}
