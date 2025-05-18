package com.mohit.message.processor.repository;

import com.mohit.message.processor.model.ChatMessage;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Repository
public class MessageRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "chat-messages";

    public MessageRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void saveMessage(ChatMessage message) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("messageId", AttributeValue.builder().s(message.getRoomId() + "-" + message.getTimestamp()).build());
        item.put("roomId", AttributeValue.builder().s(message.getRoomId()).build());
        item.put("sender", AttributeValue.builder().s(message.getSender()).build());
        item.put("content", AttributeValue.builder().s(message.getContent()).build());
        item.put("timestamp", AttributeValue.builder().n(String.valueOf(message.getTimestamp())).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }
}
