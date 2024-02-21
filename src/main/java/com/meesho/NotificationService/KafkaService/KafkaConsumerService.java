package com.meesho.NotificationService.KafkaService;

import com.meesho.NotificationService.NotificationServiceApplication;
import com.meesho.NotificationService.exceptions.SmsRequestException;
import com.meesho.NotificationService.models.ElasticSearchEntity;
import com.meesho.NotificationService.models.SmsRequestEntity;
import com.meesho.NotificationService.repository.MessageRepository;
import com.meesho.NotificationService.service.ElasticSearchService;
import com.meesho.NotificationService.service.MessagingService;
import com.meesho.NotificationService.service.NotificationService;
import com.meesho.NotificationService.service.RedisService;
import com.meesho.NotificationService.utils.IMIHttpsRequestBody;
import com.meesho.NotificationService.utils.SmsStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class KafkaConsumerService {

    public static final Logger log = LogManager.getLogger(NotificationServiceApplication.class);

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topic}", groupId = "sms_consumer_group"
//            properties = {"auto.offset.reset=earliest"}
    )
    public void receiveMessage(String requestId) {

        // receive SmsRequest Id at Kafka Consumer
        log.info("Request received at Kafka Consumer: " + requestId);

        // get the Entity from the database
        SmsRequestEntity smsRequestEntity = messageRepository.findById(requestId).get();

//        // check if the number is blacklisted or not
//        BlacklistEntity blacklistEntity = redisService.getUserById(smsRequestEntity.getPhoneNumber());
//        if (blacklistEntity != null) {
//            log.error("Phone number is blacklisted: " + smsRequestEntity.getPhoneNumber());
//            notificationService.updateRequestStatus(requestId, SmsStatus.FAILED, HttpStatus.BAD_REQUEST.value(), "Phone number is blacklisted");
//            throw new SmsRequestException("Phone number is blacklisted", SmsStatus.FAILED, HttpStatus.BAD_REQUEST);
//        }

        // SEND SMS

        // Make the POST request
        ResponseEntity<String> responseEntity = messagingService.makePostRequest(new IMIHttpsRequestBody(smsRequestEntity.getMessage(), smsRequestEntity.getPhoneNumber()));

        // Handle the response
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            log.info("Response from server: " + responseBody);
            messagingService.updateRequestStatus(requestId, SmsStatus.SENT, null, null);
            log.info("Message sent to phoneNumber. Request ID - " + requestId);
        } else {
            log.error("Error from IMI Connect - HTTP Status Code: " + responseEntity.getStatusCode().value());
            messagingService.updateRequestStatus(requestId, SmsStatus.FAILED, responseEntity.getStatusCode().value(), "Error from IMI Connect - HTTP Status Code: " + responseEntity.getStatusCodeValue());
            throw new SmsRequestException("Unable to process the request", SmsStatus.FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // save the request to elastic search
        smsRequestEntity = messageRepository.findById(requestId).get();

        elasticSearchService.saveRequest(new ElasticSearchEntity(
                smsRequestEntity.getRequestId(),
                smsRequestEntity.getPhoneNumber(),
                smsRequestEntity.getMessage(),
                smsRequestEntity.getUpdatedAt().toEpochSecond(ZoneOffset.UTC)));

    }


}
