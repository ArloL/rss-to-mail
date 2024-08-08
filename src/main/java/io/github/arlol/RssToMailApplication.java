package io.github.arlol;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import io.github.arlol.RssToMailProperties.Config;
import io.github.arlol.feed.Channel;
import io.github.arlol.feed.ChannelRepository;
import io.github.arlol.feed.FeedItem;
import io.github.arlol.feed.FeedItemProcessor;
import io.github.arlol.feed.FeedItemRepository;
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
	@Autowired
	CloseableHttpClient httpClient;

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
		for (String url : channel.getFeeds()) {
			executeSilentGet(url, response -> {
				var articles = new RssReader()
						.read(response.getEntity().getContent())
						.map(item -> toFeedItem(item, channel))
						.filter(item -> {
							if (channel.getCategories() == null
									|| channel.getCategories().isEmpty()) {
								return true;
							}
							for (String category : item.getCategories()) {
								if (channel.getCategories()
										.contains(category)) {
									return true;
								}
							}
							return false;
						})
						.filter(
								item -> item.getPublished().isAfter(CUTOFF_DATE)
						)
						.map(feedItemRepository::mergeByGuid)
						.map(item -> item.getTitle())
						.toList();
				log.info("got these articles from feed {}: {}", url, articles);
			});

		}
		if (rssToMailProperties.isMailSendingEnabled()) {
			while (feedItemProcessor
					.processMails(channel, config.getFrom(), config.getTo())) {
			}
		}
	}

	@FunctionalInterface
	private interface ClassicHttpResponseConsumer {

		void accept(ClassicHttpResponse response) throws IOException;

	}

	public <T> T executeSilentGet(
			final String url,
			final ClassicHttpResponseConsumer consumer
	) {
		try {
			var request = ClassicRequestBuilder.get(url).build();
			return httpClient.execute(request, null, response -> {
				consumer.accept(response);
				return null;
			});
		} catch (IOException e) {
			throw new UncheckedIOException(e);
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
