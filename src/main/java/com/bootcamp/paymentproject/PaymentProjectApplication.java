package com.bootcamp.paymentproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PaymentProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentProjectApplication.class, args);
    }
}
