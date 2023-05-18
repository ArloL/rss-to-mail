package io.github.arlol.feed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class FeedItemRepositoryTest {

	@Autowired
	FeedItemRepository feedItemRepository;

	@Test
	void testName() throws Exception {
		FeedItem feedItem = feedItemRepository.save(
				FeedItem.builder()
						.channelId(1L)
						.title("I am a title")
						.guid("https://example.com")
						.isPermaLink(false)
						.pubDate("now")
						.build()
		);

		assertThat(
				feedItemRepository.findFirstByChannelIdAndProcessedIsFalse(1)
		).contains(feedItem);

		feedItem = feedItem.toBuilder().processed(true).build();
		feedItem = feedItemRepository.save(feedItem);

		assertThat(
				feedItemRepository.findFirstByChannelIdAndProcessedIsFalse(1)
		).isEmpty();
	}

}
