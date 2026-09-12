package com.tvmaze.exception;

/** TVmaze respondio 429: se excedio el limite de peticiones permitido. */
public class TvMazeRateLimitException extends RuntimeException {

    public TvMazeRateLimitException(String message) {
        super(message);
    }
}
