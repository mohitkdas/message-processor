package com.mohit.message.processor.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.message.processor.model.ChatMessage;
import com.mohit.message.processor.repository.MessageRepository;
import com.mohit.message.processor.service.CacheService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

import java.util.List;

@Slf4j
@Service
public class ChatMessageListener {

    private final SqsClient sqsClient;
    private final MessageRepository messageRepository;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private final String queueUrl = "https://sqs.us-east-1.amazonaws.com/755431179999/chat-message-queue";

    public ChatMessageListener(SqsClient sqsClient, MessageRepository messageRepository, CacheService cacheService) {
        this.sqsClient = sqsClient;
        this.messageRepository = messageRepository;
        this.cacheService = cacheService;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void receiveMessages() {
        new Thread(() -> {
            while (true) {
                try {
                    ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .maxNumberOfMessages(10)
                            .build();

                    ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
                    List<Message> messages = response.messages();

                    for (Message message : messages) {
                        log.info("Received message: {}", message.body());
                        processMessage(message.body());

                        // Delete the message after processing
                        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .receiptHandle(message.receiptHandle())
                                .build());
                    }
                    Thread.sleep(1000);  // Delay to prevent tight looping
                } catch (Exception e) {
                    log.error("Error receiving messages from SQS: {}", e.getMessage());
                }
            }
        }).start();
    }

    private void processMessage(String messageBody) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(messageBody, ChatMessage.class);
            log.info("Processing message from {}: {}", chatMessage.getSender(), chatMessage.getContent());

            messageRepository.saveMessage(chatMessage);
            log.info("Message saved to DynamoDB");

            cacheService.cacheMessage(chatMessage.getRoomId(), chatMessage);
            log.info("Message cached in Redis");
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
        }
    }
}
