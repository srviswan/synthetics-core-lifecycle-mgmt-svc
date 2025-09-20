package com.synthetics.core.cashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Cashflow Management Service
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CashflowManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashflowManagementServiceApplication.class, args);
    }
}
