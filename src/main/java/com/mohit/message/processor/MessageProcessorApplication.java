package com.mohit.message.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MessageProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageProcessorApplication.class, args);
    }

}
