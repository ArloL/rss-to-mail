package io.github.arlol.feed;

import java.time.OffsetDateTime;
import java.util.List;

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
	private List<String> categories;
	private String guid;
	private Boolean isPermaLink;
	private String pubDate;
	private boolean processed;
	private OffsetDateTime published;

}
