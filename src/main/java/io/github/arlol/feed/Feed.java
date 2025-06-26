package io.github.arlol.feed;

import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Feed {

	@Id
	private Long id;
	private Long channelId;
	private String url;
	private String etag;
	private String lastModified;

}
