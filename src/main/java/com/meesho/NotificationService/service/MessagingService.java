package com.meesho.NotificationService.service;

import com.meesho.NotificationService.NotificationServiceApplication;
import com.meesho.NotificationService.exceptions.SmsRequestException;
import com.meesho.NotificationService.models.SmsRequestEntity;
import com.meesho.NotificationService.repository.MessageRepository;
import com.meesho.NotificationService.utils.IMIHttpsRequestBody;
import com.meesho.NotificationService.utils.SmsRequest;
import com.meesho.NotificationService.utils.SmsStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class MessagingService {

    @Value("${imiconnect.api.key}")
    String apiKey;

    @Value("${imiconnect.url}")
    String apiUrl;

    @Autowired
    private MessageRepository messageRepository;

    public static final Logger log = LogManager.getLogger(NotificationServiceApplication.class);


    public ResponseEntity<String> makePostRequest(IMIHttpsRequestBody requestObject) {

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("key", apiKey);

        // Create an HttpEntity with headers and request body
        HttpEntity<IMIHttpsRequestBody> requestEntity = new HttpEntity<>(requestObject, headers);

        // Create a RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Make the POST request
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

        return  responseEntity;

    }

    // validate SMS request
    public Boolean validateSmsRequest(SmsRequest smsRequest) {

        String phoneNumber = smsRequest.getPhoneNumber();
        boolean isValid = phoneNumber.matches("(^$|\\+91[0-9]{10})");

        if (!isValid) {
            log.error("Invalid Phone Number: " + phoneNumber);
            this.updateRequestStatus(smsRequest.getRequestId(), SmsStatus.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(), "phoneNumber is missing");
            throw new SmsRequestException("Invalid Phone Number: " + phoneNumber, SmsStatus.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        String message = smsRequest.getMessage();
        if (message == null || message.trim().isEmpty()) {
            log.error("Invalid Request: Message to missing");
            this.updateRequestStatus(smsRequest.getRequestId(), SmsStatus.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(), "message is missing");
            throw new SmsRequestException("message is mandatory", SmsStatus.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        if (phoneNumber.trim().isEmpty()) {
            log.error("Invalid Request: phoneNumber to missing");
            this.updateRequestStatus(smsRequest.getRequestId(), SmsStatus.INVALID_REQUEST, HttpStatus.BAD_REQUEST.value(), "phoneNumber is missing");
            throw new SmsRequestException("phoneNumber is mandatory", SmsStatus.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        return  Boolean.TRUE;
    }

    // update request status in db
    public void updateRequestStatus(String theId, SmsStatus smsStatus, Integer failureCode, String failureComment) {

        Optional<SmsRequestEntity> smsRequest = messageRepository.findById(theId);

        SmsRequestEntity curSmsRequest;
        if (smsRequest.isPresent()) {
            curSmsRequest = smsRequest.get();
            curSmsRequest.setStatus(smsStatus);
            curSmsRequest.setFailureCode(failureCode);
            curSmsRequest.setFailureComments(failureComment);
            curSmsRequest.updateUpdateTime();

            messageRepository.save(curSmsRequest);
            log.info("SMS Request updated in Database: " + smsRequest);
        }
    }
}
