package io.github.arlol.feed;

import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Channel {

	@Id
	private Long id;
	private String name;
	private String link;
	private List<String> feeds;
	private List<String> categories;

}
