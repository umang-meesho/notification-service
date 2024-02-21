package com.meesho.NotificationService.exceptions;

import com.meesho.NotificationService.NotificationServiceApplication;
import com.meesho.NotificationService.utils.SmsStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SmsRequestExceptionHandler {

    public static final Logger log = LogManager.getLogger(NotificationServiceApplication.class);

    @ExceptionHandler
    public ResponseEntity<SmsRequestErrorResponse> handleException(SmsRequestException err){
        SmsRequestErrorResponse error = new SmsRequestErrorResponse(err.getSmsStatus(), err.getMessage());
        // return the Response
        return new ResponseEntity<>(error, err.getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<SmsRequestErrorResponse> handleExceptions(Exception e){
        SmsRequestErrorResponse error = new SmsRequestErrorResponse(SmsStatus.FAILED, "An Internal Server Error Occurred");
        log.error("Internal Error Occurred: "+e.getMessage());

        // return the Response
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
