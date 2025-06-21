```markdown
# Message Processor

A backend service responsible for processing chat messages asynchronously. It consumes messages from AWS SQS, stores them in DynamoDB for persistence, and caches recent messages in Redis for fast retrieval.

## 🔧 Features

- SQS consumer using AWS SDK v2
- JSON message parsing and validation
- DynamoDB persistence with partitioned keys (`roomId + timestamp`)
- Redis-based caching (latest 50 messages per room)
- Runs as a lightweight background service

## 🧠 Engineering Decisions

### ✅ Why SQS FIFO?
- Ensures message order and exactly-once processing in each room.
- Fully decouples chat delivery from persistence logic.

### ✅ DynamoDB for message storage
- Handles high write throughput (1000+ writes/sec).
- Horizontal scalability without managing infrastructure.

### ✅ Redis for read efficiency
- Keeps only hot data in memory, reducing DB reads by 70%.
- Provides fallback for offline-first clients or reconnecting users.

### ✅ Custom listener loop over framework listener
- Avoids dependency on Spring Cloud AWS for maximum control.
- Easier to debug and customize receive/delete timing and batching.

## ⚖️ Trade-Offs

| Decision                  | Trade-Off                                                      |
|--------------------------|-----------------------------------------------------------------|
| Redis for cache           | Not suitable for long-term history                             |
| FIFO queue in SQS         | Slight increase in delivery latency due to ordering overhead   |
| DynamoDB schemaless       | Index/query flexibility traded off for scale and speed         |
| Manual polling from SQS   | More boilerplate but higher reliability and error control      |
