package com.pavlov.media.exception;

import java.time.Instant;

public record ApiError(
        String message,
        int status,
        Instant timestamp
) {}