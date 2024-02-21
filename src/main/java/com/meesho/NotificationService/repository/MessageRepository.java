package com.meesho.NotificationService.repository;

import com.meesho.NotificationService.models.SmsRequestEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<SmsRequestEntity, String> {

}
