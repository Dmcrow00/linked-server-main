package com.linked.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepciones de negocio de la aplicación.
 * <p>
 * {@link GlobalExceptionHandler} las traduce al código HTTP correspondiente con cuerpo
 * {@code ApiResponse}, así que alcanza con lanzarlas desde un servicio para cortar la operación.
 */
public class AppExceptions {

    private AppExceptions() {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends RuntimeException {
        private final String resourceName;
        private final String fieldName;
        private final Object fieldValue;

        public ResourceNotFoundException(String message) {
            super(message);
            this.resourceName = message;
            this.fieldName = null;
            this.fieldValue = null;
        }

        public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
            super(String.format("%s no encontrado con %s: '%s'", resourceName, fieldName, fieldValue));
            this.resourceName = resourceName;
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
        }

        public String getResourceName() { return resourceName; }
        public String getFieldName()    { return fieldName; }
        public Object getFieldValue()   { return fieldValue; }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class InternalException extends RuntimeException {
        public InternalException(String message) { super(message); }
        public InternalException(String message, Throwable cause) { super(message, cause); }
    }
}
