package io.github.arlol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.HttpCacheStorageEntry;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CachingHttpClients;
import org.apache.hc.client5.http.impl.cache.ehcache.EhcacheHttpCacheStorage;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpUtilsConfiguration {

	@Bean(initMethod = "init")
	CacheManager cacheManager(RssToMailProperties rssToMailProperties) {
		return CacheManagerBuilder.newCacheManagerBuilder()
				.with(
						CacheManagerBuilder.persistence(
								Path.of(rssToMailProperties.cacheDir())
										.resolve("ehcache")
										.toFile()
						)
				)
				.build();
	}

	@Bean
	Cache<String, HttpCacheStorageEntry> cache(CacheManager cacheManager) {
		return cacheManager.createCache(
				"http",
				CacheConfigurationBuilder.newCacheConfigurationBuilder(
						String.class,
						HttpCacheStorageEntry.class,
						ResourcePoolsBuilder.newResourcePoolsBuilder()
								.disk(20, MemoryUnit.MB, true)
				)
		);
	}

	@Bean
	CacheConfig cacheConfig() {
		return CacheConfig.custom()
				.setMaxCacheEntries(1000)
				.setMaxObjectSize(8192)
				.build();
	}

	@Bean
	HttpCacheStorage httpCacheStorage(
			Cache<String, HttpCacheStorageEntry> cache,
			CacheConfig cacheConfig
	) {
		return EhcacheHttpCacheStorage.createObjectCache(cache, cacheConfig);
	}

	@Bean
	CloseableHttpClient httpClient(
			CacheConfig cacheConfig,
			HttpCacheStorage httpCacheStorage,
			RssToMailProperties rssToMailProperties
	) throws IOException {
		Path cacheDir = Path.of(rssToMailProperties.cacheDir())
				.resolve("apache");
		Files.createDirectories(cacheDir);

		var timeout = Timeout.ofSeconds(45);
		var connectionConfig = ConnectionConfig.custom()
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
		var requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeout)
				.setResponseTimeout(timeout)
				.build();
		var connectionManager = PoolingHttpClientConnectionManagerBuilder
				.create()
				.setDefaultConnectionConfig(connectionConfig)
				.build();

		return CachingHttpClients.custom()
				.setCacheConfig(cacheConfig)
				.setCacheDir(cacheDir.toFile())
				.setDeleteCache(false)
				.setHttpCacheStorage(httpCacheStorage)
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connectionManager)
				.build();
	}

}
