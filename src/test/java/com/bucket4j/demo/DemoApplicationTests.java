package com.bucket4j.demo;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.webflux.Bucket4JAutoConfigurationWebfluxFilter;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.WebfluxWebFilter;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.grid.ProxyManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

	@MockBean
	AsyncCacheResolver asyncCacheResolver;
	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testRateLimiting() {
		ProxyManager<String> proxyManager = mock(ProxyManager.class);
		doReturn(proxyManager).when(asyncCacheResolver).resolve(any());
		final Bucket bucket = mock(Bucket.class);
		doReturn(Optional.of(bucket)).when(proxyManager).getProxy(any());
		doReturn(bucket).when(proxyManager).getProxy(any(), any(Supplier.class));
		doReturn(bucket).when(proxyManager).getProxy(any(), any(BucketConfiguration.class));
		doReturn(0L).when(bucket).getAvailableTokens();
		// there are 0 tokens, so this should be rate-limited
		webTestClient.get()
				.uri("/test-rate-limit")
				.exchange()
				.expectStatus()
				.isEqualTo(429);
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableConfigurationProperties(Bucket4JBootProperties.class)
	static class Config  {

		@Bean
		public AsyncCacheResolver asyncCacheResolver() {
			return mock(AsyncCacheResolver.class);
		}

		@RestController
		@RequestMapping
		public class TestController {

			@GetMapping("/test-rate-limit")
			public Mono<Void> getRateLimit() {
				return Mono.empty();
			}

		}

		// uncomment this out and it works as expected
		/*@Bean
		public WebfluxWebFilter manual(Bucket4JBootProperties properties, AsyncCacheResolver asyncCacheResolver) {
			final Bucket4JConfiguration conf = properties.getFilters().get(0);
			FilterConfiguration filterConfig = new FilterConfiguration<>();
			filterConfig.setUrl(conf.getUrl());
			filterConfig.setOrder(conf.getFilterOrder());
			filterConfig.setStrategy(conf.getStrategy());
			filterConfig.setHttpResponseBody(conf.getHttpResponseBody());
			filterConfig.setHttpResponseHeaders(conf.getHttpResponseHeaders());
			filterConfig.setMetrics(conf.getMetrics());
			filterConfig.getRateLimitChecks().add(new RateLimitCheck() {

				@Override
				public ConsumptionProbeHolder rateLimit(Object request, boolean async) {
					// hard-coding this to make demo easier
					return new ConsumptionProbeHolder(asyncCacheResolver.resolve("buckets").getProxy("/test-rate-limit").get().tryConsumeAndReturnRemaining(1));
				}

			});
			return new WebfluxWebFilter(filterConfig);
		}*/

	}

}
