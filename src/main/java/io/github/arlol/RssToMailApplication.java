package io.github.arlol;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.apptasticsoftware.rssreader.Item;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import io.github.arlol.feed.Channel;
import io.github.arlol.feed.ChannelRepository;
import io.github.arlol.feed.FeedItem;
import io.github.arlol.feed.FeedItemProcessor;
import io.github.arlol.feed.FeedItemRepository;
import io.github.arlol.feed.SilentRssReader;
import io.github.arlol.mail.MailProperties;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableEncryptableProperties
@EnableConfigurationProperties(MailProperties.class)
@Slf4j
public class RssToMailApplication implements ApplicationRunner {

	public static void main(String[] args) {
		if (args.length == 1 && "--version".equals(args[0])) {
			Package pkg = RssToMailApplication.class.getPackage();
			String title = pkg.getImplementationTitle();
			String version = pkg.getImplementationVersion();
			System.out.println(title + " " + version);
			return;
		}
		SpringApplication.run(RssToMailApplication.class, args);
	}

	@Autowired
	FeedItemRepository feedItemRepository;
	@Autowired
	ChannelRepository channelRepository;
	@Autowired
	FeedItemProcessor feedItemProcessor;

	private static final OffsetDateTime CUTOFF_DATE = OffsetDateTime
			.of(2022, 12, 1, 8, 0, 0, 0, ZoneOffset.ofHours(+1));

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String eigentlichHeißenWirKlaus = "https://feeds.soundcloud.com/users/soundcloud:users:546708438/sounds.rss";
		String montagssorbet = "https://feeds.soundcloud.com/playlists/soundcloud:playlists:2111915/sounds.rss";

		var channels = new ArrayList<Channel>();
		channels.add(
				Channel.builder()
						.link("https://www.kraftfuttermischwerk.de/")
						.feeds(
								List.of(
										"https://www.kraftfuttermischwerk.de/blogg/feed/"
								)
						)
						.categories(List.of("DJ-Mix", "Live-Set"))
						.build()
		);

		List.of(
				"https://groove.de/category/podcast/feed/",
				"https://ra.co/xml/podcast.xml",
				montagssorbet,
				eigentlichHeißenWirKlaus,
				"https://fiehe.info/1live-fiehe-podcast.rss"
		).stream().map(link -> {
			return Channel.builder().link(link).feeds(List.of(link)).build();
		}).forEach(channels::add);

		channels.stream()
				.map(channelRepository::mergeByLink)
				.forEach(this::process);

		while (feedItemProcessor.processMails()) {
		}
	}

	private void process(Channel channel) {
		for (String string : channel.getFeeds()) {
			var articles = new SilentRssReader().read(string)
					.map(item -> toFeedItem(item, channel))
					.filter(item -> {
						if (channel.getCategories() == null
								|| channel.getCategories().isEmpty()) {
							return true;
						}
						for (String category : item.getCategories()) {
							if (channel.getCategories().contains(category)) {
								return true;
							}
						}
						return false;
					})
					.filter(item -> item.getPublished().isAfter(CUTOFF_DATE))
					.map(feedItemRepository::mergeByGuid)
					.map(item -> item.getTitle())
					.toList();
			log.info("{}", articles);
		}
	}

	private static FeedItem toFeedItem(Item item, Channel channel) {
		return FeedItem.builder()
				.channelId(channel.getId())
				.title(item.getTitle().orElse(null))
				.description(item.getDescription().orElse(null))
				.link(item.getLink().orElse(null))
				.author(item.getAuthor().orElse(null))
				.category(item.getCategory().orElse(null))
				.categories(item.getCategories())
				.guid(item.getGuid().orElse(null))
				.isPermaLink(item.getIsPermaLink().orElse(null))
				.pubDate(item.getPubDate().orElse(null))
				.published(
						item.getPubDateZonedDateTime()
								.map(OffsetDateTime::from)
								.orElse(null)
				)
				.build();
	}

}
