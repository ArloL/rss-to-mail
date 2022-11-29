package io.github.arlol;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.apptasticsoftware.rssreader.Item;

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
		Channel channel = channelRepository
				.mergeByLink(Channel.builder().link(url).build());
		var articles = new SilentRssReader().read(url)
				.map(item -> toFeedItem(item, channel))
				.map(feedItemRepository::mergeByGuid)
				.map(item -> item.getTitle())
				.toList();
		log.info("{}", articles);
	}

	private static FeedItem toFeedItem(Item item, Channel channel) {
		return FeedItem.builder()
				.channelId(channel.getId())
				.title(item.getTitle().orElse(null))
				.description(item.getDescription().orElse(null))
				.link(item.getLink().orElse(null))
				.author(item.getAuthor().orElse(null))
				.category(item.getCategory().orElse(null))
				.guid(item.getGuid().orElse(null))
				.isPermaLink(item.getIsPermaLink().orElse(null))
				.pubDate(item.getPubDate().orElse(null))
				.build();
	}

}
