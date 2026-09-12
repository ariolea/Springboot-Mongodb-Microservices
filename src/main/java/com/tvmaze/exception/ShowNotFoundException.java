package com.tvmaze.exception;

/** TVmaze respondio 404: el show solicitado no existe en su catalogo. */
public class ShowNotFoundException extends RuntimeException {

    private final long showId;

    public ShowNotFoundException(long showId) {
        super("No existe un show con id %d en TVmaze.".formatted(showId));
        this.showId = showId;
    }

    public long getShowId() {
        return showId;
    }
}
