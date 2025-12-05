package mathproj.api;

public record CreateTabulatedFromFunctionRequest(String name, Long sourceFunctionId, Integer count, double from, double to) {}
