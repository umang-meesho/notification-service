package com.meesho.NotificationService.repository;

import com.meesho.NotificationService.models.ElasticSearchEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticSearchRepository extends ElasticsearchRepository<ElasticSearchEntity, String> {

}
