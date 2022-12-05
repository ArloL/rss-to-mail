package io.github.arlol.feed;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface FeedItemRepository extends CrudRepository<FeedItem, Long> {

	Optional<FeedItem> findByGuid(String guid);

	@Transactional
	default FeedItem mergeByGuid(FeedItem feedItem) {
		return save(
				findByGuid(feedItem.getGuid())
						.map(
								fi -> feedItem.toBuilder()
										.id(fi.getId())
										.processed(fi.isProcessed())
										.build()
						)
						.orElse(feedItem)
		);
	}

}
