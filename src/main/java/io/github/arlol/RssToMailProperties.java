package io.github.arlol;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.arlol.feed.Channel;

@ConfigurationProperties(prefix = "rss-to-mail")
public class RssToMailProperties {

	private List<Channel> channels = List.of();

	public List<Channel> getChannels() {
		return channels;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

}
