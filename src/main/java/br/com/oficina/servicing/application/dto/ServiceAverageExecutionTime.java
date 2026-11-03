package br.com.oficina.servicing.application.dto;

import java.util.UUID;

public record ServiceAverageExecutionTime(UUID serviceId, String name, long orderCount, Double averageMinutes) {
}
