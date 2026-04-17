package io.github.arlol.feed;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;

public record FeedItem(
		@Id Long id,
		Long channelId,
		String title,
		String description,
		String link,
		String author,
		List<String> categories,
		String guid,
		Boolean isPermaLink,
		String pubDate,
		boolean processed,
		OffsetDateTime published
) {

	public static Builder builder() {
		return new Builder();
	}

	public Builder toBuilder() {
		return new Builder(this);
	}

	public static final class Builder {

		private Long id;
		private Long channelId;
		private String title;
		private String description;
		private String link;
		private String author;
		private List<String> categories;
		private String guid;
		private Boolean isPermaLink;
		private String pubDate;
		private boolean processed;
		private OffsetDateTime published;

		private Builder() {
		}

		private Builder(FeedItem feedItem) {
			this.id = feedItem.id;
			this.channelId = feedItem.channelId;
			this.title = feedItem.title;
			this.description = feedItem.description;
			this.link = feedItem.link;
			this.author = feedItem.author;
			this.categories = feedItem.categories;
			this.guid = feedItem.guid;
			this.isPermaLink = feedItem.isPermaLink;
			this.pubDate = feedItem.pubDate;
			this.processed = feedItem.processed;
			this.published = feedItem.published;
		}

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder channelId(Long channelId) {
			this.channelId = channelId;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder link(String link) {
			this.link = link;
			return this;
		}

		public Builder author(String author) {
			this.author = author;
			return this;
		}

		public Builder categories(List<String> categories) {
			this.categories = categories;
			return this;
		}

		public Builder guid(String guid) {
			this.guid = guid;
			return this;
		}

		public Builder isPermaLink(Boolean isPermaLink) {
			this.isPermaLink = isPermaLink;
			return this;
		}

		public Builder pubDate(String pubDate) {
			this.pubDate = pubDate;
			return this;
		}

		public Builder processed(boolean processed) {
			this.processed = processed;
			return this;
		}

		public Builder published(OffsetDateTime published) {
			this.published = published;
			return this;
		}

		public FeedItem build() {
			return new FeedItem(
					id,
					channelId,
					title,
					description,
					link,
					author,
					categories,
					guid,
					isPermaLink,
					pubDate,
					processed,
					published
			);
		}

	}

}
