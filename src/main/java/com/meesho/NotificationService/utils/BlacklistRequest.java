package com.meesho.NotificationService.utils;

import java.util.List;

public class BlacklistRequest {
    List<String> phoneNumbers;

    public BlacklistRequest(){

    }
    public BlacklistRequest(List<String> phoneNumbers){
        this.phoneNumbers = phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }


    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }



    @Override
    public String toString() {
        return "BlacklistRequest{";
    }
}
