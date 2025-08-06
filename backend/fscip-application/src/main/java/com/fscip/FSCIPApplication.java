package com.fscip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {
    "com.fscip"
})
@EnableJpaRepositories(basePackages = {
    "com.fscip.common.repository",
    "com.fscip.identity.repository", 
    "com.fscip.account.repository",
    "com.fscip.rules.repository",
    "com.fscip.search.repository", 
    "com.fscip.notification.repository",
    "com.fscip.document.repository"
})
@EntityScan(basePackages = {
    "com.fscip.common.entity",
    "com.fscip.identity.entity",
    "com.fscip.account.entity", 
    "com.fscip.rules.entity",
    "com.fscip.search.entity",
    "com.fscip.notification.entity",
    "com.fscip.document.entity"
})
@EnableKafka
@EnableTransactionManagement
@EnableScheduling
public class FSCIPApplication {

    public static void main(String[] args) {
        SpringApplication.run(FSCIPApplication.class, args);
    }
}