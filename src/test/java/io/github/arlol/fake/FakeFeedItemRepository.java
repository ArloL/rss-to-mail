package io.github.arlol.fake;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.github.arlol.feed.FeedItem;
import io.github.arlol.feed.FeedItemRepository;

public final class FakeFeedItemRepository extends FakeCrudRepository<FeedItem>
		implements FeedItemRepository {

	@Override
	@SuppressWarnings("unchecked")
	public <S extends FeedItem> S save(S feedItem) {
		FeedItem stored = feedItem.id() == null
				? feedItem.toBuilder().id(++sequence).build()
				: feedItem;
		entities.removeIf(
				existing -> Objects.equals(existing.id(), stored.id())
		);
		entities.add(stored);
		return (S) stored;
	}

	@Override
	public Optional<FeedItem> findByGuid(String guid) {
		return entities.stream()
				.filter(item -> Objects.equals(item.guid(), guid))
				.findFirst();
	}

	@Override
	public Optional<FeedItem> findFirstByChannelIdAndProcessedIsFalse(
			long channelId
	) {
		return entities.stream()
				.filter(
						item -> Objects.equals(item.channelId(), channelId)
								&& !item.processed()
				)
				.findFirst();
	}

	public List<String> titles() {
		return entities.stream().map(FeedItem::title).toList();
	}

	public List<FeedItem> items() {
		return List.copyOf(entities);
	}

}
