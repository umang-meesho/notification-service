package com.meesho.NotificationService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meesho.NotificationService.controller.NotificationRestController;
import com.meesho.NotificationService.exceptions.SmsRequestException;
import com.meesho.NotificationService.models.BlacklistEntity;
import com.meesho.NotificationService.models.ElasticSearchEntity;
import com.meesho.NotificationService.models.SmsRequestEntity;
import com.meesho.NotificationService.repository.BlacklistRepository;
import com.meesho.NotificationService.repository.ElasticSearchRepository;
import com.meesho.NotificationService.repository.MessageRepository;
import com.meesho.NotificationService.service.ElasticSearchService;
import com.meesho.NotificationService.service.MessagingService;
import com.meesho.NotificationService.service.NotificationServiceImpl;
import com.meesho.NotificationService.service.RedisService;
import com.meesho.NotificationService.utils.BlacklistRequest;
import com.meesho.NotificationService.utils.ElasticSearchResponse;
import com.meesho.NotificationService.utils.SmsRequest;
import com.meesho.NotificationService.utils.SmsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationServiceApplicationTests {

    @MockBean
    private MessageRepository messageRepository;

    @Mock
    private ElasticSearchRepository elasticSearchRepository;

    @Mock
    private BlacklistRepository blacklistRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private NotificationServiceImpl notificationService;

    @InjectMocks
    private RedisService redisService;

    @InjectMocks
    private ElasticSearchService elasticSearchService;

    @InjectMocks
    private MessagingService messagingService;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private NotificationRestController notificationRestController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    // Notification Service Tests

    @Test
    public void testValidateSmsRequest_ValidRequest() {
        // Given
        SmsRequest smsRequest = new SmsRequest("+919634916827", "Test Message");

        // When, Then
        assertDoesNotThrow(() -> messagingService.validateSmsRequest(smsRequest));
    }


    @Test
    public void testValidateSmsRequest_MissingMessage() {
        // Given
        SmsRequest smsRequest = new SmsRequest( "+911234567890", "");

        // When, Then
        SmsRequestException exception = assertThrows(SmsRequestException.class, () -> messagingService.validateSmsRequest(smsRequest));
        assertEquals("message is mandatory", exception.getMessage());
        assertEquals(SmsStatus.INVALID_REQUEST, exception.getSmsStatus());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    public void testValidateSmsRequest_MissingPhoneNumber() {
        // Given
        SmsRequest smsRequest = new SmsRequest("", "Test Message");

        // When, Then
        SmsRequestException exception = assertThrows(SmsRequestException.class, () -> messagingService.validateSmsRequest(smsRequest));
        assertEquals("phoneNumber is mandatory", exception.getMessage());
        assertEquals(SmsStatus.INVALID_REQUEST, exception.getSmsStatus());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    // this should pass if the number is invalid (excluding null)
    // Criteria ->  Number is not in the format +91[10-digit number]
    @Test
    public void testValidatePhoneNumber() {
        String phoneNumber = "+921234567890";

        Exception exception = assertThrows(SmsRequestException.class, () -> {
            NotificationRestController.validatePhoneNumber(phoneNumber);
        });

        String expectedMessage = "Invalid Phone Number: " + phoneNumber;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    public void testSaveSmsRequest_SuccessfulSave() {
        // Given
        SmsRequestEntity smsRequestEntity = new SmsRequestEntity("1FDG545YBBJ5BJH", "+911234567890", "Test Message");
        when(messageRepository.save(any(SmsRequestEntity.class))).thenReturn(smsRequestEntity);

        // When
        notificationService.saveSmsRequest(smsRequestEntity);

        // Then
        verify(messageRepository, times(1)).save(smsRequestEntity);
    }

    @Test
    public void testSaveSmsRequest_SaveFailure() {
        // Given
        SmsRequestEntity smsRequestEntity = new SmsRequestEntity("1", "+919634916827", "Test Message");
        doThrow(RuntimeException.class).when(messageRepository).save(smsRequestEntity);

        // When, Then
        SmsRequestException exception = assertThrows(SmsRequestException.class,
                () -> notificationService.saveSmsRequest(smsRequestEntity));
        assertEquals("Your request cannot be processed right now. Try after some time.", exception.getMessage());
        assertEquals(SmsStatus.FAILED, exception.getSmsStatus());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    // test when the requestId is not valid then the function should throw exception
    @Test
    public void testGetSmsRequestGetByIdFromMongoDb() {
        String requestId = "6AB25E1CCE734660B6E1DEA79AD73FC5";
        when(messageRepository.findById(requestId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(SmsRequestException.class, () -> {
            notificationService.getSmsRequestById(requestId);
        });

        assertTrue(("Invalid request ID: " + requestId).contains(exception.getMessage()));
    }


    // ElasticSearch Test

    @Test
    public void testSaveRequest() {
        // Given
        ElasticSearchEntity entity = new ElasticSearchEntity("1", "+919876543210", "Test message", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        when(elasticSearchRepository.save(entity)).thenReturn(entity);

        // When
        ElasticSearchEntity savedEntity = elasticSearchService.saveRequest(entity);

        // Then
        assertEquals(entity, savedEntity);
        verify(elasticSearchRepository, times(1)).save(entity);
    }

    @Test
    public void testGetSms_NoHits() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 2, 0, 0);
        String phoneNumber = "+919876543210";

        SearchHits<ElasticSearchEntity> searchHits = mock(SearchHits.class);
        when(elasticsearchOperations.search(any(StringQuery.class), eq(ElasticSearchEntity.class), any(IndexCoordinates.class))).thenReturn(searchHits);
        when(searchHits.getSearchHits()).thenReturn(new ArrayList<>());

        // When
        ElasticSearchResponse response = elasticSearchService.getSms(phoneNumber, startTime, endTime, 0, 10);

        // Then
        assertEquals(0, response.getTotalPage());
        assertEquals(0, response.getTotalSize());
        assertTrue(response.getSmsList().isEmpty());
    }



    @Test
    public void testGetSms_WithHits() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 2, 0, 0);
        String phoneNumber = "+919876543210";
        StringQuery expectedSearchQuery = new StringQuery(
                "{ \"bool\": {\"must\": [{\"match\": {\"phoneNumber.keyword\": \"+919876543210\"}},{\"range\": {\"updatedAt\": {\"gte\":   " + startTime.toEpochSecond(ZoneOffset.UTC) + ", \"lte\": " + endTime.toEpochSecond(ZoneOffset.UTC) + "}}}]}}");

        List<ElasticSearchEntity> hits = new ArrayList<>();
        hits.add(new ElasticSearchEntity("1", phoneNumber, "Test message", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));
        hits.add(new ElasticSearchEntity("2", phoneNumber, "Another message", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

        SearchHits<ElasticSearchEntity> searchHits = mock(SearchHits.class);
        when(elasticsearchOperations.search(any(StringQuery.class), eq(ElasticSearchEntity.class), any(IndexCoordinates.class))).thenReturn(searchHits);
        when(searchHits.getSearchHits()).thenAnswer(invocationOnMock -> hits);

        // When
        ElasticSearchResponse response = elasticSearchService.getSms(phoneNumber, startTime, endTime, 0, 10);

        // Then
        assertEquals(1, response.getTotalPage());
        assertEquals(2, response.getTotalSize());
        assertEquals(2, response.getSmsList().size());
        verify(elasticsearchOperations, times(1)).search(expectedSearchQuery, ElasticSearchEntity.class, IndexCoordinates.of("sms_requests"));
    }


    // check if the number is blacklisted
    @Test
    public void testBlacklistNumber() {
        String phoneNumber = "+911234567890";

        when(blacklistRepository.findById(phoneNumber)).thenReturn(Optional.empty());

        assertTrue(null == redisService.getUserById(phoneNumber));
    }

    // Notification REST Controller Test

    // test if the post request to send sms is working as desired if the number is blacklisted
    @Test
    public void testPostSmsRequestIfNumberIsBlacklisted() throws Exception {

        SmsRequest smsRequest = new SmsRequest("+919829906261", "Send a message to this number");
        when(blacklistRepository.findById(any(smsRequest.getPhoneNumber().getClass()))).thenReturn(Optional.of(new BlacklistEntity(smsRequest.getPhoneNumber())));

        // Perform the HTTP POST request to /sms/send endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/sms/send")
                        .content(asJsonString(smsRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().json("{" +
                        "\"error\": {\"code\": \"FAILED\",\"message\": \"Phone number is blacklisted "  + smsRequest.getPhoneNumber() +"\"}}"
                        ));


//        verify(blacklistRepository, times(1)).findById(anyString());

    }

    @Test
    public void testSmsPostRequest_ValidRequest_NoBlacklist() throws Exception {

        SmsRequest smsRequest = new SmsRequest("+911236567890", "Send a message to this number");

        // Mocking behavior
        when(blacklistRepository.findById(anyString())).thenReturn(Optional.empty());
        when(messageRepository.save(any(SmsRequestEntity.class))).thenReturn(any(SmsRequestEntity.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/sms/send")
                        .content(asJsonString(smsRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\n" +
                        "    \"data\": {\n" +
                        "        \"requestId\": \""+smsRequest.getRequestId()+"\",\n" +
                        "        \"comments\": \"Successfully Sent\"\n" +
                        "    }\n" +
                        "}"
                ));
    }

    @Test
    public void testBlacklistPhoneNumberRequest_ValidNumbers() throws Exception {
        // Mocking behavior
        BlacklistRequest blacklistRequest = new BlacklistRequest(List.of("+911234567890", "+919876543210"));

        when(blacklistRepository.save(any(BlacklistEntity.class))).thenReturn(new BlacklistEntity("+911234567890"));


        mockMvc.perform(MockMvcRequestBuilders.post("/v1/blacklist")
                        .content(asJsonString(blacklistRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\n" +
                        "    \"data\": \"Successfully blacklisted\"\n" +
                        "}"
                ));
    }

    @Test
    public void testDeleteBlacklistPhoneNumberRequest() throws Exception {
        BlacklistRequest blacklistRequest = new BlacklistRequest(List.of("+911234567890", "+919876543210"));
        doNothing().when(blacklistRepository).deleteById(anyString());

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/blacklist")
                        .content(asJsonString(blacklistRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\n" +
                        "    \"data\": \"Successfully whitelisted\"\n" +
                        "}"
                ));


    }




    // Helper method to convert an object to JSON string
    private static String asJsonString(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testFindByPhoneNumber() {
        // Given
        String phoneNumber = "+911234567890";

        BlacklistEntity blacklistEntity = new BlacklistEntity(phoneNumber);
        when(blacklistRepository.findById(phoneNumber)).thenReturn(Optional.of(blacklistEntity));

        // When
        BlacklistEntity foundEntity = redisService.getUserById(phoneNumber);

        // Then
        assertEquals(blacklistEntity, foundEntity);
    }

    @Test
    public void testSaveBlacklistEntity() {
        // Given
        String phoneNumber = "+911234567890";
        BlacklistEntity blacklistEntity = new BlacklistEntity(phoneNumber);
        when(blacklistRepository.save(blacklistEntity)).thenReturn(blacklistEntity);

        // When
        BlacklistEntity savedEntity = redisService.addPhoneNumber(phoneNumber);

        // Then
        assertEquals(blacklistEntity, savedEntity);
        verify(blacklistRepository, times(1)).save(blacklistEntity);
    }

    @Test
    public void testFindByRequestId() {
        // Given
        String requestId = "123";
        SmsRequestEntity smsRequestEntity = new SmsRequestEntity(requestId, "+911234567890", "Test Message");
        when(messageRepository.findById(requestId)).thenReturn(Optional.of(smsRequestEntity));

        // When
        SmsRequestEntity foundEntity = notificationService.getSmsRequestById(requestId);
        System.out.println(messageRepository.findById(requestId));

        // Then
        assertEquals(smsRequestEntity, foundEntity);
    }




}
