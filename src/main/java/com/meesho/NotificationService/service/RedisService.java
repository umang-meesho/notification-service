package com.meesho.NotificationService.service;

import com.meesho.NotificationService.NotificationServiceApplication;
import com.meesho.NotificationService.exceptions.SmsRequestException;
import com.meesho.NotificationService.models.BlacklistEntity;
import com.meesho.NotificationService.repository.BlacklistRepository;
import com.meesho.NotificationService.utils.SmsStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RedisService {
    private static final String USER_SET_KEY = "blacklist";


    @Autowired
    private BlacklistRepository blacklistRepository;

    public static final Logger log = LogManager.getLogger(NotificationServiceApplication.class);


    @CachePut(value = USER_SET_KEY)
    public BlacklistEntity addPhoneNumber(String phoneNumber) {
        try {
            return blacklistRepository.save(new BlacklistEntity(phoneNumber));
        }catch (Exception e){
            log.error("Failed to push phone number to Database");
            throw new SmsRequestException("Failed to process the request. Try after some time", SmsStatus.FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Cacheable(value = USER_SET_KEY, key = "#phoneNumber")
    public BlacklistEntity getUserById(String phoneNumber){
        Optional<BlacklistEntity> blacklistEntity = blacklistRepository.findById(phoneNumber);
        return blacklistEntity.orElse(null);
    }

    @CacheEvict(value = USER_SET_KEY, key = "#phoneNumber")
    public String deleteUserById(String phoneNumber) {
        blacklistRepository.deleteById(phoneNumber);
        return "Successfully Deleted";
    }

    public List<BlacklistEntity> getAllBlackListedNumbers(){
        return blacklistRepository.findAll();
    }


}
