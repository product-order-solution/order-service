package com.techie.microservices.order;

import com.techie.microservices.order.dto.OrderResponse;
import com.techie.microservices.order.event.OrderPlacedEvent;
import com.techie.microservices.order.stubs.InventoryClientStub;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceWithKafkaConsumerTests {

    @Autowired
    private MySQLContainer<?> mysqlContainer;

    @Autowired
    private KafkaContainer kafkaContainer;

    @LocalServerPort
    private Integer port;

    @Value("${spring.kafka.template.default-topic}")
    private String kafkaTopic;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaConsumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // With auto-commit false, we have precise control over when offsets are committed (convenient for testing)
        // This allows us to ensure that we only commit offsets after we've fully processed and verified a message.
        // By default, auto-commit happens every 5 seconds. If your test finishes before this interval, the offset
        // might not be committed.
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                kafkaContainer.getBootstrapServers(),
                "test-group",
                "false" //Set auto-commit to false enabling your test to control when to commit.
        );
        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(kafkaTopic));

        // Clear any existing messages from the topic (perhaps from previous tests)
        clearTopicMessages();
    }

    private void clearTopicMessages() {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
        if (!records.isEmpty()) {
            consumer.commitSync();
        }
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void contextLoads() {
        assert mysqlContainer.isRunning();
        assert kafkaContainer.isRunning();
    }

    @Test
    void shouldSubmitOrderAndSendKafkaMessage() throws Exception {

        // Stub the inventory service
        InventoryClientStub.stubInventoryCall("iPhone_15", 1);

        OrderResponse orderResponse = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + TestConstants.MOCK_JWT)
                .body(TestConstants.REQUEST_BODY)
                .when()
                .post("/api/order")
                .then()
                .statusCode(201)
                .extract()
                .as(OrderResponse.class);

        assertThat(orderResponse.getMessage(), is(TestConstants.EXPECTED_RESPONSE));

        // Consume the Kafka message
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        assertThat(records.count(), is(1));

        ConsumerRecord<String, String> record = records.iterator().next();
        String messageValue = record.value();
        assertNotNull(messageValue);

        // Parse the JSON message
        OrderPlacedEvent orderPlacedEvent = objectMapper.readValue(messageValue, OrderPlacedEvent.class);

        // Assert the content of the message
        assertNotNull(orderPlacedEvent.getOrderNumber());
        assertEquals("cd@gmail.com", orderPlacedEvent.getEmail());

        // Commit the offset to mark the message as processed
        consumer.commitSync();
    }
}