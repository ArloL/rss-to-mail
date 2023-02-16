package io.github.arlol;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.arlol.feed.Channel;

@ConfigurationProperties(prefix = "rss-to-mail")
public class RssToMailProperties {

	public class Config {

		private String from;
		private String[] to = new String[0];
		private List<Channel> channels = List.of();

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public String[] getTo() {
			return to;
		}

		public void setTo(String[] to) {
			this.to = to;
		}

		public List<Channel> getChannels() {
			return channels;
		}

		public void setChannels(List<Channel> channels) {
			this.channels = channels;
		}

	}

	private boolean mailSendingEnabled;
	private List<Config> configs = List.of();

	public boolean isMailSendingEnabled() {
		return mailSendingEnabled;
	}

	public void setMailSendingEnabled(boolean mailSendingEnabled) {
		this.mailSendingEnabled = mailSendingEnabled;
	}

	public List<Config> getConfigs() {
		return configs;
	}

	public void setConfigs(List<Config> configs) {
		this.configs = configs;
	}

}
