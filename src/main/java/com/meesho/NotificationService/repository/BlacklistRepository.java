package com.meesho.NotificationService.repository;

import com.meesho.NotificationService.models.BlacklistEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BlacklistRepository extends MongoRepository<BlacklistEntity, String> {

}
