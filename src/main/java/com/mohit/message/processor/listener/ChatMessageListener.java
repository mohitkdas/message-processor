package com.mohit.message.processor.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.message.processor.model.ChatMessage;
import com.mohit.message.processor.repository.MessageRepository;
import com.mohit.message.processor.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ChatMessageListener {

    private final SqsClient sqsClient;
    private final MessageRepository messageRepository;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private final String queueUrl = "https://sqs.us-east-1.amazonaws.com/755431179999/chat-message-queue.fifo";

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ChatMessageListener(SqsClient sqsClient, MessageRepository messageRepository, CacheService cacheService) {
        this.sqsClient = sqsClient;
        this.messageRepository = messageRepository;
        this.cacheService = cacheService;
        this.objectMapper = new ObjectMapper();
    }

    @Scheduled(fixedDelay = 500)
    public void receiveMessages() {
        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(10) // Long polling
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = response.messages();

            for (Message message : messages) {
                processMessage(message);
            }

        } catch (Exception e) {
            log.error("SQS polling error: {}", e.getMessage(), e);
        }
    }

    private void processMessage(Message message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.body(), ChatMessage.class);
            log.info("Processing message from {}: {}", chatMessage.getSender(), chatMessage.getContent());

            messageRepository.saveMessage(chatMessage);
            cacheService.cacheMessage(chatMessage.getRoomId(), chatMessage);

            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());

        } catch (Exception e) {
            log.error("Message processing failed: {}", e.getMessage(), e);
        }
    }
}
