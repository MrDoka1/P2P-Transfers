package ru.krizhanovskiy.p2ptransfers.exceptions;

import java.util.List;

public record ValidationErrorResponse(String message, List<String> errors) {}