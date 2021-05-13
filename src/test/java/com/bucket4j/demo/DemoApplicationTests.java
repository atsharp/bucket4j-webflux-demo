package com.bucket4j.demo;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.WebfluxWebFilter;
import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.grid.ProxyManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

	@Autowired
	private WebTestClient webTestClient;
	@Autowired
	private Collection<WebFilter> webFilters;

	@Test
	void testRateLimiting() {
		assertThat(webFilters)
				.anyMatch(filter -> filter instanceof WebfluxWebFilter);
		webTestClient.get()
				.uri("/test-rate-limit")
				.exchange()
				.expectStatus()
				.isEqualTo(429);
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config  {

		private WebFilter delegate;

		@Bean
		public AsyncCacheResolver asyncCacheResolver() {
			final AsyncCacheResolver cacheResolver = mock(AsyncCacheResolver.class);
			doReturn(proxyManager()).when(cacheResolver).resolve(any());
			return cacheResolver;
		}

		@Bean
		public ProxyManager<String> proxyManager() {
			final Bucket bucket = mock(Bucket.class);
			final AsyncBucket asyncBucket = mock(AsyncBucket.class);
			final ConsumptionProbe consumptionProbe = ConsumptionProbe.rejected(0, 1000);
			doReturn(CompletableFuture.completedFuture(consumptionProbe)).when(asyncBucket).tryConsumeAndReturnRemaining(anyLong());
			doReturn(asyncBucket).when(bucket).asAsync();
			final ProxyManager<String> proxyManager = mock(ProxyManager.class);
			doReturn(Optional.of(bucket)).when(proxyManager).getProxy(any());
			doReturn(bucket).when(bucket).toListenable(any());
			doReturn(bucket).when(proxyManager).getProxy(any(), any(BucketConfiguration.class));
			return proxyManager;
		}

		@RestController
		@RequestMapping
		public class TestController {

			@GetMapping("/test-rate-limit")
			public Mono<Void> getRateLimit() {
				return Mono.empty();
			}

		}

		@Autowired
		@Lazy
		public void setDelegate(WebFilter delegate) {
			this.delegate = delegate;
		}

		// uncomment this and the test passes
		/*@Bean
		public WebFilter custom() {
			return (exchange, chain) -> delegate.filter(exchange, chain);
		}*/
	}

}
