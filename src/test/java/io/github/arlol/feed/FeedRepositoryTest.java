package io.github.arlol.feed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class FeedRepositoryTest {

	@Autowired
	FeedRepository feedRepository;

	@Test
	void testName() throws Exception {
		Feed feed = feedRepository.save(
				Feed.builder()
						.channelId(1L)
						.url("https://www.kraftfuttermischwerk.de/")
						.etag("etag")
						.build()
		);

		assertThat(feedRepository.findAll()).singleElement().isEqualTo(feed);

		feed = feed.toBuilder().etag("newetag").build();
		feed = feedRepository.save(feed);

		assertThat(feedRepository.findAll()).singleElement().isEqualTo(feed);
	}

}
