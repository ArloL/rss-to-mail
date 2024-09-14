package io.github.arlol.feed;

import org.springframework.data.annotation.Id;

import lombok.Builder;

@Builder(toBuilder = true)
public record Feed(
		@Id Long id,
		Long channelId,
		String url,
		String etag,
		String lastModified
) {

}
