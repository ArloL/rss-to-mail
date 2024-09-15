package io.github.arlol;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.arlol.feed.Channel;

@ConfigurationProperties(prefix = "rss-to-mail")
public record RssToMailProperties(
		boolean mailSendingEnabled,
		String cacheDir,
		List<Config> configs
) {

	public static record Config(
			String from,
			String[] to,
			List<Channel> channels
	) {

		@Override
		public String toString() {
			return "Config [from=" + from + ", to=" + Arrays.toString(to)
					+ ", channels=" + channels + "]";
		}

	}

}
