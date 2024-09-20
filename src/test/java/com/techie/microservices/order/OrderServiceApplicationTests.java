package com.techie.microservices.order;

import com.techie.microservices.order.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MySQLContainer;

import static org.hamcrest.MatcherAssert.assertThat;

// The @Import annotation explicitly includes the TestcontainersConfiguration class in the test, making the
// mysqlContainer available as a bean.
// @SpringBootTest annotation tells Spring Boot to start the application context, and to run in a random port.
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port=0)			// This annotation tells WireMock to use a random port.
class OrderServiceApplicationTests {

	// Inject the MySQLContainer bean
	@Autowired
	private MySQLContainer<?> mysqlContainer;

	//When the test run, the port chosen will be added to this variable.
	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		// Configuration for RestAssured.
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}


	@Test
	void contextLoads() {
		// Verify that the container is running
		assert mysqlContainer.isRunning();

		// You can also access other container information, e.g.:
		System.out.println("MySQL JDBC URL: " + mysqlContainer.getJdbcUrl());
		System.out.println("MySQL Username: " + mysqlContainer.getUsername());
		System.out.println("MySQL Password: " + mysqlContainer.getPassword());
	}

	@Test
	void shouldSubmitOrder() {
		String requestBody = """
			{
				"skuCode": "iPhone_15",
				"price": 1000,
				"quantity": 1
			}""";

		InventoryClientStub.stubInventoryCall("iPhone_15", 1);

		var responseBodyString = RestAssured.given()
				.header("Content-Type", "application/json")
				.body(requestBody)
				.when()
				.post("/api/order")
				.then()
				.statusCode(201)
				.extract()
				.body().asString();

		assertThat(responseBodyString, Matchers.is("Order Placed Successfully"));



	}

}
