package com.bankcore;

import com.bankcore.alert.service.SuspiciousActivityProperties;
import com.bankcore.config.CorsProperties;
import com.bankcore.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        CorsProperties.class,
        SuspiciousActivityProperties.class
})
public class BankCoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankCoreApiApplication.class, args);
    }
}
