package br.com.oficina.shared.domain;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, int number, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(content);
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), number, size, totalElements);
    }
}
