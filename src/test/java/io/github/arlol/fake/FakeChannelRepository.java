package io.github.arlol.fake;

import java.util.Objects;
import java.util.Optional;

import io.github.arlol.feed.Channel;
import io.github.arlol.feed.ChannelRepository;

public final class FakeChannelRepository extends FakeCrudRepository<Channel>
		implements ChannelRepository {

	@Override
	@SuppressWarnings("unchecked")
	public <S extends Channel> S save(S channel) {
		Channel stored = channel.id() == null
				? channel.toBuilder().id(++sequence).build()
				: channel;
		entities.removeIf(
				existing -> Objects.equals(existing.id(), stored.id())
		);
		entities.add(stored);
		return (S) stored;
	}

	@Override
	public Optional<Channel> findByLink(String link) {
		return entities.stream()
				.filter(channel -> Objects.equals(channel.link(), link))
				.findFirst();
	}

}
