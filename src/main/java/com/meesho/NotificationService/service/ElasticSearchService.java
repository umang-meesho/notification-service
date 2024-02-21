package com.meesho.NotificationService.service;

import com.meesho.NotificationService.NotificationServiceApplication;
import com.meesho.NotificationService.models.ElasticSearchEntity;
import com.meesho.NotificationService.repository.ElasticSearchRepository;
import com.meesho.NotificationService.utils.ElasticSearchResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticSearchService {

    @Autowired
    private ElasticSearchRepository elasticSearchRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public static final Logger log = LogManager.getLogger(NotificationServiceApplication.class);

    public ElasticSearchEntity saveRequest(ElasticSearchEntity elasticSearchEntity) {
        ElasticSearchEntity elasticSearchSavedEntity = elasticSearchRepository.save(elasticSearchEntity);
        log.info("Request insert into elastic search: " + elasticSearchEntity);
        return elasticSearchSavedEntity;
    }

    public ElasticSearchResponse getSms(@NotNull String phoneNumber, LocalDateTime startTime, LocalDateTime endTime, int page, int size) {

        StringQuery searchQuery = new StringQuery(
                "{ \"bool\": {" +
                        "\"must\": [" +
                        "{\"match\": {\"phoneNumber.keyword\": \"+" + phoneNumber.trim() + "\"}}," +
                        "{\"range\": {\"updatedAt\": {\"gte\":   " + startTime.toEpochSecond(ZoneOffset.UTC) + ", \"lte\": " + endTime.toEpochSecond(ZoneOffset.UTC) + "}}}" +
                        "]" +
                        "}}");

        return processElasticRequest(searchQuery, page, size);
    }

    public ElasticSearchResponse searchSmsText(String text, int page, int size) {

        StringQuery searchQuery = new StringQuery(
                "{\"bool\": {\"must\": [{\"match\": {\"message\": {\"query\": " + text + "}}}]}}");

        return processElasticRequest(searchQuery, page, size);

    }

    private ElasticSearchResponse processElasticRequest(StringQuery searchQuery, int page, int size) {
        log.info("Search Request - " + searchQuery.getSource());

        SearchHits<ElasticSearchEntity> searchHits = elasticsearchOperations.search(
                searchQuery,
                ElasticSearchEntity.class,
                IndexCoordinates.of("sms_requests"));


        List<ElasticSearchEntity> smsList = searchHits.getSearchHits()
                .stream()
                .map(hit -> mapToSmsResponse(hit.getContent()))
                .toList();

        log.info("SearchSMSText - Documents fetched from the ElasticSearch");

        int totalHits = smsList.size();

        // get the documents for a particular page
        if (page * size > Math.min((page + 1) * size, smsList.size())) {
            smsList = new ArrayList<>();
        } else {
            smsList = smsList.subList(page * size, Math.min((page + 1) * size, smsList.size()));
        }

        return new ElasticSearchResponse(smsList, (int) Math.ceil((double) totalHits / size), totalHits);
    }

    private ElasticSearchEntity mapToSmsResponse(ElasticSearchEntity entity) {
        return entity;
    }

}
