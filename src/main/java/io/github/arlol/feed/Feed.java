package io.github.arlol.feed;

import org.springframework.data.annotation.Id;

public record Feed(
		@Id Long id,
		Long channelId,
		String url,
		String etag,
		String lastModified
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
		private String url;
		private String etag;
		private String lastModified;

		private Builder() {
		}

		private Builder(Feed feed) {
			this.id = feed.id;
			this.channelId = feed.channelId;
			this.url = feed.url;
			this.etag = feed.etag;
			this.lastModified = feed.lastModified;
		}

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder channelId(Long channelId) {
			this.channelId = channelId;
			return this;
		}

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public Builder etag(String etag) {
			this.etag = etag;
			return this;
		}

		public Builder lastModified(String lastModified) {
			this.lastModified = lastModified;
			return this;
		}

		public Feed build() {
			return new Feed(id, channelId, url, etag, lastModified);
		}

	}

}
