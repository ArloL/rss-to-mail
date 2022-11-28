package io.github.arlol.feed;

import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class FeedItem {

	@Id
	private Long id;
	private Long channelId;
	private String title;
	private String description;
	private String link;
	private String author;
	private String category;
	private String guid;
	private Boolean isPermaLink;
	private String pubDate;

}
