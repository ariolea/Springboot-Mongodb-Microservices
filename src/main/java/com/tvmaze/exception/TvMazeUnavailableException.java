package com.tvmaze.exception;

/**
 * TVmaze no pudo atender la peticion (timeout, error de red o respuesta 5xx).
 * Se traduce a HTTP 502 para distinguir la falla del proveedor de una falla propia.
 */
public class TvMazeUnavailableException extends RuntimeException {

    public TvMazeUnavailableException(String message) {
        super(message);
    }

    public TvMazeUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
