package com.meesho.NotificationService.service;

import com.meesho.NotificationService.NotificationServiceApplication;
import com.meesho.NotificationService.models.BlacklistEntity;
import com.meesho.NotificationService.utils.SmsRequest;
import com.meesho.NotificationService.exceptions.SmsRequestException;
import com.meesho.NotificationService.utils.SmsStatus;
import com.meesho.NotificationService.models.SmsRequestEntity;
import com.meesho.NotificationService.KafkaService.KafkaProducerService;
import com.meesho.NotificationService.repository.MessageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    public static final Logger log = LogManager.getLogger(NotificationServiceApplication.class);

    public Boolean tempo(){
        return true;
    }

    // save the request to database - MongoDB
    public void saveSmsRequest(SmsRequestEntity smsRequestEntity) {

        try {
            messageRepository.save(smsRequestEntity);
            log.info("SMS Request saved to database: " + smsRequestEntity);
        } catch (Exception e) {
            log.error("An Error occurred while saving request to database: " + e);
            messagingService.updateRequestStatus(smsRequestEntity.getRequestId(), SmsStatus.FAILED, HttpStatus.INTERNAL_SERVER_ERROR.value(), "An Error occurred while saving request to database");
            throw new SmsRequestException("Your request cannot be processed right now. Try after some time.", SmsStatus.FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // send requestId to kafka
    public void sendRequestToKafka(String requestId) {

        try {
            kafkaProducerService.sendMessage(requestId);
            log.info("Message sent to Kafka: " + requestId);
        } catch (Exception e) {
            log.error("Failed to send message to Kafka: " + e.getMessage());
            messagingService.updateRequestStatus(requestId, SmsStatus.FAILED, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to send message to Kafka");
            throw new SmsRequestException("Your request cannot be processed right now. Try after some time.", SmsStatus.FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // get using id
    public SmsRequestEntity getSmsRequestById(String requestId){
        Optional<SmsRequestEntity> smsRequestEntity = messageRepository.findById(requestId);
        log.info(requestId);

        if(smsRequestEntity.isEmpty()){
            log.error("No Request present with Id: "+requestId);
            throw new SmsRequestException("Invalid request ID: "+requestId, SmsStatus.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        return smsRequestEntity.get();
    }

}
