package io.github.arlol.feed;

import java.util.List;

import org.springframework.data.annotation.Id;

public record Channel(
		@Id Long id,
		String name,
		String link,
		List<String> feeds,
		List<String> categories
) {

	public static Builder builder() {
		return new Builder();
	}

	public Builder toBuilder() {
		return new Builder(this);
	}

	public static final class Builder {

		private Long id;
		private String name;
		private String link;
		private List<String> feeds;
		private List<String> categories;

		private Builder() {
		}

		private Builder(Channel channel) {
			this.id = channel.id;
			this.name = channel.name;
			this.link = channel.link;
			this.feeds = channel.feeds;
			this.categories = channel.categories;
		}

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder link(String link) {
			this.link = link;
			return this;
		}

		public Builder feeds(List<String> feeds) {
			this.feeds = feeds;
			return this;
		}

		public Builder categories(List<String> categories) {
			this.categories = categories;
			return this;
		}

		public Channel build() {
			return new Channel(id, name, link, feeds, categories);
		}

	}

}
