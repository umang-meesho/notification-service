package com.meesho.NotificationService;

import com.meesho.NotificationService.repository.ElasticSearchRepository;
import com.meesho.NotificationService.repository.MessageRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = MessageRepository.class)
@EnableElasticsearchRepositories(basePackageClasses = ElasticSearchRepository.class)
@EnableCaching
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
