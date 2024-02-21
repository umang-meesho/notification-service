package com.meesho.NotificationService.exceptions;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.meesho.NotificationService.utils.SmsStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmsRequestErrorResponse {

    @JsonProperty("error")
    private Error error;

    public SmsRequestErrorResponse(SmsStatus code, String message) {
        this.error = new Error(code, message);
    }

    public Error getError() {
        return error;
    }

    public void setError(SmsStatus code, String message) {
        this.error = new Error(code, message);;
    }

    public class Error{
        private SmsStatus code;
        private String message;

        public Error(SmsStatus code, String message) {
            this.code = code;
            this.message = message;
        }

        public SmsStatus getCode() {
            return code;
        }

        public void setCode(SmsStatus code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
