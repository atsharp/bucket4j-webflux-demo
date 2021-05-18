package com.bucket4j.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@Import(MockAsyncConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private WebTestClient webTestClient;

/*
	@BeforeEach
	public void setup() {
		this.webTestClient = WebTestClient
				.bindToApplicationContext(this.context)
				.configureClient()
				.build();
	}
*/

	@Test
	void testRateLimiting() {
		webTestClient.get()
				.uri("/test-rate-limit")
				.exchange()
				.expectStatus()
				.isEqualTo(429);
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config  {

		@RestController
		@RequestMapping
		public class TestController {

			@GetMapping("/test-rate-limit")
			public Mono<Void> getRateLimit() {
				return Mono.empty();
			}

		}

	}

}