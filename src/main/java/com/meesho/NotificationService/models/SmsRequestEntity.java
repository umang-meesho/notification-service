package com.meesho.NotificationService.models;

import com.meesho.NotificationService.utils.SmsStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "sms_requests")
public class SmsRequestEntity {

    @Id
    private String requestId;
    private String phoneNumber;
    private String message;
    private SmsStatus status;
    private Integer failureCode;
    private String failureComments;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public SmsRequestEntity(String requestId, String phoneNumber, String message){
        this.requestId = requestId;
        this.phoneNumber = phoneNumber;
        this.message =message;
        this.status = SmsStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void updateUpdateTime(){
        this.updatedAt = LocalDateTime.now();
    }

}
