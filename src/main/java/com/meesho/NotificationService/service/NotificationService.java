package com.meesho.NotificationService.service;

import com.meesho.NotificationService.utils.SmsRequest;
import com.meesho.NotificationService.utils.SmsStatus;
import com.meesho.NotificationService.models.SmsRequestEntity;

public interface NotificationService {

    public void saveSmsRequest(SmsRequestEntity smsRequest);

    public void sendRequestToKafka(String requestId);


    public SmsRequestEntity getSmsRequestById(String requestId);

}
