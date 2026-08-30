package io.github.arlol.fake;

import java.util.Objects;
import java.util.Optional;

import io.github.arlol.feed.Feed;
import io.github.arlol.feed.FeedRepository;

public final class FakeFeedRepository extends FakeCrudRepository<Feed>
		implements FeedRepository {

	@Override
	@SuppressWarnings("unchecked")
	public <S extends Feed> S save(S feed) {
		Feed stored = feed.id() == null
				? feed.toBuilder().id(++sequence).build()
				: feed;
		entities.removeIf(
				existing -> Objects.equals(existing.id(), stored.id())
		);
		entities.add(stored);
		return (S) stored;
	}

	@Override
	public Optional<Feed> findByChannelIdAndUrl(Long channelId, String url) {
		return entities.stream()
				.filter(
						feed -> Objects.equals(feed.channelId(), channelId)
								&& Objects.equals(feed.url(), url)
				)
				.findFirst();
	}

	public Optional<Feed> onlyFeed() {
		return entities.stream().findFirst();
	}

}
