package com.meesho.NotificationService.exceptions;

import com.meesho.NotificationService.utils.SmsStatus;
import org.springframework.http.HttpStatus;

public class SmsRequestException extends RuntimeException {

    private SmsStatus smsStatus;
    private HttpStatus httpStatus;

    public SmsStatus getSmsStatus() {
        return smsStatus;
    }

    public void setSmsStatus(SmsStatus smsStatus) {
        this.smsStatus = smsStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public SmsRequestException(String message, SmsStatus smsStatus, HttpStatus httpStatus) {
        super(message);
        this.smsStatus = smsStatus;
        this.httpStatus = httpStatus;
    }

    public SmsRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmsRequestException(Throwable cause) {
        super(cause);
    }

    public SmsRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
