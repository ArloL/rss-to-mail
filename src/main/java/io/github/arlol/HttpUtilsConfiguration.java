package io.github.arlol;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpUtilsConfiguration {

	@Bean
	CloseableHttpClient httpClient() {
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
		return HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connectionManager)
				.build();
	}

}
