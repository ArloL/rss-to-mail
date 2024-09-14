package io.github.arlol.feed;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface FeedRepository extends CrudRepository<Feed, Long> {

	Optional<Feed> findByChannelIdAndUrl(Long channelId, String url);

}
