package com.meesho.NotificationService.utils;

import com.meesho.NotificationService.models.ElasticSearchEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Data
@NoArgsConstructor
public class ElasticSearchResponse {
    private List<ElasticSearchEntity> smsList;
    private int totalPage;
    private int totalSize;

    public ElasticSearchResponse(List<ElasticSearchEntity> smsList, int totalPage, int totalSize){
        this.smsList = smsList;
        this.totalPage = totalPage;
        this.totalSize = totalSize;
    }
}
