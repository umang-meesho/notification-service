package com.meesho.NotificationService.controller;

import com.meesho.NotificationService.NotificationServiceApplication;
import com.meesho.NotificationService.exceptions.SmsRequestException;
import com.meesho.NotificationService.models.BlacklistEntity;
import com.meesho.NotificationService.models.SmsRequestEntity;
import com.meesho.NotificationService.service.ElasticSearchService;
import com.meesho.NotificationService.service.MessagingService;
import com.meesho.NotificationService.service.NotificationService;
import com.meesho.NotificationService.service.RedisService;
import com.meesho.NotificationService.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class NotificationRestController {

    private NotificationService notificationService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private MessagingService messagingService;

    public static final Logger log = LogManager.getLogger(NotificationServiceApplication.class);

    // inject EmployeeDAO
    public NotificationRestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/sms/send")
    public SmsRequestResponse smsPostRequest(@RequestBody SmsRequest smsRequest) {

        log.info("SmsRequest Received: " + smsRequest);

        SmsRequestEntity smsRequestEntity = new SmsRequestEntity(smsRequest.getRequestId(), smsRequest.getPhoneNumber(), smsRequest.getMessage());

        // check if the number is blacklisted or not
        BlacklistEntity blacklistEntity = redisService.getUserById(smsRequestEntity.getPhoneNumber());

        if (blacklistEntity != null) {
            log.error("Phone number is blacklisted: " + smsRequestEntity.getPhoneNumber());
            messagingService.updateRequestStatus(smsRequest.getRequestId(), SmsStatus.FAILED, HttpStatus.BAD_REQUEST.value(), "Phone number is blacklisted");
            throw new SmsRequestException("Phone number is blacklisted " + smsRequest.getPhoneNumber(), SmsStatus.FAILED, HttpStatus.BAD_REQUEST);
        }

        System.out.println("the number is not blacklisted");

        // save the request to mongodb
        notificationService.saveSmsRequest(smsRequestEntity);

        // validate the sms request
        messagingService.validateSmsRequest(smsRequest);
        log.info("Sms Request is Validated.");

        // produce the requestId to kafka
        notificationService.sendRequestToKafka(smsRequest.getRequestId());
        log.info("Request Id is produced to Kafka: " + smsRequest.getRequestId());


        return new SmsRequestResponse(smsRequest.getRequestId(), "Successfully Sent");

    }

    @PostMapping("/blacklist")
    BlackListRequestResponse blacklistPhoneNumberRequest(@RequestBody BlacklistRequest blacklistRequest) {
        List<String> phoneNumbers = blacklistRequest.getPhoneNumbers();

        // validate all the phone numbers
        for (String numbers : phoneNumbers) {
            validatePhoneNumber(numbers);
        }

        // blacklist all the give phone number
        for (String number : phoneNumbers) {
            redisService.addPhoneNumber(number);
            log.info(number + " is blacklisted");
        }

        // return response
        return new BlackListRequestResponse("Successfully blacklisted");
    }

    @DeleteMapping("/blacklist")
    BlackListRequestResponse deleteBlacklistPhoneNumbers(@RequestBody BlacklistRequest blacklistRequest) {
        List<String> phoneNumbers = blacklistRequest.getPhoneNumbers();

        // validate all the phone numbers
        for (String numbers : phoneNumbers) {
            validatePhoneNumber(numbers);
        }

        // whitelist all the give phone number
        for (String number : phoneNumbers) {
            redisService.deleteUserById(number);
            log.info(number + " is whitelisted");
        }

        // return response
        return new BlackListRequestResponse("Successfully whitelisted");
    }

    @GetMapping("/blacklist")
    Map<String, List<String>> getAllBlackListedNumbers() {
        List<BlacklistEntity> blackListEntities = redisService.getAllBlackListedNumbers();

        // Extract phone numbers from BlacklistEntity objects
        List<String> phoneNumbers = blackListEntities.stream()
                .map(BlacklistEntity::getPhoneNumber)
                .toList();

        return Map.of("data", phoneNumbers);
    }

    @GetMapping("/sms/{requestId}")
    public SmsRequestEntity getRequestById(@PathVariable String requestId) {
        return notificationService.getSmsRequestById(requestId);
    }

    @GetMapping("/search")
    public ElasticSearchResponse searchSms(
            @RequestParam String phoneNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        System.out.println(phoneNumber + " " + startTime + " " + endTime + " " + page + " " + size);

        return elasticSearchService.getSms(phoneNumber, startTime, endTime, page, size);
    }

    @GetMapping("/searchontext")
    public ElasticSearchResponse searchSmsText(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return elasticSearchService.searchSmsText(text, page, size);
    }

    public static void validatePhoneNumber(String phoneNumber) {
        boolean isValid = phoneNumber.matches("(^$|\\+91[0-9]{10})");

        if (!isValid) {
            log.error("Invalid Phone Number: " + phoneNumber);
            throw new SmsRequestException("Invalid Phone Number: " + phoneNumber, SmsStatus.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

}
