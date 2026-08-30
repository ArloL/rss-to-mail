package io.github.arlol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.apache.hc.client5.http.cache.CacheResponseStatus;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.DefaultApplicationArguments;

import io.github.arlol.RssToMailProperties.Config;
import io.github.arlol.fake.FakeChannelRepository;
import io.github.arlol.fake.FakeFeedItemRepository;
import io.github.arlol.fake.FakeFeedRepository;
import io.github.arlol.fake.FakeHttpClient;
import io.github.arlol.fake.RecordingMailSender;
import io.github.arlol.feed.Channel;
import io.github.arlol.feed.FeedItem;
import io.github.arlol.feed.FeedItemProcessor;

class RssToMailApplicationTest {

	private static final String CHANNEL_LINK = "https://example.com";
	private static final String FEED_URL = "https://example.com/rss";
	private static final String LAST_MODIFIED = "Wed, 04 Jan 2023 08:00:00 GMT";

	// the cutoff date in RssToMailApplication is 2022-12-01T08:00+01:00
	private static final String RSS = """
			<?xml version="1.0" encoding="UTF-8"?>
			<rss version="2.0">
				<channel>
					<title>Example</title>
					<link>https://example.com</link>
					<description>Example feed</description>
					<item>
						<title>Tech article</title>
						<link>https://example.com/tech</link>
						<guid isPermaLink="true">https://example.com/tech</guid>
						<category>tech</category>
						<pubDate>Tue, 03 Jan 2023 08:00:00 +0100</pubDate>
					</item>
					<item>
						<title>Sports article</title>
						<link>https://example.com/sports</link>
						<guid isPermaLink="false">sports-guid</guid>
						<category>sports</category>
						<pubDate>Wed, 04 Jan 2023 08:00:00 +0100</pubDate>
					</item>
					<item>
						<title>Ancient article</title>
						<link>https://example.com/ancient</link>
						<guid isPermaLink="false">ancient-guid</guid>
						<category>tech</category>
						<pubDate>Fri, 03 Jan 2020 08:00:00 +0100</pubDate>
					</item>
				</channel>
			</rss>
			""";

	private static final String RSS_WITHOUT_PUBDATE = """
			<?xml version="1.0" encoding="UTF-8"?>
			<rss version="2.0">
				<channel>
					<title>Example</title>
					<link>https://example.com</link>
					<description>Example feed</description>
					<item>
						<title>Undated article</title>
						<link>https://example.com/undated</link>
						<guid isPermaLink="false">undated-guid</guid>
					</item>
				</channel>
			</rss>
			""";

	private final FakeChannelRepository channelRepository = new FakeChannelRepository();
	private final FakeFeedRepository feedRepository = new FakeFeedRepository();
	private final FakeFeedItemRepository feedItemRepository = new FakeFeedItemRepository();
	private final RecordingMailSender mailSender = new RecordingMailSender();

	@Test
	void storesOnlyArticlesMatchingTheConfiguredCategories() throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(request -> okResponse());

		run(properties(false, channel(List.of("tech")), List.of()), httpClient);

		assertThat(feedItemRepository.titles()).containsExactly("Tech article");
	}

	@Test
	void storesEveryRecentArticleWhenCategoriesAreEmpty() throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(request -> okResponse());

		run(properties(false, channel(List.of()), List.of()), httpClient);

		assertThat(feedItemRepository.titles())
				.containsExactly("Tech article", "Sports article");
	}

	@Test
	void storesEveryRecentArticleWhenCategoriesAreNull() throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(request -> okResponse());

		run(properties(false, channel(null), List.of()), httpClient);

		assertThat(feedItemRepository.titles())
				.containsExactly("Tech article", "Sports article");
	}

	@Test
	void storesTheParsedArticleFields() throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(request -> okResponse());

		run(properties(false, channel(List.of()), List.of()), httpClient);

		FeedItem item = feedItemRepository.items().get(0);
		assertThat(item.title()).isEqualTo("Tech article");
		assertThat(item.link()).isEqualTo("https://example.com/tech");
		assertThat(item.guid()).isEqualTo("https://example.com/tech");
		assertThat(item.isPermaLink()).isTrue();
		assertThat(item.categories()).containsExactly("tech");
		assertThat(item.published()).isNotNull();
		assertThat(item.processed()).isFalse();
	}

	@Test
	void storesCacheValidatorsAndSendsThemOnTheNextRun() throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(request -> okResponse());
		RssToMailProperties properties = properties(
				false,
				channel(List.of()),
				List.of()
		);

		run(properties, httpClient);

		assertThat(feedRepository.onlyFeed()).hasValueSatisfying(feed -> {
			assertThat(feed.url()).isEqualTo(FEED_URL);
			assertThat(feed.etag()).isEqualTo("etag-1");
			assertThat(feed.lastModified()).isEqualTo(LAST_MODIFIED);
		});

		run(properties, httpClient);

		assertThat(httpClient.requests()).hasSize(2);
		assertThat(httpClient.requests().get(1).getFirstHeader("If-None-Match"))
				.extracting(Object::toString)
				.isEqualTo("If-None-Match: etag-1");
		assertThat(
				httpClient.requests().get(1).getFirstHeader("If-Modified-Since")
		).extracting(Object::toString)
				.isEqualTo("If-Modified-Since: " + LAST_MODIFIED);
	}

	@Test
	void skipsParsingWhenTheFeedWasNotModified() throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(
					304
			);
			response.addHeader("ETag", "etag-2");
			return response;
		});

		run(properties(false, channel(List.of()), List.of()), httpClient);

		assertThat(feedItemRepository.titles()).isEmpty();
		assertThat(feedRepository.onlyFeed()).hasValueSatisfying(
				feed -> assertThat(feed.etag()).isEqualTo("etag-2")
		);
	}

	@ParameterizedTest
	@EnumSource(CacheResponseStatus.class)
	void readsTheFeedForEveryCacheResponseStatus(CacheResponseStatus status)
			throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(request -> okResponse());
		httpClient.cacheResponseStatus(status);

		run(properties(false, channel(List.of()), List.of()), httpClient);

		assertThat(feedItemRepository.titles())
				.containsExactly("Tech article", "Sports article");
	}

	@Test
	void sendsOneMailPerUnprocessedArticle() throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(request -> okResponse());

		run(
				properties(
						true,
						channel(List.of()),
						List.of("a@example.com", "b@example.com")
				),
				httpClient
		);

		assertThat(mailSender.messages()).hasSize(2);
		assertThat(mailSender.messages().get(0).getFrom())
				.isEqualTo("from@example.com");
		assertThat(mailSender.messages().get(0).getTo())
				.containsExactly("a@example.com", "b@example.com");
		assertThat(mailSender.messages().get(0).getSubject())
				.isEqualTo("Tech article - Example");
		assertThat(mailSender.messages().get(0).getText())
				.isEqualTo("https://example.com/tech");
		assertThat(mailSender.messages().get(1).getText())
				.isEqualTo("https://example.com/sports");
		assertThat(feedItemRepository.items()).isNotEmpty()
				.allMatch(FeedItem::processed);
	}

	@Test
	void sendsNoMailsWhenMailSendingIsDisabled() throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(request -> okResponse());

		run(
				properties(false, channel(List.of()), List.of("a@example.com")),
				httpClient
		);

		assertThat(mailSender.messages()).isEmpty();
		assertThat(feedItemRepository.items()).isNotEmpty()
				.noneMatch(FeedItem::processed);
	}

	@Test
	void keepsArticlesWithoutAPublicationDate() throws Exception {
		FakeHttpClient httpClient = new FakeHttpClient(
				request -> okResponse(RSS_WITHOUT_PUBDATE)
		);

		run(properties(false, channel(List.of()), List.of()), httpClient);

		assertThat(feedItemRepository.titles())
				.containsExactly("Undated article");
		assertThat(feedItemRepository.items().get(0).published()).isNull();
	}

	@Test
	void failsWhenTheResponseCarriesAmbiguousHeaders() {
		FakeHttpClient httpClient = new FakeHttpClient(request -> {
			BasicClassicHttpResponse response = new BasicClassicHttpResponse(
					200
			);
			response.addHeader("ETag", "etag-1");
			response.addHeader("ETag", "etag-2");
			return response;
		});
		RssToMailProperties properties = properties(
				false,
				channel(List.of()),
				List.of()
		);

		assertThatThrownBy(() -> run(properties, httpClient))
				.isInstanceOf(IllegalStateException.class)
				.hasRootCauseInstanceOf(ProtocolException.class);
	}

	@Test
	void wrapsTransportFailures() {
		FakeHttpClient httpClient = new FakeHttpClient(request -> {
			throw new IOException("connection reset");
		});
		RssToMailProperties properties = properties(
				false,
				channel(List.of()),
				List.of()
		);

		assertThatThrownBy(() -> run(properties, httpClient))
				.isInstanceOf(UncheckedIOException.class)
				.hasMessageContaining("connection reset");
	}

	private void run(RssToMailProperties properties, FakeHttpClient httpClient)
			throws Exception {
		new RssToMailApplication(
				properties,
				feedItemRepository,
				feedRepository,
				channelRepository,
				new FeedItemProcessor(feedItemRepository, mailSender),
				httpClient
		).run(new DefaultApplicationArguments());
	}

	private static RssToMailProperties properties(
			boolean mailSendingEnabled,
			Channel channel,
			List<String> to
	) {
		return new RssToMailProperties(
				mailSendingEnabled,
				"target/cache",
				List.of(new Config("from@example.com", to, List.of(channel)))
		);
	}

	private static Channel channel(List<String> categories) {
		return Channel.builder()
				.name("Example")
				.link(CHANNEL_LINK)
				.feeds(List.of(FEED_URL))
				.categories(categories)
				.build();
	}

	private static ClassicHttpResponse okResponse() {
		return okResponse(RSS);
	}

	private static ClassicHttpResponse okResponse(String body) {
		BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
		response.addHeader("ETag", "etag-1");
		response.addHeader("Last-Modified", LAST_MODIFIED);
		response.addHeader("Cache-Control", "max-age=60");
		response.setEntity(new StringEntity(body, ContentType.APPLICATION_XML));
		return response;
	}

}
