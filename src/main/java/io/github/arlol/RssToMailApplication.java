package io.github.arlol;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.apache.hc.client5.http.cache.CacheResponseStatus;
import org.apache.hc.client5.http.cache.HttpCacheContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ProtocolException;
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
import io.github.arlol.feed.Feed;
import io.github.arlol.feed.Feed.FeedBuilder;
import io.github.arlol.feed.FeedItem;
import io.github.arlol.feed.FeedItemProcessor;
import io.github.arlol.feed.FeedItemRepository;
import io.github.arlol.feed.FeedRepository;
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
	FeedRepository feedRepository;
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
		rssToMailProperties.configs().forEach(config -> {
			log.info("config: {}", config);
			config.channels()
					.stream()
					.map(channelRepository::mergeByLink)
					.forEach(channel -> this.process(config, channel));
		});
	}

	private void process(Config config, Channel channel) {
		for (String url : channel.getFeeds()) {

			Feed feed = feedRepository
					.findByChannelIdAndUrl(channel.getId(), url)
					.orElseGet(() -> {
						return feedRepository.save(
								Feed.builder()
										.channelId(channel.getId())
										.url(url)
										.build()
						);
					});

			FeedBuilder feedBuilder = feed.toBuilder();

			executeHttpRequest(feed, (response, context) -> {

				silentGetHeader(response, "ETag").map(Header::getValue)
						.ifPresent(etag -> {
							feedBuilder.etag(etag);
						});

				silentGetHeader(response, "Last-Modified").map(Header::getValue)
						.ifPresent(lastModified -> {
							feedBuilder.lastModified(lastModified);
						});

				silentGetHeader(response, "Cache-Control").map(Header::getValue)
						.ifPresent(cacheControl -> {
							log.info("cache-control {}", cacheControl);
						});

				CacheResponseStatus responseStatus = context
						.getCacheResponseStatus();
				switch (responseStatus) {
				case CACHE_HIT:
					log.info(
							"A response was generated from the cache with "
									+ "no requests sent upstream"
					);
					break;
				case CACHE_MODULE_RESPONSE:
					log.info(
							"The response was generated directly by the "
									+ "caching module"
					);
					break;
				case CACHE_MISS:
					log.info("The response came from an upstream server");
					break;
				case VALIDATED:
					log.info(
							"The response was generated from the cache "
									+ "after validating the entry with the origin server"
					);
					break;
				case FAILURE:
					log.info(
							"The response came from an upstream server after a cache failure"
					);
					break;
				default:
					break;
				}

				if (response.getCode() == 304) {
					log.info("skipped since it was not modified: {}", feed);
					return;
				}

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
				log.info("got these articles from feed {}: {}", feed, articles);
			});

			feedRepository.save(feedBuilder.build());
		}

		if (rssToMailProperties.mailSendingEnabled()) {
			while (feedItemProcessor
					.processMails(channel, config.from(), config.to())) {
			}
		}

	}

	private Optional<Header> silentGetHeader(
			ClassicHttpResponse response,
			String name
	) {
		try {
			return Optional.ofNullable(response.getHeader(name));
		} catch (ProtocolException e) {
			throw new IllegalStateException(e);
		}
	}

	@FunctionalInterface
	private interface ClassicHttpResponseConsumer {

		void accept(ClassicHttpResponse response, HttpCacheContext context)
				throws IOException;

	}

	public <T> T executeHttpRequest(
			final Feed feed,
			final ClassicHttpResponseConsumer consumer
	) {
		try {
			HttpCacheContext context = HttpCacheContext.create();
			var requestBuilder = ClassicRequestBuilder.get(feed.getUrl());
			if (feed.getEtag() != null) {
				requestBuilder.addHeader("If-None-Match", feed.getEtag());
			}
			if (feed.getLastModified() != null) {
				requestBuilder
						.addHeader("If-Modified-Since", feed.getLastModified());
			}
			return httpClient
					.execute(requestBuilder.build(), context, response -> {
						consumer.accept(response, context);
						return null;
					});
		} catch (IOException e) {
			log.error("Could not get feed {}", feed, e);
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
