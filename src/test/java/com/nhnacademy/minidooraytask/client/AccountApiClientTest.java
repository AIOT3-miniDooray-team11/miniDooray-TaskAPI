package com.nhnacademy.minidooraytask.client;

import com.nhnacademy.minidooraytask.client.dto.account.AccountResp;
import com.nhnacademy.minidooraytask.client.dto.account.UserStatus;
import com.nhnacademy.minidooraytask.config.ApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper; // Jackson 3

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class AccountApiClientTest {

    private AccountApiClient accountApiClient;
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ApiProperties apiProperties = new ApiProperties();
        apiProperties.setAccountUrl("http://localhost:8081/account-api/v1/accounts");

        RestClient.Builder builder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        this.accountApiClient = new AccountApiClient(restClient, apiProperties);
    }

    @Test
    @DisplayName("Account ID로 회원 조회")
    void getAccountById_success() throws Exception {
        long accountId = 1L;
        String requestUrl = "http://localhost:8081/account-api/v1/accounts" + "/" + accountId;

        AccountResp mockResponse = new AccountResp(
                accountId, "testUser", "test@dooray.com", "홍길동", UserStatus.ACTIVE, LocalDateTime.now()
        );
        String expectedJsonResponse = objectMapper.writeValueAsString(mockResponse);

        mockServer.expect(requestTo(requestUrl))
                .andRespond(withSuccess(expectedJsonResponse, MediaType.APPLICATION_JSON));

        AccountResp result = accountApiClient.getAccountById(accountId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);

        mockServer.verify();
    }
}