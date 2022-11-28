package io.github.arlol.feed;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface FeedItemRepository extends CrudRepository<FeedItem, Long> {

	Optional<FeedItem> findByGuid(String guid);

}
