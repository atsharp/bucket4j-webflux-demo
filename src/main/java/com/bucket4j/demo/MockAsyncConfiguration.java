package com.bucket4j.demo;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.webflux.Bucket4JAutoConfigurationWebfluxFilter;
import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.AsyncScheduledBucket;
import io.github.bucket4j.AsyncVerboseBucket;
import io.github.bucket4j.BlockingBucket;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.BucketListener;
import io.github.bucket4j.BucketState;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.EstimationProbe;
import io.github.bucket4j.TokensInheritanceStrategy;
import io.github.bucket4j.VerboseBucket;
import io.github.bucket4j.grid.ProxyManager;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Configuration
@AutoConfigureBefore(Bucket4JAutoConfigurationWebfluxFilter.class)
public class MockAsyncConfiguration {

  @Bean
  public AsyncCacheResolver asyncCacheResolver() {
    return new AsyncCacheResolver() {
      @Override
      public ProxyManager<String> resolve(String cacheName) {
        return proxyManager();
      }
    };
  }

  @Bean
  public ProxyManager<String> proxyManager() {
    return new ProxyManager<>() {

      @Override
      public Bucket getProxy(String s, Supplier<BucketConfiguration> supplier) {
        return new MockBucket();
      }

      @Override
      public Optional<Bucket> getProxy(String s) {
        return Optional.of(new MockBucket());
      }

      @Override
      public Optional<BucketConfiguration> getProxyConfiguration(String s) {
        return Optional.empty();
      }

    };
  }

  static class MockBucket implements Bucket {

    @Override
    public BlockingBucket asScheduler() {
      return null;
    }

    @Override
    public VerboseBucket asVerbose() {
      return null;
    }

    @Override
    public boolean isAsyncModeSupported() {
      return false;
    }

    @Override
    public AsyncBucket asAsync() {
      return new MockAsyncBucket();
    }

    @Override
    public AsyncScheduledBucket asAsyncScheduler() {
      return null;
    }

    @Override
    public boolean tryConsume(long l) {
      return false;
    }

    @Override
    public long consumeIgnoringRateLimits(long l) {
      return 0;
    }

    @Override
    public ConsumptionProbe tryConsumeAndReturnRemaining(long l) {
      return null;
    }

    @Override
    public EstimationProbe estimateAbilityToConsume(long l) {
      return null;
    }

    @Override
    public long tryConsumeAsMuchAsPossible() {
      return 0;
    }

    @Override
    public long tryConsumeAsMuchAsPossible(long l) {
      return 0;
    }

    @Override
    public void addTokens(long l) {

    }

    @Override
    public void forceAddTokens(long tokensToAdd) {

    }

    @Override
    public long getAvailableTokens() {
      return 0;
    }

    @Override
    public void replaceConfiguration(BucketConfiguration newConfiguration, TokensInheritanceStrategy tokensInheritanceStrategy) {

    }

    @Override
    public BucketState createSnapshot() {
      return null;
    }

    @Override
    public Bucket toListenable(BucketListener bucketListener) {
      return this;
    }
  }

  static class MockAsyncBucket implements AsyncBucket {

    @Override
    public AsyncVerboseBucket asVerbose() {
      return null;
    }

    @Override
    public CompletableFuture<Boolean> tryConsume(long l) {
      return null;
    }

    @Override
    public CompletableFuture<Long> consumeIgnoringRateLimits(long l) {
      return null;
    }

    @Override
    public CompletableFuture<ConsumptionProbe> tryConsumeAndReturnRemaining(long l) {
      final ConsumptionProbe consumptionProbe = ConsumptionProbe.rejected(0, 5);
      return CompletableFuture.completedFuture(consumptionProbe);
    }

    @Override
    public CompletableFuture<EstimationProbe> estimateAbilityToConsume(long l) {
      return null;
    }

    @Override
    public CompletableFuture<Long> tryConsumeAsMuchAsPossible() {
      return null;
    }

    @Override
    public CompletableFuture<Long> tryConsumeAsMuchAsPossible(long l) {
      return null;
    }

    @Override
    public CompletableFuture<Void> addTokens(long l) {
      return null;
    }

    @Override
    public CompletableFuture<Void> forceAddTokens(long tokensToAdd) {
      return null;
    }

    @Override
    public CompletableFuture<Void> replaceConfiguration(BucketConfiguration newConfiguration, TokensInheritanceStrategy tokensInheritanceStrategy) {
      return null;
    }

  }

}