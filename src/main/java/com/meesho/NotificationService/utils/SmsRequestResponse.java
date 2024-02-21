package com.meesho.NotificationService.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SmsRequestResponse {

    @JsonProperty("data")
    private Data data;

    public SmsRequestResponse(String requestId, String comments) {
        this.data = new Data(requestId, comments);
    }

    public void setData(String requestId, String comments) {
        this.data = new Data(requestId, comments);
    }

    public String getComments(){
        return this.data.getComments();
    }

    public String getRequestId(){
        return  this.data.getRequestId();
    }


    @lombok.Data
    public static class Data {
        private String requestId;
        private String comments;

        public Data(String requestId, String comments) {
            this.requestId = requestId;
            this.comments = comments;
        }

    }
}
