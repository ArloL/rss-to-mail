package io.github.arlol.feed;

import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Channel {

	@Id
	private Long id;
	private String link;

}
