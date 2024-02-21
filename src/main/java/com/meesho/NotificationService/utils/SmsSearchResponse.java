package com.meesho.NotificationService.utils;

import com.meesho.NotificationService.models.ElasticSearchEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SmsSearchResponse {
    private List<ElasticSearchEntity> smsList;
    private int totalPage;
    private int totalElements;

    @Override
    public String toString() {
        return "SmsSearchResponse{" +
                "smsList=" + smsList +
                ", totalPage=" + totalPage +
                ", totalElements=" + totalElements +
                '}';
    }
}
