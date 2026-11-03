package br.com.oficina.servicing.api.dto;

import br.com.oficina.shared.domain.PageResult;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <D, T> PageResponse<T> from(PageResult<D> page, Function<D, T> mapper) {
        return new PageResponse<>(
                page.content().stream().map(mapper).toList(),
                page.number(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}
