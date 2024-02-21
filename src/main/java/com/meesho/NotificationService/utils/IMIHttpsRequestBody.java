package com.meesho.NotificationService.utils;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class IMIHttpsRequestBody {

    private String deliveryChannel = "sms";
    private Channels channels;
    private List<Destination> destination;


    public IMIHttpsRequestBody(String text, String phoneNumber){
        this.channels = new Channels(text);
        this.destination = new ArrayList<>();
        this.destination.add(new Destination(phoneNumber, UUID.randomUUID().toString()));
    }

    // Getters and setters


    @Override
    public String toString() {
        return "IMIHttpsRequestBody{" +
                "deliveryChannel='" + deliveryChannel + '\'' +
                ", channels=" + channels +
                ", destination=" + destination +
                '}';
    }

    // Nested classes for better organization

    public static class Channels {
        private SmsChannel sms;

        public Channels(String text){
            this.sms = new SmsChannel(text);
        }

        // Getters and setters

        public SmsChannel getSms() {
            return sms;
        }

        public void setSms(SmsChannel sms) {
            this.sms = sms;
        }

        @Override
        public String toString() {
            return "Channels{" +
                    "sms=" + sms +
                    '}';
        }
    }

    @Data
    public static class SmsChannel {
        private String text;

        SmsChannel(String text){
            this.text = text;
        }

        // Getters and setters

        @Override
        public String toString() {
            return "SmsChannel{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    @Data
    public static class Destination {
        private List<String> msisdn;
        private String correlationId;

        public  Destination(String phoneNumber, String correlationId){
            this.msisdn = new ArrayList<>();
            this.msisdn.add(phoneNumber);

            this.correlationId = correlationId;
        }

        @Override
        public String toString() {
            return "Destination{" +
                    "msisdn=" + msisdn +
                    ", correlationId='" + correlationId + '\'' +
                    '}';
        }
    }
}
