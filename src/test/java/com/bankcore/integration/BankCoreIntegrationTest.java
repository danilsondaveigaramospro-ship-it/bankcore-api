package com.bankcore.integration;

import com.bankcore.account.dto.AccountResponse;
import com.bankcore.account.dto.CreateAccountRequest;
import com.bankcore.account.dto.DepositRequest;
import com.bankcore.account.dto.WithdrawRequest;
import com.bankcore.auth.dto.LoginRequest;
import com.bankcore.auth.dto.LoginResponse;
import com.bankcore.common.enums.AccountType;
import com.bankcore.common.enums.CurrencyCode;
import com.bankcore.common.enums.KycStatus;
import com.bankcore.customer.dto.CreateCustomerRequest;
import com.bankcore.customer.dto.CustomerResponse;
import com.bankcore.transaction.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class BankCoreIntegrationTest {

    static {
        // docker-java defaults to Docker API v1.32, rejected by Docker Engine 29+; v1.41 spans Docker 20.10 through 29+.
        System.setProperty("api.version", System.getProperty("api.version", "1.41"));
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bankcore_it")
            .withUsername("bankcore")
            .withPassword("bankcore_dev_password");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("bankcore.jwt.secret", () -> "integration_test_secret_with_more_than_32_bytes");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void authCustomerAccountOperationsAndAccessControlFlow() {
        LoginResponse admin = login("admin@bankcore.local");
        LoginResponse employee = login("employee@bankcore.local");
        LoginResponse alice = login("alice@bankcore.local");
        LoginResponse bob = login("bob@bankcore.local");

        assertThat(admin.role().name()).isEqualTo("ROLE_ADMIN");
        assertThat(employee.role().name()).isEqualTo("ROLE_BANK_EMPLOYEE");
        assertThat(alice.role().name()).isEqualTo("ROLE_CUSTOMER");

        CustomerResponse customer = post(
                "/api/v1/customers",
                new CreateCustomerRequest(
                        "charlie@example.com",
                        "Password123!",
                        "Charlie",
                        "Dubois",
                        LocalDate.of(1990, 1, 1),
                        "+41790000000",
                        "Rue Test 1",
                        null,
                        "1000",
                        "Lausanne",
                        "Switzerland",
                        KycStatus.VERIFIED
                ),
                employee.accessToken(),
                CustomerResponse.class
        ).getBody();
        assertThat(customer).isNotNull();

        AccountResponse account = post(
                "/api/v1/accounts",
                new CreateAccountRequest(customer.id(), CurrencyCode.CHF, AccountType.CHECKING, new BigDecimal("12000.00")),
                employee.accessToken(),
                AccountResponse.class
        ).getBody();
        assertThat(account).isNotNull();

        post("/api/v1/accounts/" + account.id() + "/deposit",
                new DepositRequest(new BigDecimal("1000.00"), CurrencyCode.CHF, "integration deposit", false),
                employee.accessToken(),
                Map.class
        );
        post("/api/v1/accounts/" + account.id() + "/withdraw",
                new WithdrawRequest(new BigDecimal("100.00"), CurrencyCode.CHF, "integration withdraw"),
                employee.accessToken(),
                Map.class
        );

        ResponseEntity<Map> transferResponse = post(
                "/api/v1/transfers",
                new TransferRequest(
                        java.util.UUID.fromString("20000000-0000-0000-0000-000000000001"),
                        java.util.UUID.fromString("20000000-0000-0000-0000-000000000003"),
                        new BigDecimal("11000.00"),
                        CurrencyCode.CHF,
                        "large transfer"
                ),
                alice.accessToken(),
                Map.class
        );
        assertThat(transferResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> forbidden = exchange(
                "/api/v1/accounts/20000000-0000-0000-0000-000000000001",
                HttpMethod.GET,
                null,
                bob.accessToken(),
                String.class
        );
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<String> alerts = exchange("/api/v1/alerts", HttpMethod.GET, null, employee.accessToken(), String.class);
        assertThat(alerts.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(alerts.getBody()).contains("LARGE_TRANSFER");
    }

    private LoginResponse login(String email) {
        return post("/api/v1/auth/login", new LoginRequest(email, "Password123!"), null, LoginResponse.class).getBody();
    }

    private <T> ResponseEntity<T> post(String path, Object body, String token, Class<T> responseType) {
        return exchange(path, HttpMethod.POST, body, token, responseType);
    }

    private <T> ResponseEntity<T> exchange(String path, HttpMethod method, Object body, String token, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return rest.exchange("http://localhost:" + port + path, method, new HttpEntity<>(body, headers), responseType);
    }
}
