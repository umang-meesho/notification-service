package com.meesho.NotificationService.utils;

import java.util.UUID;

public class SmsRequest {

    private String requestId;
    public String phoneNumber;
    public String message;


    public SmsRequest(String phoneNumber, String message){
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.requestId = UUID.randomUUID().toString().toUpperCase().replace("-", "");
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "SmsRequest{" +
                "requestId='" + requestId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
