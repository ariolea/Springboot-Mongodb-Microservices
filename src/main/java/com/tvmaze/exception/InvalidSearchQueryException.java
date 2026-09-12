package com.tvmaze.exception;

/** El criterio de busqueda llego vacio o ausente. */
public class InvalidSearchQueryException extends RuntimeException {

    public InvalidSearchQueryException(String message) {
        super(message);
    }
}
