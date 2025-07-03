package ru.krizhanovskiy.p2ptransfers.expressions;

import java.util.List;

public record ValidationErrorResponse(String message, List<String> errors) {}