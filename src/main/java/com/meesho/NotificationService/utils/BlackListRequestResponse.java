package com.meesho.NotificationService.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlackListRequestResponse {

    @JsonProperty("data")
    private String data;

    public BlackListRequestResponse(String comments) {
        this.data = comments;
    }
}
