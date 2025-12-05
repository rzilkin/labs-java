package mathproj.api;

public record SaveMetricRequest(String operation, Integer recordsProcessed, Long elapsedMs) {}
