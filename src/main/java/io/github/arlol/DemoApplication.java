package io.github.arlol;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.arlol.feed.Channel;
import io.github.arlol.feed.ChannelRepository;
import io.github.arlol.feed.FeedItem;
import io.github.arlol.feed.FeedItemRepository;
import io.github.arlol.feed.SilentRssReader;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class DemoApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	FeedItemRepository feedItemRepository;
	@Autowired
	ChannelRepository channelRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// wird auch immer auf dem blog gepostet
		// String derdaskfmwak =
		// "https://feeds.soundcloud.com/users/soundcloud:users:62364534/sounds.rss";
		String eigentlichHeißenWirKlaus = "https://feeds.soundcloud.com/users/soundcloud:users:546708438/sounds.rss";
		String montagssorbet = "https://feeds.soundcloud.com/playlists/soundcloud:playlists:2111915/sounds.rss";
		List.of(
				"https://groove.de/category/podcast/feed/",
				"https://ra.co/xml/podcast.xml",
				montagssorbet,
				eigentlichHeißenWirKlaus,
				"https://www.kraftfuttermischwerk.de/blogg/tag/dj-mix/feed/",
				"https://fiehe.info/1live-fiehe-podcast.rss"
		).forEach(this::process);
	}

	private void process(String url) {
		Channel channel = Stream.of(Channel.builder().link(url).build())
				.map(
						c1 -> channelRepository.findByLink(c1.getLink())
								.map(
										c2 -> c1.toBuilder()
												.id(c2.getId())
												.build()
								)
								.orElse(c1)
				)
				.map(channelRepository::save)
				.findFirst()
				.orElseThrow();
		var articles = new SilentRssReader().read(url)
				.map(
						item -> FeedItem.builder()
								.channelId(channel.getId())
								.title(item.getTitle().orElse(null))
								.description(item.getDescription().orElse(null))
								.link(item.getLink().orElse(null))
								.author(item.getAuthor().orElse(null))
								.category(item.getCategory().orElse(null))
								.guid(item.getGuid().orElse(null))
								.isPermaLink(item.getIsPermaLink().orElse(null))
								.pubDate(item.getPubDate().orElse(null))
								.build()
				)
				.map(
						i1 -> feedItemRepository.findByGuid(i1.getGuid())
								.map(
										i2 -> i1.toBuilder()
												.id(i2.getId())
												.build()
								)
								.orElse(i1)
				)
				.map(feedItemRepository::save)
				.map(item -> item.getTitle())
				.toList();
		log.info("{}", articles);
	}

}
