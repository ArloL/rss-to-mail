package io.github.arlol;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.apptasticsoftware.rssreader.Item;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import io.github.arlol.RssToMailProperties.Config;
import io.github.arlol.feed.Channel;
import io.github.arlol.feed.ChannelRepository;
import io.github.arlol.feed.FeedItem;
import io.github.arlol.feed.FeedItemProcessor;
import io.github.arlol.feed.FeedItemRepository;
import io.github.arlol.feed.SilentRssReader;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableEncryptableProperties
@EnableConfigurationProperties(RssToMailProperties.class)
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
	RssToMailProperties rssToMailProperties;
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
		rssToMailProperties.getConfigs().forEach(config -> {
			log.info("config: {}", config);
			config.getChannels()
					.stream()
					.map(channelRepository::mergeByLink)
					.forEach(channel -> this.process(config, channel));
		});
	}

	private void process(Config config, Channel channel) {
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
			log.info("got these articles from feed {}: {}", string, articles);
		}
		if (rssToMailProperties.isMailSendingEnabled()) {
			while (feedItemProcessor
					.processMails(channel, config.getFrom(), config.getTo())) {
			}
		}
	}

	private static FeedItem toFeedItem(Item item, Channel channel) {
		return FeedItem.builder()
				.channelId(channel.getId())
				.title(item.getTitle().orElse(""))
				.description(item.getDescription().orElse(null))
				.link(item.getLink().orElse(null))
				.author(item.getAuthor().orElse(null))
				.categories(item.getCategories())
				.guid(item.getGuid().orElse(null))
				.isPermaLink(item.getIsPermaLink().orElse(Boolean.FALSE))
				.pubDate(item.getPubDate().orElse(null))
				.published(
						item.getPubDateZonedDateTime()
								.map(OffsetDateTime::from)
								.orElse(null)
				)
				.build();
	}

}
