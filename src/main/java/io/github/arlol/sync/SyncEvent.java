package io.github.arlol.sync;

import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SyncEvent {

	@Id
	private Long id;
	private String action;
	private Long feedItemId;

}
