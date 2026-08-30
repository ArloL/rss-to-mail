package io.github.arlol;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.arlol.feed.Channel;

@ConfigurationProperties(prefix = "rss-to-mail")
public record RssToMailProperties(
		boolean mailSendingEnabled,
		String cacheDir,
		List<Config> configs
) {

	public RssToMailProperties {
		configs = Optional.ofNullable(configs).orElse(List.of());
	}

	public static record Config(
			String from,
			List<String> to,
			List<Channel> channels
	) {

		public Config {
			to = Optional.ofNullable(to).orElse(List.of());
			channels = Optional.ofNullable(channels).orElse(List.of());
		}

	}

}
