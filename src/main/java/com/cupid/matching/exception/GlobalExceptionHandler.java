package com.cupid.matching.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Converts persistence failures into safe, user-friendly responses.
 *
 * Supports:
 * NFR_Persistence_Timeout
 * NFR_Traceability
 * Reliability
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(QueryTimeoutException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleQueryTimeout(
            QueryTimeoutException exception,
            Model model
    ) {
        model.addAttribute(
                "errorTitle",
                "Database request timed out"
        );

        model.addAttribute(
                "errorMessage",
                "The operation could not be completed within five seconds. "
                        + "Please try again."
        );

        return "error/database-error";
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleConnectionFailure(
            DataAccessResourceFailureException exception,
            Model model
    ) {
        model.addAttribute(
                "errorTitle",
                "Database temporarily unavailable"
        );

        model.addAttribute(
                "errorMessage",
                "Cupid cannot connect to the database right now. "
                        + "Please try again shortly."
        );

        return "error/database-error";
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOtherDatabaseFailure(
            DataAccessException exception,
            Model model
    ) {
        model.addAttribute(
                "errorTitle",
                "Unable to complete the operation"
        );

        model.addAttribute(
                "errorMessage",
                "A database error occurred. No partial changes were saved."
        );

        return "error/database-error";
    }
}