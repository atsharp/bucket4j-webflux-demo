package com.bucket4j.demo;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.webflux.Bucket4JAutoConfigurationWebfluxFilter;
import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.grid.ProxyManager;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Configuration
@AutoConfigureBefore(Bucket4JAutoConfigurationWebfluxFilter.class)
public class MockAsyncConfiguration {

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
    final ConsumptionProbe consumptionProbe = ConsumptionProbe.rejected(0, 5);
    doReturn(CompletableFuture.completedFuture(consumptionProbe)).when(asyncBucket).tryConsumeAndReturnRemaining(anyLong());
    doReturn(asyncBucket).when(bucket).asAsync();
    final ProxyManager<String> proxyManager = mock(ProxyManager.class);
    doReturn(Optional.of(bucket)).when(proxyManager).getProxy(any());
    doReturn(bucket).when(bucket).toListenable(any());
    doReturn(bucket).when(proxyManager).getProxy(any(), any(BucketConfiguration.class));
    return proxyManager;
  }

}