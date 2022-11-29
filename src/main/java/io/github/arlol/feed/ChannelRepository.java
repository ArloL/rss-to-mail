package io.github.arlol.feed;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ChannelRepository extends CrudRepository<Channel, Long> {

	Optional<Channel> findByLink(String link);

	@Transactional
	default Channel mergeByLink(Channel channel) {
		return save(
				findByLink(channel.getLink())
						.map(c -> channel.toBuilder().id(c.getId()).build())
						.orElse(channel)
		);
	}

}
