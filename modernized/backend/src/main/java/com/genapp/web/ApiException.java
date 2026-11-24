package com.genapp.web;

import org.springframework.http.HttpStatus;

/**
 * Mirrors the legacy COMMAREA return codes:
 *   01 not found, 70 customer does not exist, 90 Db2/SQL error,
 *   98 bad commarea length (bad request), 99 unknown/unsupported request.
 */
public class ApiException extends RuntimeException {

    private final String returnCode;
    private final HttpStatus status;

    public ApiException(String returnCode, HttpStatus status, String message) {
        super(message);
        this.returnCode = returnCode;
        this.status = status;
    }

    public String getReturnCode() { return returnCode; }
    public HttpStatus getStatus() { return status; }

    public static ApiException notFound(String message) {
        return new ApiException("01", HttpStatus.NOT_FOUND, message);
    }

    public static ApiException customerMissing(String message) {
        return new ApiException("70", HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public static ApiException unsupported(String message) {
        return new ApiException("99", HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException dbError(String message) {
        return new ApiException("90", HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
