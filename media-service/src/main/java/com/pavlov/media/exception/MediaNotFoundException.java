package com.pavlov.media.exception;

public class MediaNotFoundException extends MediaException {
    public MediaNotFoundException(Long id) {
        super("Media file not found. id=" + id);
    }
}