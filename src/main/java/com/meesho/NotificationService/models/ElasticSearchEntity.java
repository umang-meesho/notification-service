package com.meesho.NotificationService.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@NoArgsConstructor
@Data
@Document(indexName = "sms_requests")
public class ElasticSearchEntity {

    @Id
    private String requestId;
    private String phoneNumber;
    private String message;
    private long updatedAt;

    public ElasticSearchEntity(String requestId, String phoneNumber, String message, long updatedAt){
        this.requestId = requestId;
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.updatedAt = updatedAt;
    }
}
