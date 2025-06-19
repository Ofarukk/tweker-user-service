package com.tweker.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class UserServiceApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
			.withDatabaseName("test-db")
			.withUsername("testuser")
			.withPassword("testpass");

	@Container
	static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/%s",
				postgres.getHost(),
				postgres.getMappedPort(5432),
				postgres.getDatabaseName());

		registry.add("spring.r2dbc.url", () -> r2dbcUrl);
		registry.add("spring.r2dbc.username", postgres::getUsername);
		registry.add("spring.r2dbc.password", postgres::getPassword);

		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
		registry.add("KAFKA_BOOTSTRAP_SERVERS", kafka::getBootstrapServers);
	}

	@Test
	void contextLoads() {
	}
}